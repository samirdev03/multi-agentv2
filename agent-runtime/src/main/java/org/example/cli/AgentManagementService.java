package org.example.cli;

import jakarta.persistence.EntityNotFoundException;
import org.example.agent.AgentChannelEntity;
import org.example.agent.AgentEntity;
import org.example.agent.AgentRepository;
import org.example.web.dto.ChannelType;
import org.example.tools.ToolRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class AgentManagementService {
    private final AgentRepository repository;
    private final ToolRegistry toolRegistry;

    public AgentManagementService(AgentRepository repository, ToolRegistry toolRegistry) {
        this.repository = repository;
        this.toolRegistry = toolRegistry;
    }

    @Transactional
    public AgentEntity create(String name, String prompt, String provider, String model,
                              ChannelType type, String channelId, String responseUrl, String tools) {
        requireText(name, "name");
        requireText(prompt, "systemPrompt");
        requireText(provider, "provider");
        requireText(model, "modelId");
        requireText(channelId, "channelId");
        if (repository.existsByChannels_TypeAndChannels_ChannelId(type.name(), channelId)) {
            throw new IllegalArgumentException("Channel ist bereits einem Agenten zugeordnet: " + type + "/" + channelId);
        }
        AgentEntity agent = AgentEntity.builder()
                .name(name).systemPrompt(prompt).provider(provider).modelId(model)
                .toolIds(parseTools(tools)).build();
        AgentChannelEntity channel = AgentChannelEntity.builder()
                .type(type.name()).channelId(channelId)
                .responseUrl(blankToNull(responseUrl)).agent(agent).build();
        agent.setChannels(List.of(channel));
        return repository.save(agent);
    }

    @Transactional
    public AgentEntity update(Long id, String name, String prompt, String provider, String model,
                              ChannelType type, String channelId, String responseUrl, String tools) {
        AgentEntity agent = repository.findById(id).orElseThrow(() -> notFound(id));
        AgentChannelEntity channel = agent.getChannels() == null || agent.getChannels().isEmpty()
                ? null : agent.getChannels().getFirst();
        if (channel == null) throw new IllegalStateException("Agent besitzt keinen Channel");
        String newChannelId = blankToNull(channelId) == null ? channel.getChannelId() : channelId;
        ChannelType newType = type == null ? ChannelType.valueOf(channel.getType()) : type;
        boolean changed = !newType.name().equals(channel.getType()) || !newChannelId.equals(channel.getChannelId());
        if (changed && repository.existsByChannels_TypeAndChannels_ChannelId(newType.name(), newChannelId)) {
            AgentEntity owner = repository.findFirstByChannels_TypeAndChannels_ChannelId(newType.name(), newChannelId).orElse(null);
            if (owner != null && !id.equals(owner.getAgentId())) throw new IllegalArgumentException("Channel ist bereits belegt");
        }
        if (blankToNull(name) != null) agent.setName(name);
        if (blankToNull(prompt) != null) agent.setSystemPrompt(prompt);
        if (blankToNull(provider) != null) agent.setProvider(provider);
        if (blankToNull(model) != null) agent.setModelId(model);
        if (tools != null) agent.setToolIds(parseTools(tools));
        channel.setType(newType.name()); channel.setChannelId(newChannelId);
        if (responseUrl != null) channel.setResponseUrl(blankToNull(responseUrl));
        return repository.save(agent);
    }

    @Transactional(readOnly = true)
    public List<AgentEntity> list() { return repository.findAllWithChannels(); }

    @Transactional(readOnly = true)
    public AgentEntity get(Long id) { return repository.findByIdWithChannels(id).orElseThrow(() -> notFound(id)); }

    @Transactional
    public void delete(Long id) { repository.delete(get(id)); }

    private LinkedHashSet<String> parseTools(String value) {
        if (value == null || value.isBlank()) return new LinkedHashSet<>();
        LinkedHashSet<String> ids = Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isBlank()).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        ids.forEach(toolRegistry::get);
        return ids;
    }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
    private void requireText(String value, String name) { if (blankToNull(value) == null) throw new IllegalArgumentException(name + " darf nicht leer sein"); }
    private EntityNotFoundException notFound(Long id) { return new EntityNotFoundException("Agent nicht gefunden: " + id); }
}
