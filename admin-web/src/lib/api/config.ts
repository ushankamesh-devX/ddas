const DEFAULT_LOCAL_API_BASE_URL = "http://localhost:8080";

export const apiBaseUrl = (
  process.env.NEXT_PUBLIC_API_BASE_URL ?? DEFAULT_LOCAL_API_BASE_URL
).replace(/\/$/, "");
