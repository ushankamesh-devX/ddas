const modules = [
  {
    name: "Dam monitoring",
    description: "Live dam status, water levels, and operational overview.",
  },
  {
    name: "Sensors and telemetry",
    description: "IoT device health, readings, and data-quality checks.",
  },
  {
    name: "Alerts",
    description: "Emergency alerts, delivery status, and recipient coverage.",
  },
  {
    name: "Evacuation",
    description: "Risk zones, routes, safe locations, and response coordination.",
  },
  {
    name: "Community reports",
    description: "Review citizen reports and supporting field evidence.",
  },
  {
    name: "News",
    description: "Publish public safety updates and operational notices.",
  },
];

export default function Home() {
  const apiUrl =
    process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top_left,#d8efe7_0,#f4f7f6_42%,#eef2f1_100%)] px-6 py-10 text-slate-900 sm:px-10 lg:px-16">
      <div className="mx-auto max-w-7xl">
        <header className="flex flex-col gap-5 border-b border-emerald-950/15 pb-8 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="mb-3 text-sm font-bold uppercase tracking-[0.24em] text-emerald-700">
              DDAS
            </p>
            <h1 className="text-4xl font-semibold tracking-tight sm:text-5xl">
              Operations console
            </h1>
            <p className="mt-4 max-w-2xl text-base leading-7 text-slate-600">
              Administrative workspace for dam monitoring, telemetry, alerts,
              and emergency response.
            </p>
          </div>
          <div className="w-fit rounded-full border border-emerald-800/15 bg-white/75 px-4 py-2 text-sm text-slate-600 shadow-sm">
            Initial frontend scaffold
          </div>
        </header>

        <section className="grid gap-4 py-10 sm:grid-cols-2 lg:grid-cols-3">
          {modules.map((module, index) => (
            <article
              key={module.name}
              className="group rounded-2xl border border-slate-900/10 bg-white/80 p-6 shadow-sm backdrop-blur transition hover:-translate-y-0.5 hover:border-emerald-700/30 hover:shadow-md"
            >
              <div className="mb-8 flex items-center justify-between">
                <span className="flex size-9 items-center justify-center rounded-full bg-emerald-900 text-sm font-semibold text-white">
                  {String(index + 1).padStart(2, "0")}
                </span>
                <span className="text-xs font-semibold uppercase tracking-wider text-emerald-700">
                  Planned
                </span>
              </div>
              <h2 className="text-xl font-semibold tracking-tight">
                {module.name}
              </h2>
              <p className="mt-3 leading-6 text-slate-600">
                {module.description}
              </p>
            </article>
          ))}
        </section>

        <footer className="flex flex-col gap-2 border-t border-emerald-950/15 pt-6 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between">
          <span>Next.js · TypeScript · Tailwind CSS</span>
          <span>
            API: <code className="font-mono text-slate-700">{apiUrl}</code>
          </span>
        </footer>
      </div>
    </main>
  );
}
