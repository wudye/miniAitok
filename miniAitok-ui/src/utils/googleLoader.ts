import { setOptions, importLibrary } from "@googlemaps/js-api-loader";

setOptions({
  apiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY as string,
  version: "weekly", // 或具体版本，例如 '3.57'
  // libraries will be loaded via importLibrary
} as any);

// 导入核心 maps 和 places 库，返回一个 Promise
export const loadGoogleMaps = () => importLibrary(["maps", "places"]);