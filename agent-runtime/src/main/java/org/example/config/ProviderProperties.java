package org.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "provider")
public class ProviderProperties {
    private Map<String, ProviderPropertiesData> providers = new HashMap<>();

    @Data
    public static class ProviderPropertiesData {
        private String provider;
        private String baseUrl;
        private String apiKey;
    }
}
