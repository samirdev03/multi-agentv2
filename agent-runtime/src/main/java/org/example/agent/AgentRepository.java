package org.example.agent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentRepository extends JpaRepository<AgentEntity, Long> {
    Optional<AgentEntity> findFirstByChannels_ChannelId(String channelId);

    Optional<AgentEntity> findFirstByChannels_TypeAndChannels_ChannelId(
            String type,
            String channelId
    );

}
