package org.example.llm.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ClientRegistry {
    public final Map<String, Client> clients = new HashMap<>();
    private final OpenRouterClient openRouterClient;
    
    @PostConstruct
    void init(){
        clients.put("openrouter", openRouterClient);
    }

    /**
     * Returns the {@link Client} for the given provider. Access the client only through this
     * method, never directly via the {@code clients} field: Beans in {@code org.example..*} are
     * CGLIB-proxied by the {@code LoggingAspect}. For classes without a no-arg constructor the
     * proxy does not run field initializers, so direct access to a public field yields {@code null}.
     */
    public Client getClient(String provider) {
        return clients.get(provider);
    }

    public Set<String> providerCodes() {
        return Set.copyOf(clients.keySet());
    }
}
