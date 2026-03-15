SET NAMES utf8mb4;
USE `store`;

INSERT INTO `ums_user` (`username`, `password`, `phone`, `nickname`, `avatar`, `status`, `is_deleted`)
VALUES (
  'anime_demo',
  '$2a$10$29YXnfrI1LYiWfpg7oXFwOGbvaxY.pio7KuVwS5YqvK94QoJ9Bx9.',
  '13800138101',
  CONVERT(UNHEX('E5B08FE9B1BCE5908CE5ADA6') USING utf8mb4),
  '/default-anime-avatar.svg',
  1,
  0
)
ON DUPLICATE KEY UPDATE
  `phone` = VALUES(`phone`),
  `nickname` = VALUES(`nickname`),
  `avatar` = VALUES(`avatar`),
  `status` = 1,
  `is_deleted` = 0;
