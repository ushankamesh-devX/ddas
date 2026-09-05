import { apiBaseUrl } from "@/lib/api/config";
import type { Dam, Gate, IotDevice, LatestReading, Reading, Sensor } from "./types";

type ApiError = { code?: string; message?: string };

async function request<T>(path: string, token?: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...init,
    headers: {
      Accept: "application/json",
      ...(init?.body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init?.headers,
    },
  });
  if (!response.ok) {
    const error = (await response.json().catch(() => ({}))) as ApiError;
    throw new Error(error.message ?? `Request failed (${response.status})`);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export async function login(email: string, password: string): Promise<string> {
  const result = await request<{ accessToken: string }>("/api/v1/auth/login", undefined, {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
  return result.accessToken;
}

export const monitoringApi = {
  dams: (token: string) => request<Dam[]>("/api/v1/dams", token),
  sensors: (token: string, damId: string) => request<Sensor[]>(`/api/v1/dams/${damId}/sensors`, token),
  latest: (token: string, damId: string) => request<LatestReading[]>(`/api/v1/dams/${damId}/telemetry/latest`, token),
  history: (token: string, damId: string, sensorId: string) =>
    request<Reading[]>(`/api/v1/dams/${damId}/sensors/${sensorId}/readings?size=100`, token),
  gates: (token: string, damId: string) => request<Gate[]>(`/api/v1/dams/${damId}/gates`, token),
  devices: (token: string, damId: string) => request<IotDevice[]>(`/api/v1/dams/${damId}/iot-devices`, token),
  createSensor: (token: string, damId: string, body: object) =>
    request<Sensor>(`/api/v1/dams/${damId}/sensors`, token, { method: "POST", body: JSON.stringify(body) }),
  updateSensor: (token: string, damId: string, sensor: Sensor) =>
    request<Sensor>(`/api/v1/dams/${damId}/sensors/${sensor.id}`, token, {
      method: "PUT",
      body: JSON.stringify({
        name: sensor.name,
        sensorType: sensor.sensorType,
        unit: sensor.unit,
        location: sensor.location,
        visibility: sensor.visibility,
        exposeExactLocation: sensor.exposeExactLocation,
        warningThreshold: sensor.warningThreshold,
        criticalThreshold: sensor.criticalThreshold,
        thresholdDirection: sensor.thresholdDirection,
        status: sensor.status,
      }),
    }),
  createGate: (token: string, damId: string, body: object) =>
    request<Gate>(`/api/v1/dams/${damId}/gates`, token, { method: "POST", body: JSON.stringify(body) }),
  updateGate: (token: string, damId: string, gate: Gate) =>
    request<Gate>(`/api/v1/dams/${damId}/gates/${gate.id}`, token, {
      method: "PUT",
      body: JSON.stringify({ code: gate.code, name: gate.name, location: gate.location, status: gate.status,
        openingPercent: gate.openingPercent, visibility: gate.visibility }),
    }),
  createDevice: (token: string, damId: string, name: string) =>
    request<IotDevice>(`/api/v1/dams/${damId}/iot-devices`, token, {
      method: "POST",
      body: JSON.stringify({ name, deviceType: "GATEWAY", authMethod: "DEVICE_KEY" }),
    }),
  deviceAction: (token: string, damId: string, deviceId: string, action: "rotate-key" | "revoke" | "disable" | "enable") =>
    request<IotDevice | undefined>(`/api/v1/dams/${damId}/iot-devices/${deviceId}/${action}`, token, {
      method: "POST",
      ...(action === "revoke" || action === "disable" ? { body: JSON.stringify({ reason: "Admin console action" }) } : {}),
    }),
  assignSensor: (token: string, damId: string, deviceId: string, sensorId: string) =>
    request<void>(`/api/v1/dams/${damId}/iot-devices/${deviceId}/sensors/${sensorId}`, token, { method: "POST" }),
  updateDamState: (token: string, damId: string, state: Dam["operationalState"]) =>
    request<Dam>(`/api/v1/dams/${damId}/state`, token, {
      method: "PATCH",
      body: JSON.stringify({ state, publicStatusMessage: `Dam status is ${state.toLowerCase()}.` }),
    }),
};

export async function subscribeToTelemetry(
  token: string,
  damId: string,
  onReading: (reading: LatestReading) => void,
  signal: AbortSignal,
) {
  const response = await fetch(`${apiBaseUrl}/api/v1/dams/${damId}/telemetry/stream`, {
    headers: { Authorization: `Bearer ${token}`, Accept: "text/event-stream" },
    signal,
  });
  if (!response.ok || !response.body) throw new Error("Unable to open telemetry stream");
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  while (!signal.aborted) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const events = buffer.split("\n\n");
    buffer = events.pop() ?? "";
    for (const event of events) {
      const eventName = event.split("\n").find((line) => line.startsWith("event:"))?.slice(6).trim();
      const data = event.split("\n").find((line) => line.startsWith("data:"))?.slice(5).trim();
      if (eventName === "telemetry" && data) onReading(JSON.parse(data) as LatestReading);
    }
  }
}
