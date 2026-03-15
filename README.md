# store

简易在线商城全栈项目（Spring Boot + Vue 3）。

## 1. 环境要求

- JDK: `17`
- Maven: `3.9+`（或直接用项目自带 `mvnw`）
- Node.js: `18+`（建议 `20+`）
- npm: `9+`
- MySQL: `8.0+`
- Redis: 可选（当前版本已引入依赖，但核心流程不强依赖）

## 2. 项目结构

- `mall-backend`: Spring Boot 后端
- `mall-frontend`: Vue 3 + Vite 前端
- `database.md`: 业务建表说明
- `PRD.md` / `TechDesign.md`: 产品与技术设计文档

## 3. 数据库准备

1. 创建数据库：`store`（`utf8mb4`）
2. 按 `database.md` 建表（已用到的核心表包含：
   `ums_user`、`ums_user_address`、`pms_spu`、`pms_sku`、`oms_cart_item`、`oms_order`、`oms_order_item`、`oms_payment_info`、`pms_category`）

当前后端默认连接（见 `mall-backend/src/main/resources/application.properties`）：

- URL: `jdbc:mysql://127.0.0.1:3306/store...`
- 用户: `root`
- 密码: `xzyxzy99`

建议本地按需修改为自己的数据库账号密码。

## 4. 运行命令

### 4.1 启动后端

在项目根目录执行：

```powershell
cd mall-backend
.\mvnw spring-boot:run
```

后端默认端口：`8080`

### 4.2 启动前端

新开终端，在项目根目录执行：

```powershell
cd mall-frontend
npm install
npm run dev
```

前端默认端口：`5173`  
Vite 已配置代理：`/api -> http://127.0.0.1:8080`

## 5. 测试命令

### 5.1 后端测试

```powershell
cd mall-backend
.\mvnw test
```

### 5.2 前端构建检查

```powershell
cd mall-frontend
npm run build
```

## 6. 当前已实现（摘要）

- 用户注册/登录（JWT）
- 个人信息、头像上传、手机号绑定
- 地址管理：新增、修改、分页列表（5条/页）、单删/批删/全删
- 商品首页与商品详情接口
- 购物车：加入、改数量、分页列表（5条/页）
- 前端：商品详情加入购物车、购物车分页与多选/单选/全选、个人中心地址管理页面

## 7. 当前未实现 / 待完善

- 订单主流程未完成：`/api/order/create` 仍为占位返回（`TODO_ORDER_SN`）
- 订单确认页、订单列表页前端仍是占位页面，未完成真实下单与订单查询闭环
- 支付流程未接通：第三方支付回调、支付状态流转、支付流水落库未实现
- 订单状态机（待支付/待发货/待收货/已完成/已取消）与超时取消释放库存未实现
- 库存并发防超卖（锁库存、支付后扣减、回滚释放）尚未完整落地
- B 端管理后台（商品管理、发货、看板）接口与前端未实现
- 商品分类 `pms_category` 已有表，但分类管理/挂载商品的业务接口未完成
- 全局安全体系待加强：当前主要是控制器手动解析 Token，未形成完整 Spring Security 鉴权链路
