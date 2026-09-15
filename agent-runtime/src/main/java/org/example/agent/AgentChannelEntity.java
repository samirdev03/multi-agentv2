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

    @Column(name = "response_url", length = 2048)
    private String responseUrl;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private AgentEntity agent;
}
