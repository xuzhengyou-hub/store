SET NAMES utf8mb4;
USE `store`;

INSERT INTO `pms_spu` (`id`, `name`, `description`, `main_image`, `detail_html`, `publish_status`)
VALUES
  (1, '华为畅享 50z 5000万高清AI三摄 5000mAh超能续航 128GB 宝石蓝', '5000万高清三摄 5000mAh超能续航 128GB 宝石蓝 大内存鸿蒙智能手机', 'https://placehold.co/420x420/ece6ff/5f57c7?text=Phone+1', '<p>华为畅享 50z 详细图文介绍</p>', 1),
  (2, '高性能轻薄笔记本 14英寸', '12代处理器 16GB内存 512GB固态 轻薄办公本', 'https://placehold.co/420x420/eaf6ff/3478c7?text=Laptop+2', '<p>轻薄笔记本详细图文介绍</p>', 1),
  (3, '主动降噪蓝牙耳机 Pro', '超长续航 多场景降噪 智能通透模式', 'https://placehold.co/420x420/f2f2f2/666666?text=Earbuds+3', '<p>蓝牙耳机详细图文介绍</p>', 1),
  (4, '多功能智能手表 S', '运动健康监测 全天候心率记录', 'https://placehold.co/420x420/f4f8ff/6a82c7?text=Watch+4', '<p>智能手表详细图文介绍</p>', 1),
  (5, '专业便携投影仪', '1080P高清 自动校正 小体积大画面', 'https://placehold.co/420x420/fff2e8/c17939?text=Projector+5', '<p>投影仪详细图文介绍</p>', 1),
  (6, '高速固态移动硬盘 1TB', '高速传输 大容量轻便移动存储', 'https://placehold.co/420x420/eff7f4/2f8f75?text=SSD+6', '<p>移动硬盘详细图文介绍</p>', 1),
  (7, '机械键盘 电竞版', 'RGB灯效 多键无冲 手感清脆', 'https://placehold.co/420x420/f8f0ff/8f54c7?text=Keyboard+7', '<p>机械键盘详细图文介绍</p>', 1),
  (8, '高清视频会议摄像头', '自动对焦 降噪麦克风 远程会议清晰流畅', 'https://placehold.co/420x420/f2fbff/4d93b3?text=Camera+8', '<p>摄像头详细图文介绍</p>', 1),
  (9, '电竞显示器 27英寸', '2K分辨率 高刷新率 低延迟', 'https://placehold.co/420x420/f5f7ff/6273c7?text=Monitor+9', '<p>显示器详细图文介绍</p>', 1),
  (10, '平板电脑 11英寸', '学习娱乐办公一体 轻薄长续航', 'https://placehold.co/420x420/f8f8f8/666666?text=Tablet+10', '<p>平板电脑详细图文介绍</p>', 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `description` = VALUES(`description`),
  `main_image` = VALUES(`main_image`),
  `detail_html` = VALUES(`detail_html`),
  `publish_status` = VALUES(`publish_status`);

INSERT INTO `pms_sku` (`id`, `spu_id`, `sku_name`, `sku_code`, `price`, `stock`, `lock_stock`, `image`, `attributes`)
VALUES
  (1, 1, '华为畅享 50z 128G 宝石蓝', 'SKU-0001', 2600.00, 1517, 0, 'https://placehold.co/420x420/ece6ff/5f57c7?text=Phone+1', '{"color":"宝石蓝","storage":"128G"}'),
  (2, 2, '轻薄笔记本 14英寸 16G+512G', 'SKU-0002', 4999.00, 367, 0, 'https://placehold.co/420x420/eaf6ff/3478c7?text=Laptop+2', '{"memory":"16G","disk":"512G"}'),
  (3, 3, '主动降噪耳机 Pro 黑色', 'SKU-0003', 899.00, 1288, 0, 'https://placehold.co/420x420/f2f2f2/666666?text=Earbuds+3', '{"color":"黑色"}'),
  (4, 4, '智能手表 S 经典款', 'SKU-0004', 1599.00, 652, 0, 'https://placehold.co/420x420/f4f8ff/6a82c7?text=Watch+4', '{"strap":"硅胶"}'),
  (5, 5, '便携投影仪 标准版', 'SKU-0005', 2299.00, 341, 0, 'https://placehold.co/420x420/fff2e8/c17939?text=Projector+5', '{"resolution":"1080P"}'),
  (6, 6, '移动固态硬盘 1TB', 'SKU-0006', 699.00, 981, 0, 'https://placehold.co/420x420/eff7f4/2f8f75?text=SSD+6', '{"capacity":"1TB"}'),
  (7, 7, '机械键盘 电竞版 红轴', 'SKU-0007', 329.00, 1203, 0, 'https://placehold.co/420x420/f8f0ff/8f54c7?text=Keyboard+7', '{"switch":"红轴"}'),
  (8, 8, '高清会议摄像头 USB版', 'SKU-0008', 459.00, 777, 0, 'https://placehold.co/420x420/f2fbff/4d93b3?text=Camera+8', '{"interface":"USB"}'),
  (9, 9, '电竞显示器 27英寸 2K', 'SKU-0009', 1899.00, 288, 0, 'https://placehold.co/420x420/f5f7ff/6273c7?text=Monitor+9', '{"size":"27"}'),
  (10, 10, '平板电脑 11英寸 256G', 'SKU-0010', 2699.00, 419, 0, 'https://placehold.co/420x420/f8f8f8/666666?text=Tablet+10', '{"storage":"256G"}')
ON DUPLICATE KEY UPDATE
  `spu_id` = VALUES(`spu_id`),
  `sku_name` = VALUES(`sku_name`),
  `sku_code` = VALUES(`sku_code`),
  `price` = VALUES(`price`),
  `stock` = VALUES(`stock`),
  `lock_stock` = VALUES(`lock_stock`),
  `image` = VALUES(`image`),
  `attributes` = VALUES(`attributes`);