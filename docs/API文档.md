# 减减 API 接口文档

> 版本: 1.0.0
> 更新日期: 2026-05-10

---

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: Bearer Token (JWT)
- **Header格式**: `Authorization: Bearer <token>`

---

## 通用响应格式

```json
{
  "success": true,
  "message": "success",
  "data": { ... }
}
```

失败时:
```json
{
  "success": false,
  "message": "错误信息",
  "data": null
}
```

---

## 一、认证接口 (Auth)

### 1.1 游客登录
```
POST /api/auth/guest
Content-Type: application/json

Request:
{
  "deviceId": "设备唯一ID"
}

Response:
{
  "success": true,
  "data": {
    "success": true,
    "token": "JWT_TOKEN",
    "expireAt": "2026-05-17T21:51:36",
    "user": { ... }
  }
}
```

### 1.2 获取当前用户
```
GET /api/auth/me
Authorization: Bearer <token>

Response:
{
  "success": true,
  "data": { User对象 }
}
```

### 1.3 验证Token
```
POST /api/auth/validate
Authorization: Bearer <token>

Response:
{
  "success": true,
  "data": { "valid": true }
}
```

### 1.4 登出
```
POST /api/auth/logout
Authorization: Bearer <token>

Response:
{
  "success": true,
  "message": "已退出登录"
}
```

### 1.5 发送短信验证码
```
POST /api/auth/sendSms
Content-Type: application/json

Request:
{
  "phone": "13800138000"
}

Response:
{
  "success": true,
  "message": "验证码已发送"
}
```

### 1.6 手机号登录
```
POST /api/auth/phoneLogin
Content-Type: application/json

Request:
{
  "phone": "13800138000",
  "code": "123456",
  "nickname": "昵称（可选）"
}

Response:
{
  "success": true,
  "data": {
    "success": true,
    "token": "JWT_TOKEN",
    "user": { ... }
  }
}
```

---

## 二、用户接口 (User)

### 2.1 获取/创建用户
```
GET /api/user/getOrCreate?openid=xxx

Response:
{
  "success": true,
  "data": { User对象 }
}
```

### 2.2 获取用户详情
```
GET /api/user/{id}

Response:
{
  "success": true,
  "data": { User对象 }
}
```

### 2.3 更新用户档案
```
PUT /api/user/{id}
Content-Type: application/json
Authorization: Bearer <token>

Request:
{
  "nickname": "昵称",
  "age": 25,
  "gender": 2,          // 1=男, 2=女
  "height": 165,
  "initialWeight": 70,
  "targetWeight": 60,
  "userType": "weight_loss",  // weight_loss/shaping/maintenance/muscle_gain
  "constitutionTags": "易水肿,代谢低",
  "exerciseFrequency": 3,
  "exercisePreference": "居家",
  "fitnessLevel": "新手",
  "hasKneeIssue": false,
  "dietPreference": "清淡",
  "breakfastHabit": true,
  "dietaryTaboo": "海鲜,辛辣",
  "workPressure": 3,
  "waterIntake": 2000,
  "standingHours": 8,
  "targetAreas": "腰,腹,臀",
  "sleepStart": "22:00",
  "sleepEnd": "06:00",
  "startWeightDate": "2026-01-01",
  "weightLossPeriod": 90
}

Response:
{
  "success": true,
  "message": "更新成功",
  "data": { User对象 }
}
```

**说明**: `basicMetabolism`会根据身高、体重、年龄、性别自动计算

---

## 三、体重接口 (Weight)

### 3.1 添加体重记录
```
POST /api/weight/add?userId={id}
Content-Type: application/json
Authorization: Bearer <token>

Request:
{
  "recordDate": "2026-05-10",
  "weight": 70.5,
  "sleepStart": "22:00",
  "sleepEnd": "06:00",
  "note": "备注（可选）"
}

Response:
{
  "success": true,
  "message": "体重记录成功",
  "data": {
    "id": 64,
    "userId": 13,
    "weight": 70.5,
    "recordDate": "2026-05-10",
    ...
  }
}
```

### 3.2 获取体重历史
```
GET /api/weight/history?userId={id}&days=7

Response:
{
  "success": true,
  "data": [ WeightRecord, ... ]
}
```

### 3.3 获取最近体重
```
GET /api/weight/recent?userId={id}&days=7

Response:
{
  "success": true,
  "data": [ WeightRecord, ... ]
}
```

### 3.4 获取体重统计
```
GET /api/weight/statistics?userId={id}

Response:
{
  "success": true,
  "data": {
    "latestWeight": 70.5,
    "initialWeight": 75.0,
    "totalChange": -4.5,
    "avgWeight": 71.2
  }
}
```

### 3.5 更新体重记录
```
PUT /api/weight/{id}
Content-Type: application/json

Request:
{
  "weight": 69.5,
  "note": "更新备注"
}
```

### 3.6 删除体重记录
```
DELETE /api/weight/{id}

Response:
{
  "success": true,
  "message": "删除成功"
}
```

---

## 四、身体维度接口 (Measurement)

### 4.1 添加维度记录
```
POST /api/measurements
Content-Type: application/json

Request:
{
  "userId": 13,
  "recordDate": "2026-05-10",
  "waist": 80,
  "hip": 95,
  "chest": 90,
  "upperArm": 30,
  "forearm": 25,
  "thigh": 55,
  "calf": 38
}

Response:
{
  "success": true,
  "data": { BodyMeasurement对象 }
}
```

### 4.2 获取维度历史
```
GET /api/measurements/{userId}?days=30

Response:
{
  "success": true,
  "data": [ BodyMeasurement, ... ]
}
```

### 4.3 更新维度记录
```
PUT /api/measurements/{id}
Content-Type: application/json

Request:
{
  "waist": 78,
  "hip": 93
}
```

### 4.4 删除维度记录
```
DELETE /api/measurements/{id}

Response:
{
  "success": true,
  "message": "删除成功"
}
```

---

## 五、经期接口 (Menstrual)

### 5.1 添加经期记录
```
POST /api/menstrual
Content-Type: application/json

Request:
{
  "userId": 13,
  "cycleStartDate": "2026-05-01",
  "cycleEndDate": "2026-05-05",
  "flowLevel": "medium",  // light/medium/heavy
  "isInPeriod": true,
  "hasPain": false,
  "otherInfo": "腹胀,疲倦"
}

Response:
{
  "success": true,
  "data": { MenstrualRecord对象 }
}
```

### 5.2 获取经期历史
```
GET /api/menstrual/{userId}

Response:
{
  "success": true,
  "data": [ MenstrualRecord, ... ]
}
```

### 5.3 获取当前周期阶段
```
GET /api/menstrual/{userId}/phase

Response:
{
  "success": true,
  "data": {
    "phase": "经期",
    "cycleDay": 3,
    "daysUntilNextPeriod": null,
    "exerciseRecommendation": "适合轻度运动，如散步、瑜伽",
    "dietRecommendation": "注意补铁，多吃红肉、动物肝脏",
    "bodyStatus": "可能会有腹部不适，注意休息"
  }
}
```

---

## 六、AI对话接口 (Chat)

### 6.1 发送消息
```
POST /api/chat/send
Content-Type: application/json
Authorization: Bearer <token>

Request:
{
  "message": "今日方案"
}

Response:
{
  "success": true,
  "data": {
    "reply": "🌅 早安！测试用户\n📅 05月10日 周日\n\n📊 今日概况\n...",
    "intent": "daily_plan"
  }
}
```

**支持的意图 (intent)**:
| intent | 说明 |
|--------|------|
| greeting | 打招呼 |
| profile_setup | 建档引导 |
| profile_complete | 建档完成 |
| weight_record | 体重记录 |
| weight_curve | 体重曲线 |
| profile_query | 档案查询 |
| profile_update | 档案更新 |
| daily_plan | 每日方案 |
| checkin | 打卡 |
| emotion | 情绪表达 |
| standup | 站立记录 |
| chat | 正常对话 |

**支持的自然语言**:
- "今天体重70公斤" - 记录体重
- "今日方案" / "今天计划" - 获取每日方案
- "打卡" / "签到" - 打卡
- "站起来了" - 记录站立
- "心情不好" / "暴食" / "焦虑" - 情绪安抚

---

## 七、运动记录接口 (Exercise)

### 7.1 添加运动记录
```
POST /api/exercise
Content-Type: application/json

Request:
{
  "userId": 13,
  "type": "跑步",
  "duration": 30,
  "calories": 300,
  "recordDate": "2026-05-10",
  "startTime": "07:00",
  "endTime": "07:30",
  "note": "晨跑"
}

Response:
{
  "success": true,
  "data": { ExerciseRecord对象 }
}
```

### 7.2 获取运动历史
```
GET /api/exercise/{userId}?days=30

Response:
{
  "success": true,
  "data": [ ExerciseRecord, ... ]
}
```

---

## 八、饮食记录接口 (Diet)

### 8.1 添加饮食记录
```
POST /api/diet
Content-Type: application/json

Request:
{
  "userId": 13,
  "mealType": "lunch",  // breakfast/lunch/dinner/snack
  "foods": "鸡胸肉150g, 西兰花200g, 米饭100g",
  "calories": 400,
  "recordDate": "2026-05-10",
  "note": "午餐"
}

Response:
{
  "success": true,
  "data": { DietRecord对象 }
}
```

### 8.2 获取饮食历史
```
GET /api/diet/{userId}?days=7

Response:
{
  "success": true,
  "data": [ DietRecord, ... ]
}
```

---

## 数据模型

### User (用户)
```json
{
  "id": 13,
  "openid": "guest_xxx",
  "nickname": "测试用户",
  "avatar": "头像URL",
  "gender": 2,
  "age": 25,
  "height": 165.0,
  "initialWeight": 70.0,
  "targetWeight": 60.0,
  "weightLossPeriod": 90,
  "startWeightDate": "2026-01-01",
  "basicMetabolism": 1345.0,
  "userType": "weight_loss",
  "constitutionTags": "易水肿,代谢低",
  "exerciseFrequency": 3,
  "exercisePreference": "居家",
  "fitnessLevel": "新手",
  "hasKneeIssue": false,
  "dietPreference": "清淡",
  "breakfastHabit": true,
  "dietaryTaboo": "海鲜,辛辣",
  "workPressure": 3,
  "waterIntake": 2000,
  "standingHours": 8,
  "targetAreas": "腰,腹,臀",
  "sleepStart": "22:00",
  "sleepEnd": "06:00"
}
```

### WeightRecord (体重记录)
```json
{
  "id": 64,
  "userId": 13,
  "weight": 70.5,
  "recordDate": "2026-05-10",
  "sleepStart": "22:00",
  "sleepEnd": "06:00",
  "note": "备注",
  "createdAt": "2026-05-10T21:55:11"
}
```

### BodyMeasurement (维度记录)
```json
{
  "id": 1,
  "userId": 13,
  "recordDate": "2026-05-10",
  "waist": 80.0,
  "hip": 95.0,
  "chest": 90.0,
  "upperArm": 30.0,
  "forearm": 25.0,
  "thigh": 55.0,
  "calf": 38.0
}
```

---

## 错误码

| 错误信息 | 说明 |
|---------|------|
| 设备ID不能为空 | deviceId未提供 |
| 用户不存在 | 用户ID无效 |
| 体重不能为空 | weight未提供 |
| token无效或已过期 | Token过期或无效 |
| 未登录 | 未提供Authorization header |

---

## 微信模板消息ID (待配置)

在微信公众平台申请模板后，配置到 `WechatTemplateMessageService.java`:

```java
private static final String TEMPLATE_ID_MORNING = "晨间问候模板ID";
private static final String TEMPLATE_ID_WATER = "饮水提醒模板ID";
private static final String TEMPLATE_ID_EXERCISE = "运动提醒模板ID";
private static final String TEMPLATE_ID_SUMMARY = "晚间复盘模板ID";
private static final String TEMPLATE_ID_WEEKLY = "周报模板ID";
private static final String TEMPLATE_ID_MONTHLY = "月报模板ID";
private static final String TEMPLATE_ID_SEDENTARY = "久坐提醒模板ID";
private static final String TEMPLATE_ID_EMOTION = "情绪安抚模板ID";
```