package org.example.agent;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
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

    @OneToMany(
            mappedBy = "agent",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AgentChannelEntity> channels;


    @Column(columnDefinition = "modelId")
    private String modelId;

    @Column(columnDefinition = "provider")
    private String provider;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "agent_tools",
            joinColumns = @JoinColumn(name = "agent_id")
    )
    @Column(name = "tool_id")
    @Builder.Default
    private Set<String> toolIds = new HashSet<>();

}
