package org.example.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CredentialRegistry {

    private final ProviderProperties props;
    private final Map<String, Credential> credentials = new HashMap<>();

    @PostConstruct
    public void init() {
        props.getProviders().forEach((provider, config) -> {

            Credential credential = new Credential(
                    provider,
                    config.getBaseUrl(),
                    config.getApiKey()
            );
            credentials.put(provider, credential);
        });
    }


    public void register(Credential credential) {
        credentials.put(credential.provider(), credential);
    }

    public Optional<Credential> get(String provider) {
        return Optional.ofNullable(credentials.get(provider));
    }

    public Credential getRequired(String provider) {
        Credential credential = credentials.get(provider);

        if (credential == null) {
            throw new IllegalArgumentException(
                    "Keine Credentials für Provider gefunden: " + provider
            );
        }

        return credential;
    }

    public boolean contains(String provider) {
        return credentials.containsKey(provider);
    }
}
