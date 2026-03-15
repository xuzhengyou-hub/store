### 1. 用户模块 (UMS - User Management System)

这是最基础的用户表，满足 PRD 中“手机号+验证码”登录的需求。
+ user=root
+ password=xzyxzy99

先创建一个数据库：store
再将下面的表插入


```sql
CREATE TABLE `ums_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '用户名(唯一登录凭证)',
  `password` varchar(255) NOT NULL COMMENT '密码(必须加密存储)',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号码(可选, 后期可用于找回密码)',
  `nickname` varchar(64) DEFAULT NULL COMMENT '用户昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint(1) DEFAULT '1' COMMENT '帐号启用状态: 0->禁用; 1->启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除: 0->未删除; 1->已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  UNIQUE KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端用户表(账号密码版)';
```

------

### 2. 商品模块 (PMS - Product Management System)

这里严格贯彻 PRD 中 **SPU（商品通用信息）** 和 **SKU（具体规格库存）** 分离的思想。

#### 2.1 商品 SPU 表 (标准产品单位)

存放诸如“iPhone 15”这种通用信息，不管什么颜色尺码，这些信息都是一样的。

```sql
CREATE TABLE `pms_spu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品SPU主键',
  `name` varchar(128) NOT NULL COMMENT '商品名称 (如: iPhone 15)',
  `description` varchar(255) DEFAULT NULL COMMENT '商品副标题/卖点描述',
  `main_image` varchar(255) NOT NULL COMMENT '商品主图',
  `detail_html` text COMMENT '商品富文本详情(PC/H5端展示)',
  `publish_status` tinyint(1) DEFAULT '0' COMMENT '上架状态: 0->下架; 1->上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU表';
```

#### 2.2 商品 SKU 表 (库存量单位) - ⚠️ 核心

这是用户真正购买的东西，比如“iPhone 15 + 黑色 + 256G”。**防超卖的核心字段就在这里。**

```sql
CREATE TABLE `pms_sku` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品SKU主键',
  `spu_id` bigint(20) NOT NULL COMMENT '关联的SPU_ID',
  `sku_name` varchar(255) NOT NULL COMMENT 'SKU完整名称 (如: iPhone 15 黑色 256G)',
  `sku_code` varchar(64) NOT NULL COMMENT 'SKU编码(商家内部管理用)',
  `price` decimal(10,2) NOT NULL COMMENT '销售价格',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '真实可用库存',
  `lock_stock` int(11) NOT NULL DEFAULT '0' COMMENT '锁定库存(用于并发防超卖)',
  `image` varchar(255) DEFAULT NULL COMMENT '该规格的图片(如黑色的手机图)',
  `attributes` json DEFAULT NULL COMMENT '规格属性(JSON格式,如: {"color":"black", "storage":"256G"})',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';
```

------

### 3. 订单模块 (OMS - Order Management System)

订单模块也必须分为“主订单”和“订单明细（商品快照）”两部分。

#### 3.1 订单主表

记录谁买了单、付了多少钱、收货地址在哪。

SQL

```sql
CREATE TABLE `oms_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单主键ID',
  `order_sn` varchar(64) NOT NULL COMMENT '订单编号(系统生成,展示给用户,建议雪花算法)',
  `user_id` bigint(20) NOT NULL COMMENT '下单用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '应付/实付金额(扣除优惠后)',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '订单状态: 0->待付款; 1->待发货; 2->待收货; 3->已完成; 4->已取消',
  `payment_time` datetime DEFAULT NULL COMMENT '支付成功时间',
  `receiver_name` varchar(64) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(32) NOT NULL COMMENT '收货人电话',
  `receiver_detail_address` varchar(255) NOT NULL COMMENT '详细收货地址',
  `delivery_company` varchar(64) DEFAULT NULL COMMENT '物流公司(发货时填写)',
  `delivery_sn` varchar(64) DEFAULT NULL COMMENT '物流单号(发货时填写)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交订单时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_sn` (`order_sn`),
  KEY `idx_user_id_status` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';
```

#### 3.2 订单明细表 (数据快照) - ⚠️ 核心

这表明细记录了该订单到底包含了哪些 SKU。

```sql
CREATE TABLE `oms_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) NOT NULL COMMENT '关联的订单主表ID',
  `order_sn` varchar(64) NOT NULL COMMENT '订单编号(冗余字段,方便查询)',
  `spu_id` bigint(20) NOT NULL COMMENT '商品SPU_ID',
  `sku_id` bigint(20) NOT NULL COMMENT '商品SKU_ID',
  `sku_name` varchar(255) NOT NULL COMMENT '购买时的SKU名称(快照)',
  `sku_pic` varchar(255) DEFAULT NULL COMMENT '购买时的SKU图片(快照)',
  `sku_price` decimal(10,2) NOT NULL COMMENT '购买时的价格(快照)',
  `sku_quantity` int(11) NOT NULL COMMENT '购买数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细快照表';
```