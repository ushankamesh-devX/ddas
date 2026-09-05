export type Dam = {
  id: string;
  code: string;
  name: string;
  operationalState: "NORMAL" | "WATCH" | "WARNING" | "EMERGENCY";
  active: boolean;
};

export type GeoPoint = { type: "Point"; coordinates: [number, number] };

export type Sensor = {
  id: string;
  damId: string;
  code: string;
  name: string;
  sensorType: string;
  unit: string;
  location: GeoPoint | null;
  visibility: "PRIVATE" | "PUBLIC_SUMMARY" | "PUBLIC";
  exposeExactLocation: boolean;
  warningThreshold: number | null;
  criticalThreshold: number | null;
  thresholdDirection: "HIGH" | "LOW";
  status: "ACTIVE" | "DISABLED" | "MAINTENANCE" | "OFFLINE";
  lastSeenAt: string | null;
};

export type LatestReading = {
  sensorId: string;
  sensorName: string;
  sensorType: string;
  unit: string;
  status: Sensor["status"];
  value: number | null;
  quality: string | null;
  measuredAt: string | null;
};

export type Reading = {
  id: number;
  sensorId: string;
  measuredAt: string;
  receivedAt: string;
  value: number;
  quality: string;
  messageId: string | null;
};

export type Gate = {
  id: string;
  damId: string;
  code: string;
  name: string;
  location: GeoPoint | null;
  status: "OPEN" | "CLOSED" | "PARTIAL" | "UNKNOWN" | "MAINTENANCE";
  openingPercent: number | null;
  visibility: Sensor["visibility"];
};

export type IotDevice = {
  id: string;
  damId: string;
  deviceId: string;
  name: string;
  deviceType: string;
  authMethod: string;
  status: "ACTIVE" | "DISABLED" | "REVOKED";
  lastConnectedAt: string | null;
  sensorIds: string[];
  credentials?: { deviceId: string; deviceKey: string; shownOnce: boolean } | null;
  mqtt?: { host: string; port: number; topic: string } | null;
};
