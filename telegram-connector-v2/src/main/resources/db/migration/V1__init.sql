CREATE TABLE telegram_channel (
    channel_id VARCHAR(255) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    bot_token  VARCHAR(255) NOT NULL
);
