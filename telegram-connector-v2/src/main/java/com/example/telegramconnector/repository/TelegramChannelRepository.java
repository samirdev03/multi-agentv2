package com.example.telegramconnector.repository;

import com.example.telegramconnector.domain.TelegramChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramChannelRepository extends JpaRepository<TelegramChannel, String> {
}
