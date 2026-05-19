# 减减 - AI 减肥管家小程序

> 一款基于 AI 的智能减肥助手小程序，提供体重记录、饮食管理、运动追踪、经期记录、AI 对话等一体化服务。

![微信小程序](https://img.shields.io/badge/Platform-WeChat%20Mini%20Program-07C160?style=flat-square&logo=weixin)
![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?style=flat-square&logo=spring)
![Redis](https://img.shields.io/badge/Redis-会话管理-red?style=flat-square&logo=redis)

---

## 功能特性

### 核心功能
- **首页数据看板** - 体重趋势图、维度变化、距目标/距上次/累计变化统计
- **体重记录** - 快速记录体重，支持历史导入和趋势分析
- **饮食记录** - 记录早午晚餐/加餐，追踪卡路里摄入
- **运动记录** - 支持多种运动类型，内置搜索快速选取
- **维度记录** - 记录腰/臀/胸/臂/腿等身体围度
- **经期记录** - 经期日历、周期预测、阶段建议

### AI 智能助手
- **自然语言记录** - 用自然对话记录体重、运动、饮食、经期、维度
- **每日方案** - AI 根据个人档案生成个性化饮食/运动方案
- **情绪支持** - 识别负面情绪并给予安慰和建议
- **会话管理** - Redis 滑动窗口会话（20 条消息 / 24h TTL）

### 个人档案
- 基础信息（昵称、年龄、性别、身高）
- 体重目标（初始体重、目标体重、计划周期）
- 体质标签（易水肿、代谢低、碳水敏感等）
- 运动习惯 / 饮食习惯 / 生活状态

---

## 技术架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  微信小程序     │────▶│   Spring Boot   │────▶│     MySQL       │
│  diet-butler-   │     │   Backend       │     │   Database      │
│    mini         │     │   (Port 8080)   │     │                 │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │     Redis       │
                        │  (会话存储)     │
                        └─────────────────┘
```

### 小程序端
- **框架**：微信小程序原生开发
- **状态管理**：Storage Sync API
- **UI**：自定义 WXML/WXSS

### 后端
- **框架**：Spring Boot 3.x
- **语言**：Java 21
- **数据库**：MySQL + Hibernate JPA
- **会话**：Spring Data Redis（滑动窗口）
- **AI**：Spring AI + 通义千问 API
- **安全**：JWT Token 认证

---

## 快速开始

### 1. 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 21+ | 后端运行 |
| Maven | 3.8+ | 后端构建 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6.0+ | 会话存储 |
| 微信开发者工具 | 最新版 | 小程序开发调试 |

### 2. 启动 Redis

```bash
docker run -d --name redis \
  -p 6379:6379 \
  redis:7-alpine
```

### 3. 配置后端

编辑 `server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/diet_butler?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379

llm:
  api-key: your_dashscope_api_key   # 阿里云通义千问 API Key

jwt:
  secret: your-32-char-minimum-secret-key
  expire-days: 7

wechat:
  appid: your_wechat_appid
  secret: your_wechat_secret
```

### 4. 启动后端

```bash
cd server
mvn compile
mvn spring-boot:run
```

后端启动于 `http://localhost:8080`

### 5. 启动小程序

1. 打开微信开发者工具
2. 导入 `diet-butler-mini` 项目
3. 确保 `project.config.json` 中 `appid` 正确
4. 修改 `diet-butler-mini/config/api.js` 中的 `API_BASE`：
   ```javascript
   const API_BASE = 'http://localhost:8080/api';
   ```
5. 点击"编译运行"

### 6. 测试账号

| 登录方式 | 验证码 | 说明 |
|---------|--------|------|
| 游客模式 | 无需 | 推荐快速体验 |
| 手机号登录 | 控制台打印 | 开发模式 |
| 微信登录 | 需配置真实 AppID | 正式环境 |

---

## 项目结构

```
diet-butler/
├── diet-butler-mini/          # 微信小程序前端
│   ├── pages/
│   │   ├── login/             # 登录页
│   │   ├── index/             # 首页（体重趋势、维度）
│   │   ├── exercise/          # 运动记录
│   │   ├── diet/              # 饮食记录
│   │   ├── chat/              # AI 对话
│   │   └── knowledge/         # 健康知识库
│   ├── config/
│   │   └── api.js             # API 基础地址配置
│   └── app.json               # 小程序配置
│
├── server/                    # Spring Boot 后端
│   ├── src/main/java/com/dietbutler/
│   │   ├── controller/       # REST 控制器
│   │   ├── service/          # 业务逻辑
│   │   ├── repository/       # JPA 仓库
│   │   ├── entity/          # 数据库实体
│   │   ├── dto/             # 数据传输对象
│   │   └── config/          # 配置类
│   └── src/main/resources/
│       ├── application.yml   # 主配置
│       └── schema.sql       # 数据库 Schema
│
├── docs/                      # 文档
│   ├── API文档.md            # API 接口文档
│   └── 用户手册.md           # 用户使用手册
│
└── 配置说明.md                # 登录配置说明
```

---

## API 概览

| 模块 | 主要接口 |
|------|---------|
| 认证 | `/api/auth/guest`, `/api/auth/phoneLogin`, `/api/auth/sendSms` |
| 用户 | `/api/user/{id}`, `/api/user/{id}` (PUT) |
| 体重 | `/api/weight/add`, `/api/weight/history`, `/api/weight/statistics` |
| 运动 | `/api/exercise`, `/api/exercise/{userId}` |
| 饮食 | `/api/diet`, `/api/diet/{userId}` |
| 维度 | `/api/measurements`, `/api/measurements/{userId}` |
| 经期 | `/api/menstrual`, `/api/menstrual/{userId}/phase` |
| AI 对话 | `/api/chat/send`, `/api/chat/session` (创建会话) |

详细接口文档见 [docs/API文档.md](docs/API文档.md)

---

## AI 对话能力

AI 助手支持自然语言指令，自动识别意图：

| 指令示例 | AI 行为 |
|---------|--------|
| "今天体重 70 公斤" | 记录体重 |
| "今日方案" / "今天怎么吃" | 生成每日方案 |
| "打卡" / "签到" | 记录打卡 |
| "站起来了" / "站立" | 记录站立活动 |
| "心情不好" / "暴食" / "焦虑" | 情绪安抚 |
| 正常对话 | AI 根据用户档案给出建议 |

---

## 部署说明

### 开发环境（localtunnel）

```bash
# 启动后端
cd server && mvn spring-boot:run

# 启动 localtunnel
lt --port 8080

# 修改小程序 API 地址为 tunnel URL
```

### 生产环境

1. 购买云服务器（推荐 2核4G）
2. 安装 Docker Compose
3. 部署 MySQL、Redis 容器
4. 打包后端为 JAR，部署运行
5. 配置 Nginx 反向代理
6. 微信小程序后台配置合法域名

---

## 更新日志

### v1.1.0 (2026-05-18)
- 新增 Redis AI 会话管理（滑动窗口 20 条 / 24h）
- 首页体重统计计算修复
- 经期记录点击逻辑修复
- 运动记录增加搜索功能
- 新对话按钮重置会话

### v1.0.0 (2026-05-10)
- 首次发布
- 体重记录、维度记录、经期记录
- AI 对话助手
- 个性化每日方案
- 健康知识库
- 体脂估算

---

## 截图预览

| 首页 | AI 对话 | 运动记录 |
|:---:|:---:|:---:|
| 体重趋势、统计卡片 | 智能对话交互 | 运动类型搜索 |

---

## 许可证

MIT License