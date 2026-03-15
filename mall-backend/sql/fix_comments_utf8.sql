USE `store`;

ALTER TABLE `ums_user`
  MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN `username` varchar(64) NOT NULL COMMENT '用户名(唯一登录凭证)',
  MODIFY COLUMN `password` varchar(255) NOT NULL COMMENT '密码(必须加密存储)',
  MODIFY COLUMN `phone` varchar(20) DEFAULT NULL COMMENT '手机号码(可选，后期可用于找回密码)',
  MODIFY COLUMN `nickname` varchar(64) DEFAULT NULL COMMENT '用户昵称',
  MODIFY COLUMN `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  MODIFY COLUMN `status` tinyint(1) DEFAULT '1' COMMENT '账号启用状态: 0->禁用; 1->启用',
  MODIFY COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  MODIFY COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  MODIFY COLUMN `is_deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除: 0->未删除; 1->已删除',
  COMMENT='C端用户表(账号密码体系)';

ALTER TABLE `pms_spu`
  MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品SPU主键',
  MODIFY COLUMN `name` varchar(128) NOT NULL COMMENT '商品名称(如 iPhone 15)',
  MODIFY COLUMN `description` varchar(255) DEFAULT NULL COMMENT '商品副标题/卖点描述',
  MODIFY COLUMN `main_image` varchar(255) NOT NULL COMMENT '商品主图',
  MODIFY COLUMN `detail_html` text COMMENT '商品富文本详情(PC/H5展示)',
  MODIFY COLUMN `publish_status` tinyint(1) DEFAULT '0' COMMENT '上架状态: 0->下架; 1->上架',
  MODIFY COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  COMMENT='商品SPU表';

ALTER TABLE `pms_sku`
  MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品SKU主键',
  MODIFY COLUMN `spu_id` bigint(20) NOT NULL COMMENT '关联SPU_ID',
  MODIFY COLUMN `sku_name` varchar(255) NOT NULL COMMENT 'SKU完整名称(如 iPhone 15 黑色 256G)',
  MODIFY COLUMN `sku_code` varchar(64) NOT NULL COMMENT 'SKU编码(商家内部管理用)',
  MODIFY COLUMN `price` decimal(10,2) NOT NULL COMMENT '销售价格',
  MODIFY COLUMN `stock` int(11) NOT NULL DEFAULT '0' COMMENT '可用库存',
  MODIFY COLUMN `lock_stock` int(11) NOT NULL DEFAULT '0' COMMENT '锁定库存(用于并发防超卖)',
  MODIFY COLUMN `image` varchar(255) DEFAULT NULL COMMENT '规格图片(如黑色手机图)',
  MODIFY COLUMN `attributes` json DEFAULT NULL COMMENT '规格属性JSON(如 {"color":"black", "storage":"256G"})',
  MODIFY COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  COMMENT='商品SKU表';

ALTER TABLE `oms_order`
  MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单主键ID',
  MODIFY COLUMN `order_sn` varchar(64) NOT NULL COMMENT '订单编号(系统生成，展示给用户)',
  MODIFY COLUMN `user_id` bigint(20) NOT NULL COMMENT '下单用户ID',
  MODIFY COLUMN `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  MODIFY COLUMN `pay_amount` decimal(10,2) NOT NULL COMMENT '应付/实付金额(扣除优惠后)',
  MODIFY COLUMN `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '订单状态: 0->待付款; 1->待发货; 2->待收货; 3->已完成; 4->已取消',
  MODIFY COLUMN `payment_time` datetime DEFAULT NULL COMMENT '支付成功时间',
  MODIFY COLUMN `receiver_name` varchar(64) NOT NULL COMMENT '收货人姓名',
  MODIFY COLUMN `receiver_phone` varchar(32) NOT NULL COMMENT '收货人电话',
  MODIFY COLUMN `receiver_detail_address` varchar(255) NOT NULL COMMENT '详细收货地址',
  MODIFY COLUMN `delivery_company` varchar(64) DEFAULT NULL COMMENT '物流公司(发货时填写)',
  MODIFY COLUMN `delivery_sn` varchar(64) DEFAULT NULL COMMENT '物流单号(发货时填写)',
  MODIFY COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交订单时间',
  MODIFY COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  COMMENT='订单主表';

ALTER TABLE `oms_order_item`
  MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN `order_id` bigint(20) NOT NULL COMMENT '关联订单主表ID',
  MODIFY COLUMN `order_sn` varchar(64) NOT NULL COMMENT '订单编号(冗余字段，便于查询)',
  MODIFY COLUMN `spu_id` bigint(20) NOT NULL COMMENT '商品SPU_ID',
  MODIFY COLUMN `sku_id` bigint(20) NOT NULL COMMENT '商品SKU_ID',
  MODIFY COLUMN `sku_name` varchar(255) NOT NULL COMMENT '购买时SKU名称(快照)',
  MODIFY COLUMN `sku_pic` varchar(255) DEFAULT NULL COMMENT '购买时SKU图片(快照)',
  MODIFY COLUMN `sku_price` decimal(10,2) NOT NULL COMMENT '购买时价格(快照)',
  MODIFY COLUMN `sku_quantity` int(11) NOT NULL COMMENT '购买数量',
  MODIFY COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  COMMENT='订单商品明细快照表';
