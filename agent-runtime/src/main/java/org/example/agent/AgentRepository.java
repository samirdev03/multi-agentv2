package org.example.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<AgentEntity, Long> {
    @EntityGraph(attributePaths = {"channels", "toolIds"})
    @Query("select distinct a from AgentEntity a")
    List<AgentEntity> findAllWithChannels();

    @EntityGraph(attributePaths = {"channels", "toolIds"})
    @Query("select distinct a from AgentEntity a where a.agentId = :id")
    Optional<AgentEntity> findByIdWithChannels(Long id);

    Optional<AgentEntity> findFirstByChannels_ChannelId(String channelId);

    Optional<AgentEntity> findFirstByChannels_TypeAndChannels_ChannelId(
            String type,
            String channelId
    );

    boolean existsByChannels_TypeAndChannels_ChannelId(String type, String channelId);

}
