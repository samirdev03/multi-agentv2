package org.example.tools.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "tools.http")
public class HttpToolsProperties {
    private long maxBytes = 1_000_000;
    private int timeoutSeconds = 10;
}
