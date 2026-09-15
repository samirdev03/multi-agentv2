package org.example.tools.service;

import lombok.RequiredArgsConstructor;
import org.example.tools.config.HttpToolsProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.InetAddress;
import java.net.URI;

@Service
@RequiredArgsConstructor
public class HttpContentService {
    private final HttpToolsProperties properties;
    private final RestClient.Builder restClientBuilder;

    public String fetch(String url) {
        URI uri;
        try { uri = URI.create(url); } catch (IllegalArgumentException e) { throw new IllegalArgumentException("Ungueltige URL", e); }
        if (!uri.isAbsolute() || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalArgumentException("Nur HTTP/HTTPS URLs sind erlaubt");
        }
        try {
            InetAddress address = InetAddress.getByName(uri.getHost());
            if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || address.isMulticastAddress()) {
                throw new IllegalArgumentException("Private oder lokale Ziele sind nicht erlaubt");
            }
        } catch (java.net.UnknownHostException e) { throw new IllegalArgumentException("Host konnte nicht aufgeloest werden", e); }
        String body = restClientBuilder.build().get().uri(uri).accept(MediaType.TEXT_HTML, MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON)
                .retrieve().body(String.class);
        if (body == null) return "";
        if (body.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > properties.getMaxBytes()) {
            throw new IllegalArgumentException("Antwort ueberschreitet das konfigurierte Groessenlimit");
        }
        return body;
    }
}
