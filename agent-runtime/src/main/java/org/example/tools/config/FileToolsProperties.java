package org.example.tools.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "tools.files")
public class FileToolsProperties {

    private String path;

    private long maxSizeBytes;
}
