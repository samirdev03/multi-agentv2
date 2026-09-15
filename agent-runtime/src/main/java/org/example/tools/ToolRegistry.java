package org.example.tools;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Set;

@Component
public class ToolRegistry {

    private final Map<String, AgentTool> tools;

    public ToolRegistry(List<AgentTool> agentTools) {

        this.tools = agentTools.stream()
                .collect(Collectors.toMap(
                        AgentTool::getId,
                        Function.identity()
                ));
    }

    public AgentTool get(String id) {

        AgentTool tool = tools.get(id);

        if (tool == null) {
            throw new IllegalArgumentException(
                    "Unbekanntes Tool: " + id
            );
        }

        return tool;
    }

    public Set<String> toolIds() {
        return Set.copyOf(tools.keySet());
    }
}
