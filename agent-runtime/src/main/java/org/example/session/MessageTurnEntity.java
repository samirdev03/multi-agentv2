package org.example.session;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="message_turns", uniqueConstraints=@UniqueConstraint(name="uk_message_request",columnNames="request_id")) @Getter @Setter @NoArgsConstructor
 public class MessageTurnEntity
{ @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="request_id",nullable=false,unique=true) private UUID requestId; @Column(nullable=false,length=16000) private String content; @Column(nullable=false) private String channelId; @Enumerated(EnumType.STRING) @Column(nullable=false) private MessageTurnStatus status=MessageTurnStatus.PENDING; @Column(nullable=false) private Instant receivedAt=Instant.now(); @Column(length=16000) private String responseContent; public MessageTurnEntity(UUID requestId,String channelId,String content){this.requestId=requestId;this.channelId=channelId;this.content=content;} public void processing(){status=MessageTurnStatus.PROCESSING;} public void completed(String response){status=MessageTurnStatus.COMPLETED;responseContent=response;} public void failed(){status=MessageTurnStatus.FAILED;} }
