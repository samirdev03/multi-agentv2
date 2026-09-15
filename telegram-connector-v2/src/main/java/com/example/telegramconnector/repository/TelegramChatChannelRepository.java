package com.example.telegramconnector.repository;
import com.example.telegramconnector.domain.TelegramChatChannel; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TelegramChatChannelRepository extends JpaRepository<TelegramChatChannel,String>{Optional<TelegramChatChannel> findByTelegramChatIdAndBotChannelId(Long chatId,String botChannelId);}
