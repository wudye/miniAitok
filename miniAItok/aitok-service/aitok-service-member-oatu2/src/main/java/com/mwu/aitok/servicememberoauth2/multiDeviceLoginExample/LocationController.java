package com.mwu.aitok.servicememberoauth2.multiDeviceLoginExample;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1")
public class LocationController {
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/location")
    public ResponseEntity<String> reverseGeocode(@RequestBody LocationRequest req) {
        if (req == null || req.lat == null || req.lon == null) {
            return ResponseEntity.badRequest().body("");
        }
        try {
            String url = String.format(
                    "https://nominatim.openstreetmap.org/reverse?lat=%s&lon=%s&format=json&accept-language=zh",
                    req.lat, req.lon);
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "YourAppName/1.0 (contact@example.com)");
            HttpEntity<Void> ent = new HttpEntity<>(headers);
            ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, ent, String.class);
            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                JsonNode node = mapper.readTree(resp.getBody());
                String display = node.path("display_name").asText("");
                return ResponseEntity.ok(display);
            }
        } catch (Exception ignored) { }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("");
    }

    static class LocationRequest {
        public Double lat;
        public Double lon;
    }
}
