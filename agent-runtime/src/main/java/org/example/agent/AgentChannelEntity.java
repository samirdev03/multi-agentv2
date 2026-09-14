package org.example.agent;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agent_channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String channelId;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private AgentEntity agent;
}