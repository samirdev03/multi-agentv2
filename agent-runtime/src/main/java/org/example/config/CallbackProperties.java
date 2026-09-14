package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "callback")
public class CallbackProperties {

    private List<URI> allowedBaseUrls = List.of();
    private String connectorToken;

    public CallbackProperties() {
    }

    public CallbackProperties(List<URI> allowedBaseUrls, String connectorToken) {
        setAllowedBaseUrls(allowedBaseUrls);
        setConnectorToken(connectorToken);
    }

    public List<URI> getAllowedBaseUrls() {
        return allowedBaseUrls;
    }

    public void setAllowedBaseUrls(List<URI> allowedBaseUrls) {
        this.allowedBaseUrls = allowedBaseUrls == null
                ? List.of()
                : List.copyOf(allowedBaseUrls);
    }

    public String getConnectorToken() {
        return connectorToken;
    }

    public void setConnectorToken(String connectorToken) {
        Assert.hasText(connectorToken, "callback.connector-token must not be blank");
        this.connectorToken = connectorToken;
    }
}
