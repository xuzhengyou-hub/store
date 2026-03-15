# 🛠️ 技术设计文档 (TDD)：简易在线商城系统 (V1.0)

**文档状态：** V1.0 (MVP 阶段)

**核心受众：** 后端工程师、前端工程师

## 1. 整体架构设计 (System Architecture)

系统采用经典的前后端分离架构，通过 RESTful API 进行轻量级的数据交互。

- **接入层 (Frontend)：** Vue 3 负责视图渲染与路由跳转，管理用户 Token 和应用状态（Pinia）。
- **网关与控制层 (Controller)：** Spring Boot 接收 HTTP 请求，进行参数校验（`@Valid`）和请求路由。
- **业务逻辑层 (Service)：** 核心业务大脑，处理复杂的电商逻辑（如订单金额计算、库存校验）。
- **数据访问层 (DAO/Mapper)：** 使用 MyBatis-Plus 与底层数据源交互。
- **持久层与缓存层：** MySQL 负责核心数据的落地持久化；Redis 负责高频数据的读取与高并发场景下的库存预扣。

## 2. 技术栈选型 (Technology Stack)

| **领域**           | **核心技术**                     | **说明**                                                    |
| ------------------ | -------------------------------- | ----------------------------------------------------------- |
| **前端 (C端/B端)** | Vue 3 + Vite + Element Plus/Vant | 使用 Composition API，Vite 提升构建速度。                   |
| **后端框架**       | Spring Boot 3.x (Java 17)        | 现代化企业级标准，内嵌 Tomcat。                             |
| **持久层框架**     | MyBatis-Plus                     | 极大地简化单表 CRUD 操作，提高开发效率。                    |
| **数据库**         | MySQL 8.0                        | 强一致性关系型数据库，处理核心订单与账务。                  |
| **缓存与中间件**   | Redis                            | 用于存储 JWT Token 黑名单、验证码（若后续加入）、库存预热。 |
| **安全与鉴权**     | Spring Security + JWT            | 结合 BCrypt 加密实现无状态的会话管理。                      |

## 3. 核心技术方案设计 (Core Solutions)

### 3.1 用户鉴权机制 (Authentication & Security)

由于采用了前后端分离，传统的 Session 机制不再适用，我们将采用 **JWT (JSON Web Token)** 方案。

- **密码安全存储：** 前端传输明文密码（建议通过 HTTPS），后端使用 `BCryptPasswordEncoder` 进行不可逆哈希加密后存入 MySQL `ums_user` 表的 `password` 字段。
- **Token 颁发与验证：**
  1. 用户登录成功后，后端生成一个有效期为 7 天的 JWT Token，并返回给前端。
  2. 前端将 Token 存入 `localStorage`，后续所有拦截器（Axios Interceptor）自动在 Header 中携带 `Authorization: Bearer <Token>`。
  3. 后端配置 Spring Security 的 Filter 链，拦截除 `/api/user/login` 和 `/api/user/register` 外的所有请求，校验 Token 的合法性。

### 3.2 高并发库存防超卖方案 (Inventory Concurrency Control)

这是电商系统的生命线。对于 V1.0 的 MVP 版本，我们采用**“数据库乐观锁 + 状态机”**的轻量级方案，避免引入过重的分布式锁机制。

- **锁定库存（下单时）：**

  当用户点击“提交订单”时，执行更新语句时带上库存条件校验：

  SQL

  ```
  -- 利用 MySQL 的行锁和原子性，只有当真实库存减去已锁定库存大于等于购买量时，才允许扣减
  UPDATE pms_sku
  SET lock_stock = lock_stock + #{quantity}
  WHERE id = #{skuId} AND (stock - lock_stock) >= #{quantity};
  ```

  如果更新影响的行数为 0，说明库存不足，抛出 `BusinessException("库存不足")`，触发事务回滚。

- **扣减库存（支付成功后）：**

  SQL

  ```
  -- 支付成功后，真正扣减有效库存，同时释放锁定库存
  UPDATE pms_sku
  SET stock = stock - #{quantity}, lock_stock = lock_stock - #{quantity}
  WHERE id = #{skuId};
  ```

### 3.3 订单创建的分布式事务控制 (Transaction Management)

订单创建是一个复合动作，必须保证 **ACID (原子性、一致性、隔离性、持久性)**。

- **技术实现：** 在 `OrderService` 的 `createOrder` 方法上添加 `@Transactional(rollbackFor = Exception.class)` 注解。
- **执行步骤：**
  1. 校验商品是否存在及价格是否变更。
  2. 执行上述的“锁定库存” SQL。
  3. 生成订单主表记录（`oms_order`）。
  4. 生成订单明细记录（`oms_order_item`）。
- **异常兜底：** 只要第 1 到 4 步中任何一步抛出 RuntimeException（例如锁定库存失败抛出异常），Spring 框架会自动回滚所有已执行的 SQL，绝不会产生脏数据。

## 4. RESTful API 接口规范设计 (API Design)

前后端对接需要统一的数据返回格式，后端统一定义 `Result<T>` 泛型类：

JSON

```
{
  "code": 200,          // 状态码：200 成功，400 参数错误，401 未登录，500 服务器错误
  "message": "操作成功",  // 提示信息
  "data": { ... }       // 实际返回的数据负载
}
```

**核心接口清单示例：**

### 🛒 一、 C端 (消费者前台) 核心接口清单

所有的 C 端接口统一以 `/api/` 作为前缀。除了登录和注册外，其他接口都需要在 HTTP Header 中携带 JWT Token。

#### 1. 用户认证与个人中心 (User)

| **接口路径**             | **请求方式** | **核心功能**         | **核心参数/说明**                 |
| ------------------------ | ------------ | -------------------- | --------------------------------- |
| `/api/user/register`     | POST         | 用户注册             | `username`, `password`            |
| `/api/user/login`        | POST         | 用户登录             | 验证成功返回 `JWT Token`          |
| `/api/user/info`         | GET          | 获取当前登录用户信息 | 解析 Token 返回基础信息           |
| `/api/user/address/list` | GET          | 获取用户收货地址列表 | 结算页必须用到                    |
| `/api/user/address/add`  | POST         | 新增收货地址         | `receiverName`, `phone`, `detail` |

#### 2. 商品浏览与检索 (Product)

| **接口路径**            | **请求方式** | **核心功能**              | **核心参数/说明**                          |
| ----------------------- | ------------ | ------------------------- | ------------------------------------------ |
| `/api/product/home`     | GET          | 获取首页数据              | 轮播图、推荐商品栏位                       |
| `/api/product/spu/page` | GET          | 分页查询商品列表          | `page`, `size`, `keyword` (支持搜索)       |
| `/api/product/spu/{id}` | GET          | 获取商品 SPU 详情         | 包含商品基础介绍、富文本详情               |
| `/api/product/sku/list` | GET          | 获取指定 SPU 下的所有 SKU | `spuId` (前端用于渲染不同颜色的规格选择器) |

#### 3. 购物车模块 (Cart - *极其重要*)

| **接口路径**       | **请求方式** | **核心功能**       | **核心参数/说明**                       |
| ------------------ | ------------ | ------------------ | --------------------------------------- |
| `/api/cart/add`    | POST         | 将 SKU 加入购物车  | `skuId`, `quantity`                     |
| `/api/cart/list`   | GET          | 获取我的购物车列表 | 关联查询出每个 SKU 的当前最新价格和库存 |
| `/api/cart/update` | PUT          | 修改购物车商品数量 | `cartItemId`, `quantity`                |
| `/api/cart/delete` | DELETE       | 移除购物车商品     | `cartItemIds` (支持批量删除)            |

#### 4. 订单与支付交易 (Order - *系统的灵魂*)

| **接口路径**             | **请求方式** | **核心功能**              | **核心参数/说明**                                        |
| ------------------------ | ------------ | ------------------------- | -------------------------------------------------------- |
| `/api/order/preview`     | POST         | 订单结算页预览            | 传入选中的购物车 ID，后端计算总价和运费                  |
| `/api/order/create`      | POST         | **提交订单 (防超卖核心)** | `skuIds`, `addressId` -> 锁定库存，返回订单号            |
| `/api/order/list`        | GET          | 获取我的订单列表          | `status` (区分待付款、待发货、已完成)                    |
| `/api/order/detail/{id}` | GET          | 获取订单详情              | 展示订单快照（商品明细、实付金额）                       |
| `/api/order/cancel`      | PUT          | 取消订单                  | 手动取消或超时自动取消，**必须释放预扣库存**             |
| `/api/payment/mock`      | POST         | 模拟支付接口              | `orderSn` (V1.0前期没接微信支付，用这个测试跑通发货链路) |

------

### 💻 二、 B端 (商家管理后台) 核心接口清单

后台接口通常会有单独的路由前缀（例如 `/api/admin/...`），并且在 Spring Security 中需要校验该 Token 是否具备管理员权限（Role_Admin）。

#### 1. 基础与看板数据 (Admin & Dashboard)

| **接口路径**                | **请求方式** | **核心功能**     | **核心参数/说明**            |
| --------------------------- | ------------ | ---------------- | ---------------------------- |
| `/api/admin/login`          | POST         | 商家后台登录     | 独立于 C 端的登录逻辑        |
| `/api/admin/dashboard/stat` | GET          | 获取首页核心数据 | 今日 GMV、新增订单数、用户数 |

#### 2. 商品与库存管理 (Product Management)

| **接口路径**                | **请求方式** | **核心功能**           | **核心参数/说明**                            |
| --------------------------- | ------------ | ---------------------- | -------------------------------------------- |
| `/api/admin/spu/create`     | POST         | 发布新商品 SPU         | 基础信息、主图、详情描述录入                 |
| `/api/admin/spu/update`     | PUT          | 修改商品 SPU / 上下架  | `publishStatus` (0下架，1上架)               |
| `/api/admin/sku/save_batch` | POST         | **批量生成与设置 SKU** | 批量写入交叉规格（如红+64G）的独立价格与库存 |
| `/api/admin/sku/stock`      | PUT          | 手动调整 SKU 库存      | 应对线下补货进仓                             |

#### 3. 订单与发货管理 (Order Management)

| **接口路径**               | **请求方式** | **核心功能**     | **核心参数/说明**                                            |
| -------------------------- | ------------ | ---------------- | ------------------------------------------------------------ |
| `/api/admin/order/page`    | GET          | 全局订单分页查询 | 支持按订单号、手机号、订单状态进行复合检索                   |
| `/api/admin/order/deliver` | POST         | **订单发货**     | 必传 `orderId`, `deliveryCompany` (快递公司), `deliverySn` (物流单号) |

## 5. 目录结构规划 (Project Structure)

在 Spring Boot 中，推荐采用按业务模块划分的结构（领域驱动设计的雏形），而不是将所有 Controller 塞在一起：

Plaintext

```
src/main/java/com/yourname/mall
├── config/             # 配置类 (SecurityConfig, MyBatisPlusConfig, RedisConfig)
├── exception/          # 全局异常处理 (GlobalExceptionHandler)
├── common/             # 通用工具类与结果集封装 (Result, Constants)
├── modules/            # 核心业务模块
│   ├── user/           # 用户模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   └── entity/
│   ├── product/        # 商品模块 (SPU, SKU 逻辑)
│   └── order/          # 订单模块
└── MallApplication.java# 启动类
```