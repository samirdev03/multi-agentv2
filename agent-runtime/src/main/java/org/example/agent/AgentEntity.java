package org.example.agent;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="agents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "agent_id")
    private Long agentId;

    @Column(columnDefinition = "name")
    private String name;

    @Column(columnDefinition = "systemPrompt")
    private String systemPrompt;

    @Column(columnDefinition = "channelId")
    private String channelId;

    @Column(columnDefinition = "modelId")
    private String modelId;

    @Column(columnDefinition = "provider")
    private String provider;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "agent_tools",
            joinColumns = @JoinColumn(name = "agent_id")
    )
    @Column(name = "tool_name")
    @Builder.Default
    private Set<String> tools = new HashSet<>();

}
