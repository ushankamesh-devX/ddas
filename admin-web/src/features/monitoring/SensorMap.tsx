"use client";

import { useEffect, useRef } from "react";
import { Map as MapLibreMap, Marker, NavigationControl, Popup } from "maplibre-gl";
import type { Sensor } from "./types";

export function SensorMap({ sensors, onPick }: { sensors: Sensor[]; onPick?: (longitude: number, latitude: number) => void }) {
  const container = useRef<HTMLDivElement>(null);
  const map = useRef<MapLibreMap | null>(null);
  const markers = useRef<Marker[]>([]);
  const pickHandler = useRef(onPick);

  useEffect(() => {
    pickHandler.current = onPick;
  }, [onPick]);

  useEffect(() => {
    if (!container.current || map.current) return;
    map.current = new MapLibreMap({
      container: container.current,
      style: "https://demotiles.maplibre.org/style.json",
      center: [80.7, 7.8],
      zoom: 6.5,
    });
    map.current.addControl(new NavigationControl(), "top-right");
    map.current.on("click", (event) => pickHandler.current?.(event.lngLat.lng, event.lngLat.lat));
    return () => {
      map.current?.remove();
      map.current = null;
    };
  }, []);

  useEffect(() => {
    markers.current.forEach((marker) => marker.remove());
    markers.current = sensors.flatMap((sensor) => {
      if (!sensor.location || !map.current) return [];
      const marker = new Marker({ color: sensor.status === "ACTIVE" ? "#10b981" : "#f59e0b" })
        .setLngLat(sensor.location.coordinates)
        .setPopup(new Popup({ offset: 20 }).setText(`${sensor.name} · ${sensor.sensorType}`))
        .addTo(map.current);
      return [marker];
    });
  }, [sensors]);

  return <div ref={container} className="h-[360px] w-full overflow-hidden rounded-2xl" aria-label="Sensor placement map" />;
}
