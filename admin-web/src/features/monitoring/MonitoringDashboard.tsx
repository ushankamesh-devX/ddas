"use client";

import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { login, monitoringApi, subscribeToTelemetry } from "./api";
import { SensorMap } from "./SensorMap";
import type { Dam, Gate, IotDevice, LatestReading, Reading, Sensor } from "./types";

const stateColors: Record<Dam["operationalState"], string> = {
  NORMAL: "bg-emerald-100 text-emerald-800",
  WATCH: "bg-amber-100 text-amber-800",
  WARNING: "bg-orange-100 text-orange-800",
  EMERGENCY: "bg-red-100 text-red-800",
};

export function MonitoringDashboard() {
  const [token, setToken] = useState(() => typeof window === "undefined" ? "" : sessionStorage.getItem("ddas-token") ?? "");
  const [email, setEmail] = useState("admin@example.test");
  const [password, setPassword] = useState("");
  const [dams, setDams] = useState<Dam[]>([]);
  const [damId, setDamId] = useState("");
  const [sensors, setSensors] = useState<Sensor[]>([]);
  const [latest, setLatest] = useState<LatestReading[]>([]);
  const [gates, setGates] = useState<Gate[]>([]);
  const [devices, setDevices] = useState<IotDevice[]>([]);
  const [history, setHistory] = useState<Reading[]>([]);
  const [historySensor, setHistorySensor] = useState<Sensor | null>(null);
  const [streamState, setStreamState] = useState("Connecting…");
  const [error, setError] = useState("");
  const [secret, setSecret] = useState<IotDevice | null>(null);
  const [editingSensor, setEditingSensor] = useState<Sensor | null>(null);
  const [form, setForm] = useState({
    code: "", name: "", sensorType: "WATER_LEVEL", unit: "m", longitude: "80.70", latitude: "7.80",
    visibility: "PRIVATE" as Sensor["visibility"], warningThreshold: "", criticalThreshold: "",
  });

  const selectedDam = dams.find((dam) => dam.id === damId);

  const loadDam = useCallback(async (auth: string, selectedId: string) => {
    const [sensorData, latestData, gateData, deviceData] = await Promise.all([
      monitoringApi.sensors(auth, selectedId), monitoringApi.latest(auth, selectedId),
      monitoringApi.gates(auth, selectedId), monitoringApi.devices(auth, selectedId),
    ]);
    setSensors(sensorData);
    setLatest(latestData);
    setGates(gateData);
    setDevices(deviceData);
  }, []);

  useEffect(() => {
    if (!token) return;
    monitoringApi.dams(token).then((data) => {
      setDams(data);
      setDamId((current) => current || data[0]?.id || "");
    }).catch((reason: Error) => setError(reason.message));
  }, [token]);

  useEffect(() => {
    if (!token || !damId) return;
    const controller = new AbortController();
    queueMicrotask(() => loadDam(token, damId).catch((reason: Error) => setError(reason.message)));
    subscribeToTelemetry(token, damId, (reading) => {
      setStreamState("Live");
      setLatest((current) => {
        const next = current.filter((item) => item.sensorId !== reading.sensorId);
        return [reading, ...next];
      });
    }, controller.signal).catch((reason: Error) => {
      if (!controller.signal.aborted) setStreamState(reason.message);
    });
    return () => controller.abort();
  }, [damId, loadDam, token]);

  async function submitLogin(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      const accessToken = await login(email, password);
      sessionStorage.setItem("ddas-token", accessToken);
      setToken(accessToken);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Login failed");
    }
  }

  function logout() {
    sessionStorage.removeItem("ddas-token");
    setToken("");
    setDams([]);
    setDamId("");
  }

  async function submitSensor(event: FormEvent) {
    event.preventDefault();
    if (!damId) return;
    try {
      const values = {
        code: form.code, name: form.name, sensorType: form.sensorType, unit: form.unit,
        location: { type: "Point", coordinates: [Number(form.longitude), Number(form.latitude)] },
        visibility: form.visibility, exposeExactLocation: form.visibility === "PUBLIC",
        warningThreshold: form.warningThreshold ? Number(form.warningThreshold) : null,
        criticalThreshold: form.criticalThreshold ? Number(form.criticalThreshold) : null,
        thresholdDirection: "HIGH",
      };
      if (editingSensor) {
        await monitoringApi.updateSensor(token, damId, {
          ...editingSensor, ...values, location: values.location as Sensor["location"],
          thresholdDirection: "HIGH", status: editingSensor.status,
        });
      } else {
        await monitoringApi.createSensor(token, damId, values);
      }
      setEditingSensor(null);
      setForm((current) => ({ ...current, code: "", name: "" }));
      await loadDam(token, damId);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to create sensor");
    }
  }

  function editSensor(sensor: Sensor) {
    setEditingSensor(sensor);
    setForm({
      code: sensor.code, name: sensor.name, sensorType: sensor.sensorType, unit: sensor.unit,
      longitude: String(sensor.location?.coordinates[0] ?? 80.70),
      latitude: String(sensor.location?.coordinates[1] ?? 7.80), visibility: sensor.visibility,
      warningThreshold: sensor.warningThreshold == null ? "" : String(sensor.warningThreshold),
      criticalThreshold: sensor.criticalThreshold == null ? "" : String(sensor.criticalThreshold),
    });
  }

  async function cycleVisibility(sensor: Sensor) {
    const order: Sensor["visibility"][] = ["PRIVATE", "PUBLIC_SUMMARY", "PUBLIC"];
    const next = order[(order.indexOf(sensor.visibility) + 1) % order.length];
    try {
      await monitoringApi.updateSensor(token, damId, { ...sensor, visibility: next, exposeExactLocation: next === "PUBLIC" });
      await loadDam(token, damId);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to update visibility");
    }
  }

  async function selectHistory(sensor: Sensor) {
    setHistorySensor(sensor);
    try {
      setHistory((await monitoringApi.history(token, damId, sensor.id)).reverse());
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to load history");
    }
  }

  async function createGate() {
    const count = gates.length + 1;
    try {
      await monitoringApi.createGate(token, damId, {
        code: `GATE-${Date.now()}`, name: `Spillway Gate ${count}`, status: "UNKNOWN",
        openingPercent: null, visibility: "PRIVATE", location: null,
      });
      await loadDam(token, damId);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to create gate");
    }
  }

  async function cycleGate(gate: Gate) {
    const states: Gate["status"][] = ["UNKNOWN", "CLOSED", "PARTIAL", "OPEN", "MAINTENANCE"];
    const status = states[(states.indexOf(gate.status) + 1) % states.length];
    const openingPercent = status === "OPEN" ? 100 : status === "CLOSED" ? 0 : status === "PARTIAL" ? 50 : null;
    try {
      await monitoringApi.updateGate(token, damId, { ...gate, status, openingPercent });
      await loadDam(token, damId);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to update gate");
    }
  }

  async function createDevice() {
    try {
      const device = await monitoringApi.createDevice(token, damId, `Gateway ${devices.length + 1}`);
      setSecret(device);
      await loadDam(token, damId);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to create device");
    }
  }

  async function deviceAction(device: IotDevice, action: "rotate-key" | "revoke" | "disable" | "enable") {
    try {
      const result = await monitoringApi.deviceAction(token, damId, device.id, action);
      if (result?.credentials) setSecret(result);
      await loadDam(token, damId);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Device action failed");
    }
  }

  async function assignAvailableSensor(device: IotDevice) {
    const assigned = new Set(devices.flatMap((item) => item.sensorIds));
    const sensor = sensors.find((item) => !assigned.has(item.id));
    if (!sensor) return setError("Every sensor is already assigned to a device.");
    try {
      await monitoringApi.assignSensor(token, damId, device.id, sensor.id);
      await loadDam(token, damId);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to assign sensor");
    }
  }

  async function changeDamState(state: Dam["operationalState"]) {
    if (!selectedDam) return;
    try {
      await monitoringApi.updateDamState(token, selectedDam.id, state);
      const refreshed = await monitoringApi.dams(token);
      setDams(refreshed);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to update dam state");
    }
  }

  if (!token) return <Login email={email} password={password} error={error} setEmail={setEmail} setPassword={setPassword} submit={submitLogin} />;

  return (
    <main className="min-h-screen bg-[#071916] text-slate-100">
      <header className="border-b border-white/10 bg-[#0a211d]/95 px-5 py-4 backdrop-blur lg:px-10">
        <div className="mx-auto flex max-w-[1500px] flex-wrap items-center justify-between gap-4">
          <div><p className="text-xs font-bold uppercase tracking-[.3em] text-emerald-400">DDAS Control</p><h1 className="text-2xl font-semibold">Dam monitoring</h1></div>
          <div className="flex items-center gap-3">
            <select value={damId} onChange={(event) => setDamId(event.target.value)} className="rounded-xl border border-white/15 bg-white/10 px-4 py-2 text-sm">
              {dams.map((dam) => <option className="text-slate-900" key={dam.id} value={dam.id}>{dam.name}</option>)}
            </select>
            <span className="rounded-full bg-emerald-400/15 px-3 py-1 text-xs text-emerald-300">● {streamState}</span>
            <button onClick={logout} className="rounded-xl border border-white/15 px-4 py-2 text-sm hover:bg-white/10">Sign out</button>
          </div>
        </div>
      </header>

      <div className="mx-auto grid max-w-[1500px] gap-6 px-5 py-6 lg:px-10">
        {error && <button onClick={() => setError("")} className="rounded-xl border border-red-400/30 bg-red-400/10 p-3 text-left text-sm text-red-200">{error} — click to dismiss</button>}
        <section className="grid gap-4 lg:grid-cols-[1.4fr_1fr]">
          <div className="rounded-3xl border border-white/10 bg-white/[.055] p-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div><p className="text-sm text-slate-400">Operational overview</p><h2 className="mt-1 text-3xl font-semibold">{selectedDam?.name ?? "Select a dam"}</h2><p className="mt-2 font-mono text-xs text-slate-500">{selectedDam?.code}</p></div>
              {selectedDam && <span className={`rounded-full px-4 py-2 text-sm font-bold ${stateColors[selectedDam.operationalState]}`}>{selectedDam.operationalState}</span>}
            </div>
            <div className="mt-7 grid grid-cols-2 gap-3 sm:grid-cols-4">
              {(["NORMAL", "WATCH", "WARNING", "EMERGENCY"] as const).map((state) => <button key={state} onClick={() => changeDamState(state)} className="rounded-xl border border-white/10 px-3 py-2 text-xs font-semibold hover:border-emerald-400/60 hover:bg-emerald-400/10">Set {state}</button>)}
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Metric label="Sensors" value={sensors.length} detail={`${latest.filter((reading) => reading.status === "ACTIVE").length} online`} />
            <Metric label="IoT devices" value={devices.length} detail={`${devices.filter((device) => device.status === "ACTIVE").length} active`} />
            <Metric label="Gates" value={gates.length} detail={`${gates.filter((gate) => gate.status === "OPEN").length} open`} />
            <Metric label="Data quality" value={latest.filter((reading) => reading.quality === "GOOD").length} detail="good readings" />
          </div>
        </section>

        <section>
          <div className="mb-3 flex items-end justify-between"><div><p className="text-sm text-slate-400">Latest telemetry</p><h2 className="text-xl font-semibold">Live readings</h2></div><span className="text-xs text-slate-500">MQTT → DB → SSE</span></div>
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {latest.map((reading) => <TelemetryCard key={reading.sensorId} reading={reading} />)}
            {!latest.length && <Empty text="No sensors are registered for this dam." />}
          </div>
        </section>

        <section className="grid gap-6 xl:grid-cols-[1.45fr_1fr]">
          <div className="overflow-hidden rounded-3xl border border-white/10 bg-white/[.055]">
            <div className="flex items-center justify-between p-5"><div><p className="text-sm text-slate-400">Asset registry</p><h2 className="text-xl font-semibold">Sensors</h2></div><span className="text-xs text-slate-500">Click visibility to change it</span></div>
            <div className="overflow-x-auto"><table className="w-full text-left text-sm"><thead className="border-y border-white/10 bg-black/10 text-xs uppercase tracking-wider text-slate-400"><tr><th className="p-4">Sensor</th><th className="p-4">Type</th><th className="p-4">Status</th><th className="p-4">Visibility</th><th className="p-4">Actions</th></tr></thead><tbody>{sensors.map((sensor) => <tr key={sensor.id} className="border-b border-white/[.07]"><td className="p-4"><strong>{sensor.name}</strong><br/><span className="font-mono text-xs text-slate-500">{sensor.code}</span></td><td className="p-4 text-slate-300">{sensor.sensorType}<br/><span className="text-xs text-slate-500">{sensor.unit}</span></td><td className="p-4"><Status value={sensor.status} /></td><td className="p-4"><button onClick={() => cycleVisibility(sensor)} className="rounded-lg border border-white/10 px-2 py-1 text-xs hover:bg-white/10">{sensor.visibility}</button></td><td className="p-4"><div className="flex gap-3"><button onClick={() => editSensor(sensor)} className="text-emerald-300 hover:underline">Edit</button><button onClick={() => selectHistory(sensor)} className="text-emerald-300 hover:underline">Chart</button></div></td></tr>)}</tbody></table></div>
          </div>
          <form onSubmit={submitSensor} className="rounded-3xl border border-white/10 bg-white/[.055] p-5">
            <p className="text-sm text-slate-400">Map registration</p><div className="flex items-center justify-between"><h2 className="text-xl font-semibold">{editingSensor ? "Edit sensor" : "Add sensor"}</h2>{editingSensor && <button type="button" onClick={() => setEditingSensor(null)} className="text-xs text-slate-400">Cancel</button>}</div>
            <div className="mt-4 grid grid-cols-2 gap-3"><Input label="Code" value={form.code} set={(value) => setForm({ ...form, code: value })} required/><Input label="Name" value={form.name} set={(value) => setForm({ ...form, name: value })} required/><Input label="Longitude" value={form.longitude} set={(value) => setForm({ ...form, longitude: value })} required/><Input label="Latitude" value={form.latitude} set={(value) => setForm({ ...form, latitude: value })} required/><Input label="Warning" value={form.warningThreshold} set={(value) => setForm({ ...form, warningThreshold: value })}/><Input label="Critical" value={form.criticalThreshold} set={(value) => setForm({ ...form, criticalThreshold: value })}/></div>
            <label className="mt-3 block text-xs text-slate-400">Public visibility<select value={form.visibility} onChange={(event) => setForm({ ...form, visibility: event.target.value as Sensor["visibility"] })} className="mt-1 w-full rounded-xl border border-white/10 bg-[#102c27] p-3 text-sm text-white"><option>PRIVATE</option><option>PUBLIC_SUMMARY</option><option>PUBLIC</option></select></label>
            <button className="mt-4 w-full rounded-xl bg-emerald-400 px-4 py-3 font-semibold text-emerald-950 hover:bg-emerald-300">{editingSensor ? "Save sensor" : "Register sensor"}</button>
          </form>
        </section>

        <section className="rounded-3xl border border-white/10 bg-white/[.055] p-3"><SensorMap sensors={sensors} onPick={(longitude, latitude) => setForm({ ...form, longitude: longitude.toFixed(6), latitude: latitude.toFixed(6) })}/><p className="px-3 pb-1 pt-3 text-xs text-slate-400">Click the map to populate the new sensor coordinates.</p></section>

        {historySensor && <section className="rounded-3xl border border-white/10 bg-white/[.055] p-6"><div className="flex justify-between"><div><p className="text-sm text-slate-400">Last 100 readings</p><h2 className="text-xl font-semibold">{historySensor.name} history</h2></div><button onClick={() => setHistorySensor(null)}>Close</button></div><HistoryChart readings={history} unit={historySensor.unit}/></section>}

        <section className="grid gap-6 lg:grid-cols-2">
          <AssetPanel title="Gates" action="Add gate" onAction={createGate}>{gates.map((gate) => <div key={gate.id} className="border-b border-white/[.07] py-3"><AssetRow name={gate.name} meta={`${gate.status} · ${gate.openingPercent ?? 0}% open`} /><button onClick={() => cycleGate(gate)} className="mt-2 text-xs text-emerald-300">Advance gate state</button></div>)}</AssetPanel>
          <AssetPanel title="IoT devices" action="Provision device" onAction={createDevice}>{devices.map((device) => <div key={device.id} className="border-b border-white/[.07] py-3"><AssetRow name={device.name} meta={`${device.status} · ${device.sensorIds.length} sensors`} /><div className="mt-2 flex flex-wrap gap-3 text-xs text-emerald-300">{device.status === "ACTIVE" && <><button onClick={() => assignAvailableSensor(device)}>Assign sensor</button><button onClick={() => deviceAction(device, "rotate-key")}>Rotate key</button><button onClick={() => deviceAction(device, "disable")}>Disable</button><button onClick={() => deviceAction(device, "revoke")} className="text-red-300">Revoke</button></>}{device.status === "DISABLED" && <button onClick={() => deviceAction(device, "enable")}>Enable</button>}</div></div>)}</AssetPanel>
        </section>
      </div>

      {secret?.credentials && <div className="fixed inset-0 z-50 grid place-items-center bg-black/75 p-5"><div className="max-w-xl rounded-3xl border border-amber-300/30 bg-[#10241f] p-7 shadow-2xl"><p className="text-sm font-bold text-amber-300">Shown once</p><h2 className="mt-1 text-2xl font-semibold">Save the device credential now</h2><p className="mt-4 break-all rounded-xl bg-black/30 p-4 font-mono text-sm">{secret.credentials.deviceKey}</p><p className="mt-3 text-sm text-slate-400">Topic: {secret.mqtt?.topic}</p><button onClick={() => setSecret(null)} className="mt-6 w-full rounded-xl bg-emerald-400 p-3 font-semibold text-emerald-950">I saved it securely</button></div></div>}
    </main>
  );
}

function Login({ email, password, error, setEmail, setPassword, submit }: { email: string; password: string; error: string; setEmail: (value: string) => void; setPassword: (value: string) => void; submit: (event: FormEvent) => void }) {
  return <main className="grid min-h-screen place-items-center bg-[#071916] p-5 text-white"><form onSubmit={submit} className="w-full max-w-md rounded-3xl border border-white/10 bg-white/[.06] p-8 shadow-2xl"><p className="text-xs font-bold uppercase tracking-[.3em] text-emerald-400">DDAS Control</p><h1 className="mt-2 text-3xl font-semibold">Operator sign in</h1><p className="mt-2 text-sm text-slate-400">Use your dam staff account to open live monitoring.</p><div className="mt-7 space-y-4"><Input label="Email" value={email} set={setEmail} required/><Input label="Password" value={password} set={setPassword} type="password" required/></div>{error && <p className="mt-4 text-sm text-red-300">{error}</p>}<button className="mt-6 w-full rounded-xl bg-emerald-400 p-3 font-semibold text-emerald-950">Sign in</button></form></main>;
}

function Input({ label, value, set, type = "text", required = false }: { label: string; value: string; set: (value: string) => void; type?: string; required?: boolean }) {
  return <label className="block text-xs text-slate-400">{label}<input type={type} value={value} onChange={(event) => set(event.target.value)} required={required} className="mt-1 w-full rounded-xl border border-white/10 bg-white/[.06] p-3 text-sm text-white outline-none focus:border-emerald-400/60"/></label>;
}

function Metric({ label, value, detail }: { label: string; value: number; detail: string }) { return <div className="rounded-2xl border border-white/10 bg-white/[.055] p-4"><p className="text-xs uppercase tracking-wider text-slate-500">{label}</p><p className="mt-1 text-3xl font-semibold">{value}</p><p className="text-xs text-slate-400">{detail}</p></div>; }
function Status({ value }: { value: string }) { return <span className={`rounded-full px-2 py-1 text-xs ${value === "ACTIVE" || value === "GOOD" ? "bg-emerald-400/15 text-emerald-300" : "bg-amber-400/15 text-amber-300"}`}>{value}</span>; }
function Empty({ text }: { text: string }) { return <div className="col-span-full rounded-2xl border border-dashed border-white/15 p-8 text-center text-sm text-slate-500">{text}</div>; }
function TelemetryCard({ reading }: { reading: LatestReading }) { return <article className="rounded-2xl border border-white/10 bg-gradient-to-br from-white/[.08] to-white/[.035] p-5"><div className="flex justify-between gap-3"><div><p className="text-sm text-slate-400">{reading.sensorName}</p><p className="mt-2 text-3xl font-semibold">{reading.value ?? "—"} <span className="text-base font-normal text-slate-400">{reading.unit}</span></p></div><Status value={reading.status}/></div><div className="mt-5 flex justify-between text-xs text-slate-500"><span>{reading.quality ?? "No data"}</span><span>{reading.measuredAt ? new Date(reading.measuredAt).toLocaleTimeString() : "Awaiting reading"}</span></div></article>; }
function AssetPanel({ title, action, onAction, children }: { title: string; action: string; onAction: () => void; children: React.ReactNode }) { return <section className="rounded-3xl border border-white/10 bg-white/[.055] p-5"><div className="flex items-center justify-between"><h2 className="text-xl font-semibold">{title}</h2><button onClick={onAction} className="rounded-xl bg-emerald-400/15 px-3 py-2 text-xs font-semibold text-emerald-300 hover:bg-emerald-400/25">{action}</button></div><div className="mt-3">{children}</div></section>; }
function AssetRow({ name, meta }: { name: string; meta: string }) { return <div className="flex items-center justify-between gap-4"><strong className="text-sm">{name}</strong><span className="text-xs text-slate-400">{meta}</span></div>; }

function HistoryChart({ readings, unit }: { readings: Reading[]; unit: string }) {
  const points = useMemo(() => {
    if (!readings.length) return "";
    const values = readings.map((item) => Number(item.value));
    const min = Math.min(...values), max = Math.max(...values), range = max - min || 1;
    return values.map((value, index) => `${(index / Math.max(1, values.length - 1)) * 100},${90 - ((value - min) / range) * 75}`).join(" ");
  }, [readings]);
  if (!readings.length) return <Empty text="No historical readings yet."/>;
  return <div className="mt-5"><svg viewBox="0 0 100 100" className="h-52 w-full overflow-visible" preserveAspectRatio="none"><path d="M0 90 H100" stroke="rgba(255,255,255,.12)"/><polyline points={points} fill="none" stroke="#34d399" strokeWidth="2" vectorEffect="non-scaling-stroke"/></svg><div className="flex justify-between text-xs text-slate-500"><span>{new Date(readings[0].measuredAt).toLocaleString()}</span><span>{readings.at(-1)?.value} {unit}</span><span>{new Date(readings.at(-1)!.measuredAt).toLocaleString()}</span></div></div>;
}
