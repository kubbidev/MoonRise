-- MoonRise H2 Schema.

CREATE TABLE `{prefix}users`
(
    `id`          BIGINT       NOT NULL,
    `username`    VARCHAR(32)  NOT NULL,
    `global_name` VARCHAR(32)  NOT NULL,
    `avatar`      VARCHAR(300) NOT NULL,
    `last_seen`   BIGINT       NOT NULL,
    PRIMARY KEY (`id`)
);
CREATE INDEX ON `{prefix}users` (`username`);

CREATE TABLE `{prefix}guilds`
(
    `id`   BIGINT       NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `icon` VARCHAR(300) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `{prefix}members`
(
    `user_id`      BIGINT       NOT NULL,
    `guild_id`     BIGINT       NOT NULL,
    `nickname`     VARCHAR(32)  NOT NULL,
    `guild_avatar` VARCHAR(300) NOT NULL,
    `biography`    VARCHAR(300) NOT NULL,
    PRIMARY KEY (`user_id`)
);
CREATE INDEX ON `{prefix}members` (`guild_id`);