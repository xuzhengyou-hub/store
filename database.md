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



### 4. 用户的收货地址表 (`ums_user_address`) - 🚨 下单必备

**为什么需要它？** 之前的主订单表里虽然有收货地址字段，但那是用户下单时的**“数据快照”**。用户平时需要在个人中心管理多个地址（家里、公司），下单时直接勾选。

SQL

```sql
CREATE TABLE `ums_user_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
  `receiver_name` varchar(64) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(32) NOT NULL COMMENT '收货人电话',
  `province` varchar(64) DEFAULT NULL COMMENT '省份',
  `city` varchar(64) DEFAULT NULL COMMENT '城市',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址(街道、门牌号)',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认地址: 0->否; 1->是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';
```

### 5. 购物车表 (`oms_cart_item`) - 🛒 提升体验必备

**为什么需要它？** 很多架构师喜欢把购物车数据完全放在 Redis 里（速度极快）。但对于新手或小型商城，**强烈建议在 MySQL 里建一张购物车表作为持久化备份**。这样用户在手机上加了购物车，换到电脑上登录，购物车里的东西还在。

SQL

```sql
CREATE TABLE `oms_cart_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `spu_id` bigint(20) NOT NULL COMMENT '商品SPU_ID',
  `sku_id` bigint(20) NOT NULL COMMENT '商品SKU_ID',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '购买数量',
  `price` decimal(10,2) NOT NULL COMMENT '加入购物车时的价格(用于比价提醒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车明细表';
```

### 6. 商品分类表 (`pms_category`) - 🗂️ 浏览导航必备

**为什么需要它？** 你的商城总不能把所有商品大杂烩一样全堆在首页吧？你需要“手机数码”、“男装女装”这样的分类目录。通常这是一个**无限极分类（树形结构）**的表。

SQL

```sql
CREATE TABLE `pms_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '上级分类的编号: 0表示一级分类',
  `name` varchar(64) NOT NULL COMMENT '分类名称',
  `level` tinyint(4) NOT NULL DEFAULT '1' COMMENT '分类级别: 1->一级; 2->二级',
  `sort` int(11) DEFAULT '0' COMMENT '排序字段(数字越大越靠前)',
  `icon` varchar(255) DEFAULT NULL COMMENT '分类图标URL',
  `show_status` tinyint(1) DEFAULT '1' COMMENT '是否显示: 0->不显示; 1->显示',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';
```

*(注：建好这个表后，你需要回头在 `pms_spu` 表里加一个 `category_id` 字段，把商品挂载到对应的分类下。)*

### 7. 支付流水表 (`oms_payment_info`) - 💰 财务对账必备（可选但强烈推荐）

**为什么需要它？** 订单表 (`oms_order`) 记录的是“业务状态”，而支付表记录的是“钱的轨迹”。当用户用微信支付成功后，微信服务器会回调你的 Spring Boot 接口，你需要把微信的**第三方交易流水号**记录下来，方便以后查账或退款。

SQL

```sql
CREATE TABLE `oms_payment_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_sn` varchar(64) NOT NULL COMMENT '关联的订单号',
  `user_id` bigint(20) NOT NULL COMMENT '支付用户ID',
  `pay_type` tinyint(1) DEFAULT NULL COMMENT '支付方式: 1->微信; 2->支付宝',
  `trade_no` varchar(128) DEFAULT NULL COMMENT '第三方支付平台交易流水号(极度重要)',
  `total_amount` decimal(10,2) NOT NULL COMMENT '实际支付金额',
  `payment_status` tinyint(1) DEFAULT '0' COMMENT '支付状态: 0->未支付; 1->已支付; 2->支付失败',
  `callback_time` datetime DEFAULT NULL COMMENT '第三方回调时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_sn` (`order_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水记录表';
```