package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "callback")
public class CallbackProperties {

    private List<URI> allowedBaseUrls = List.of();

    public CallbackProperties() {
    }

    public CallbackProperties(List<URI> allowedBaseUrls) {
        setAllowedBaseUrls(allowedBaseUrls);
    }

    public List<URI> getAllowedBaseUrls() {
        return allowedBaseUrls;
    }

    public void setAllowedBaseUrls(List<URI> allowedBaseUrls) {
        this.allowedBaseUrls = allowedBaseUrls == null
                ? List.of()
                : List.copyOf(allowedBaseUrls);
    }
}
