package com.example.telegramconnector.domain;
import jakarta.persistence.*; import lombok.*; import java.util.UUID;
@Entity @Table(name="telegram_chat_channels",uniqueConstraints=@UniqueConstraint(name="uk_chat_bot",columnNames={"telegram_chat_id","bot_channel_id"})) @Getter @NoArgsConstructor(access=AccessLevel.PROTECTED)
public class TelegramChatChannel { @Id private String channelId; @Column(name="telegram_chat_id",nullable=false) private Long telegramChatId; @Column(name="bot_channel_id",nullable=false) private String botChannelId; public TelegramChatChannel(String id,Long chat,String bot){channelId=id;telegramChatId=chat;botChannelId=bot;} }
