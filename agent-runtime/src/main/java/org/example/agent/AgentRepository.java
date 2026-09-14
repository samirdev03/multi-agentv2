package org.example.agent;

import org.example.api.dto.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentRepository extends JpaRepository<AgentEntity, Long> {
    Optional<AgentEntity> findByChannelId(String channelId);
    Optional<AgentEntity> findByChannel(ChannelType type, String channelId);

}
