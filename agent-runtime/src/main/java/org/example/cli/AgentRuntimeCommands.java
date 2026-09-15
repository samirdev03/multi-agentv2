package org.example.cli;

import org.example.agent.AgentEntity;
import org.example.llm.client.ClientRegistry;
import org.example.tools.ToolRegistry;
import org.example.web.dto.ChannelType;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Arrays;
import java.util.stream.Collectors;

@ShellComponent
public class AgentRuntimeCommands {
    private final AgentManagementService service;
    private final ClientRegistry clients;
    private final ToolRegistry tools;

    public AgentRuntimeCommands(AgentManagementService service, ClientRegistry clients, ToolRegistry tools) {
        this.service = service; this.clients = clients; this.tools = tools;
    }

    @ShellMethod(key = "agent-list", value = "Listet alle Agenten mit ihrem Channel.")
    public String list() {
        var agents = service.list();
        return agents.isEmpty() ? "Keine Agenten." : agents.stream().map(this::format).collect(Collectors.joining(System.lineSeparator()));
    }

    @ShellMethod(key = "agent-create", value = "Erzeugt einen Agenten und seinen Channel.")
    public String create(@ShellOption("--name") String name, @ShellOption("--prompt") String prompt,
                         @ShellOption("--provider") String provider, @ShellOption("--model") String model,
                         @ShellOption("--channel-type") String type, @ShellOption("--channel-id") String channelId,
                         @ShellOption(defaultValue = "", value = "--response-url") String responseUrl,
                         @ShellOption(defaultValue = "", value = "--tools") String toolIds) {
        AgentEntity agent = service.create(name, prompt, provider, model, parseType(type), channelId, responseUrl, toolIds);
        return "Agent erstellt: " + agent.getAgentId();
    }

    @ShellMethod(key = "agent-show", value = "Zeigt einen Agenten.")
    public String show(@ShellOption("--id") Long id) { return format(service.get(id)); }

    @ShellMethod(key = "agent-update", value = "Aktualisiert Agent und Channel; leere optionale Werte bleiben unverändert.")
    public String update(@ShellOption("--id") Long id,
                         @ShellOption(defaultValue = "", value = "--name") String name,
                         @ShellOption(defaultValue = "", value = "--prompt") String prompt,
                         @ShellOption(defaultValue = "", value = "--provider") String provider,
                         @ShellOption(defaultValue = "", value = "--model") String model,
                         @ShellOption(defaultValue = "", value = "--channel-type") String type,
                         @ShellOption(defaultValue = "", value = "--channel-id") String channelId,
                         @ShellOption(defaultValue = "", value = "--response-url") String responseUrl,
                         @ShellOption(defaultValue = "", value = "--tools") String toolIds) {
        AgentEntity agent = service.update(id, name, prompt, provider, model,
                type.isBlank() ? null : parseType(type), channelId, responseUrl, toolIds.isBlank() ? null : toolIds);
        return "Agent aktualisiert: " + agent.getAgentId();
    }

    @ShellMethod(key = "agent-delete", value = "Löscht einen Agenten inklusive seines Channels.")
    public String delete(@ShellOption("--id") Long id) { service.delete(id); return "Agent gelöscht: " + id; }

    @ShellMethod(key = "provider-list", value = "Zeigt Provider-Codes der registrierten Clients.")
    public String providers() { return String.join(System.lineSeparator(), clients.providerCodes()); }

    @ShellMethod(key = "channel-type-list", value = "Zeigt bekannte Channel-Typen.")
    public String channelTypes() { return Arrays.stream(ChannelType.values()).map(Enum::name).collect(Collectors.joining(System.lineSeparator())); }

    @ShellMethod(key = "tool-list", value = "Zeigt verfügbare Tool-IDs.")
    public String toolIds() { return String.join(System.lineSeparator(), tools.toolIds()); }

    private ChannelType parseType(String value) { try { return ChannelType.valueOf(value.trim().toUpperCase()); } catch (RuntimeException e) { throw new IllegalArgumentException("Unbekannter ChannelType: " + value); } }
    private String format(AgentEntity a) { var c = a.getChannels().isEmpty() ? null : a.getChannels().getFirst(); return "%d | %s | %s | %s | %s | channel=%s/%s".formatted(a.getAgentId(), a.getName(), a.getProvider(), a.getModelId(), a.getToolIds(), c == null ? "-" : c.getType(), c == null ? "-" : c.getChannelId()); }
}
