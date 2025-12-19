import React, { useEffect, useRef } from "react";
import { loadGoogleMaps } from "./googleLoader";

export function Map({ center = { lat: 39.9042, lng: 116.4074 }, zoom = 11 }:{
  center?: { lat:number; lng:number };
  zoom?: number;
}) {
  const ref = useRef<HTMLDivElement | null>(null);
  // Avoid referencing the global 'google' namespace directly to prevent TypeScript errors when @types/google.maps isn't installed.
  // For stronger typing, install `@types/google.maps` and add `/// <reference types="google.maps" />` at the top of this file.
  const mapRef = useRef<any>(null);

  useEffect(() => {
    let cancelled = false;

    loadGoogleMaps()
      .then(() => {
        if (cancelled) return;
        if (!ref.current) return;
        // `google` 全局已经可用，并由 `@types/google.maps` 提供类型
        const g = (window as any).google;
        if (!g) {
          console.error("Google Maps object is not available on window");
          return;
        }
        mapRef.current = new g.maps.Map(ref.current, {
          center,
          zoom,
        });
      })
      .catch((err: unknown) => {
        console.error("Google Maps 加载失败", err);
      });

    return () => {
      cancelled = true;
      // 可选：如果需要销毁地图实例
      // mapRef.current = null;
    };
  }, [center.lat, center.lng, zoom]);

  return <div ref={ref} style={{ width: "100%", height: "400px" }} />;
}