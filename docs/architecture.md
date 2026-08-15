# 苍穹外卖 —— 架构图集

> 本文件包含项目全部架构图，用于 README 展示与项目讲解背诵。
> 使用 Mermaid 语法，GitHub 会自动渲染。

---

## 1. 系统总体架构图（一图看懂全貌）

```mermaid
flowchart TB
    subgraph 客户端
        A[管理端 Web<br/>Vue2 + Element UI<br/>Nginx 8089 托管]
        B[用户端 小程序 API]
    end

    subgraph 后端服务 [Spring Boot 8080]
        C[Controller 层<br/>admin/ 管理端 + user/ 用户端]
        D[Service 层<br/>业务逻辑/事务]
        E[Mapper 层<br/>MyBatis-Plus]
    end

    subgraph 基础设施
        F[(MySQL<br/>业务数据)]
        G[(Redis<br/>营业状态)]
        H[WebSocket<br/>订单实时推送]
    end

    A -- HTTP /api/ --> C
    B -- HTTP /user/ --> C
    A -- WS /ws/ --> H
    C --> D --> E
    E --> F
    E --> G
    D -. 支付成功推送 .-> H
```

**背诵要点**：两个客户端 → 一个 Spring Boot → 两个存储 + 一个推送通道。管理端走 nginx 托管静态页 + 反代 /api；用户端直接调 /user/ 接口。

---

## 2. 后端分层架构（面试必问三层架构）

```mermaid
flowchart TB
    subgraph 请求入口
        I1[JwtTokenAdminInterceptor<br/>拦截 /admin/**]
        I2[JwtTokenUserInterceptor<br/>拦截 /user/**]
    end

    subgraph 分层
        C[Controller 层<br/>接收请求/参数绑定/返回 Result]
        S[Service 层<br/>业务逻辑/状态流转/事务]
        M[Mapper 层<br/>SQL 操作]
    end

    subgraph 横切关注点
        A1[AutoFillAspect AOP<br/>自动填充创建/更新时间]
        H1[GlobalExceptionHandler<br/>全局异常处理]
    end

    subgraph 数据层
        DB[(MySQL)]
        RD[(Redis)]
    end

    I1 --> C
    I2 --> C
    C --> S --> M --> DB
    S --> RD
    A1 -. 拦截 Mapper 方法 .-> M
    H1 -. 兜底所有异常 .-> C
```

**背诵要点**：请求先进拦截器（验 JWT）→ Controller → Service → Mapper → 数据库；AOP 横切 Mapper 自动填字段；全局异常处理器兜底。

---

## 3. 订单状态机（核心业务，必背）

```mermaid
stateDiagram-v2
    [*] --> 1待付款: 用户下单
    1待付款 --> 2待接单: 支付成功(模拟)
    1待付款 --> 6已取消: 超时15分钟未支付(定时任务)
    2待接单 --> 3已接单: 管理端接单
    2待接单 --> 6已取消: 管理端拒单/取消
    3已接单 --> 4派送中: 管理端派送
    3已接单 --> 6已取消: 用户/管理端取消
    4派送中 --> 5已完成: 管理端完成
    4派送中 --> 5已完成: 超过预计送达(定时任务自动完成)
    5已完成 --> [*]
    6已取消 --> [*]
```

**背诵要点**：正向流转 1→2→3→4→5，任意可取消到 6；两个定时任务兜底（超时未支付取消、超时未送达自动完成）。

---

## 4. 下单 → 实时通知 全流程时序（WebSocket 亮点）

```mermaid
sequenceDiagram
    participant U as 用户端(小程序)
    participant S as 后端 SpringBoot
    participant R as Redis
    participant A as 管理端(Web)
    participant W as WebSocketServer

    U->>S: 1. POST /user/order/submit 下单
    S-->>U: 返回订单号(待付款)
    U->>S: 2. PUT /user/order/payment 支付(模拟)
    S->>S: 更新状态 1→2 待接单
    S->>W: 3. 推送 {type:1, orderId, content}
    W-->>A: 4. 右上角弹"待接单"通知+提示音
    S->>S: 5. 插入 message 表(未读)
    A->>S: 6. 点击通知 → 跳转订单管理
    S-->>A: 自动打开订单详情弹窗
    A->>S: 7. 接单/派送/完成
```

**背诵要点**：支付成功双通道——WebSocket 实时弹窗（快）+ message 表持久化（消息中心可查）。

---

## 5. 消息中心设计（消息表 + 已读机制）

```mermaid
flowchart LR
    subgraph 消息产生
        P[订单支付成功]
        R[用户催单]
    end

    subgraph 后端
        M[(message 表<br/>content/details/type/is_read/create_time)]
        API[MessageController<br/>/admin/messages/*]
    end

    subgraph 前端消息中心
        P1[分页查询 page]
        P2[未读数 countUnread]
        P3[标记已读 setStatus/batch]
        P4[删除已读 deleteRead]
    end

    P --> M
    R --> M
    M --> API --> P1
    M --> API --> P2
    M --> API --> P3
    M --> API --> P4
```

**背诵要点**：消息表字段（content/details/type/is_read/create_time），4 个接口（分页/未读/已读/删除），支付和催单是消息来源。

---

## 6. 定时任务设计（@Scheduled 兜底机制）

```mermaid
flowchart TB
    subgraph OrderTask
        T1[每分钟<br/>超时未支付订单 → 自动取消]
        T2[每30分钟<br/>超时未送达 → 自动完成]
    end

    subgraph ShopTask
        T3[每天 06:00<br/>自动营业 status=1]
        T4[每天 23:00<br/>自动打烊 status=0]
    end

    T1 --> O1[orders 表<br/>status=1 且 超15分钟 → 6]
    T2 --> O2[orders 表<br/>status=4 且 超预计送达 → 5]
    T3 --> R1[Redis shop:status = 1]
    T4 --> R1[Redis shop:status = 0]
```

**背诵要点**：4 个定时任务两两一组——OrderTask 管订单兜底（取消/完成），ShopTask 管店铺开关（Redis 状态）。

---

## 7. 数据统计与报表导出（差异化亮点）

```mermaid
flowchart LR
    subgraph 数据来源
        D1[(orders 订单表)]
        D2[(user 用户表)]
        D3[(order_detail 明细表)]
    end

    subgraph ReportMapper [按天聚合 SQL]
        S1[营业额 sum(amount) where status=5]
        S2[用户数 count create_time]
        S3[订单数 count status]
        S4[TOP10 按 name 分组 sum(number)]
    end

    subgraph 接口
        A1[营业额统计]
        A2[用户统计]
        A3[订单统计]
        A4[销量 TOP10]
        A5[数据概览]
        A6[Excel 导出 POI]
    end

    D1 --> S1 --> A1
    D2 --> S2 --> A2
    D1 --> S3 --> A3
    D3 --> S4 --> A4
    A5 --> A1 & A2 & A3
    A6 --> A1
```

**背诵要点**：报表 = 按天分组的聚合 SQL；导出 = POI 动态建 Excel 文件写进响应流；前端 responseType:blob 接收下载。

---

## 8. 登录认证流程（JWT）

```mermaid
sequenceDiagram
    participant U as 用户/管理员
    participant C as Controller
    participant S as Service
    participant DB as 数据库
    participant I as JWT拦截器

    U->>C: 1. POST /login {username,password}
    C->>S: 2. 查询用户+校验密码(MD5)
    S-->>C: 3. 生成 JWT token(含用户id)
    C-->>U: 4. 返回 token
    Note over U,I: 后续请求携带 token
    U->>I: 5. 请求业务接口 + header: token
    I->>I: 6. 解析校验 token
    I->>I: 7. 用户id存入 ThreadLocal
    I-->>C: 8. 放行 → 执行业务
    C-->>U: 9. 返回业务数据
```

**背诵要点**：登录发 token → 拦截器验 token → ThreadLocal 存当前用户 → Controller 用 UserContext.getCurrentId() 拿用户。

---

## 9. 项目模块结构

```mermaid
flowchart TB
    SKY[sky-take-out 父工程]
    SKY --> C[sky-common<br/>常量/异常/Result/工具/JWT]
    SKY --> P[sky-pojo<br/>entity 实体/dto 入参/vo 出参]
    SKY --> S[sky-server<br/>controller/service/mapper<br/>config/interceptor/aspect/task]
    FE[project-sky-admin-vue-ts<br/>管理端前端 Vue2+TS]
    NG[nginx-1.20.2<br/>静态托管+反向代理]
```

**背诵要点**：Maven 三模块——common 工具、pojo 数据模型、server 业务；前端独立仓库，nginx 做托管和代理。
