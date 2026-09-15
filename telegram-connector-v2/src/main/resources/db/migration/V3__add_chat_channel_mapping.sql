CREATE TABLE IF NOT EXISTS telegram_chat_channels (
    channel_id VARCHAR(64) PRIMARY KEY,
    telegram_chat_id BIGINT NOT NULL,
    bot_channel_id VARCHAR(255) NOT NULL,
    CONSTRAINT uk_chat_bot UNIQUE (telegram_chat_id, bot_channel_id)
);
