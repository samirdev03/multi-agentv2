package org.example.llm.client;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClientRegistry {
    public final Map<String, Client> clients = new HashMap<>();
    private OpenRouterClient openRouterClient;
    
    @PostConstruct
    void init(){
        clients.put("openrouter", openRouterClient);
    }
}
