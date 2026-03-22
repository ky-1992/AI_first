# 新生儿日记 Android 应用 - 软件设计文档 (SDD)

**版本**: 1.0.0  
**日期**: 2024-01-15  
**状态**: 已归档

---

## 目录

1. [引言](#1-引言)
2. [系统概述](#2-系统概述)
3. [功能规格](#3-功能规格)
4. [数据设计](#4-数据设计)
5. [界面设计](#5-界面设计)
6. [接口设计](#6-接口设计)
7. [系统架构](#7-系统架构)
8. [存储方案](#8-存储方案)
9. [错误处理](#9-错误处理)
10. [安全与隐私](#10-安全与隐私)
11. [测试要求](#11-测试要求)
12. [项目里程碑](#12-项目里程碑)
13. [附录](#13-附录)

---

## 1. 引言

### 1.1 目的

本文档详细描述新生儿日记 (NeonatalDiary) Android 应用的设计规格，作为开发团队的实现指南和项目交接文档。

### 1.2 范围

- 记录新生儿每日基本情况（喂养、情绪、体重等）
- 支持图片/视频附件
- 单宝宝本地存储优先

### 1.3 定义

| 术语 | 定义 |
|-----|------|
| MVP | 最小可行产品 (Minimum Viable Product) |
| SDD | 软件设计文档 (Software Design Document) |
| Room | Android 本地数据库框架 |
| Compose | Jetpack Compose UI 框架 |

### 1.4 参考文档

- Android Developers 官方文档
- Jetpack Compose 官方指南
- Room 数据库文档

---

## 2. 系统概述

### 2.1 项目信息

| 项目名称 | NeonatalDiary |
|---------|---------------|
| 项目类型 | Android 原生应用 |
| 目标用户 | 新生儿父母/照护者 |
| 核心功能 | 记录新生儿每日基本情况，支持图片/视频附件 |
| 目标版本 | 1.0.0 (MVP) |
| 最低支持 | Android 8.0 (API 26) |

### 2.2 目标与约束

#### 目标
- 提供简洁易用的日记记录界面
- 确保数据本地化存储，保护用户隐私
- 支持多媒体附件（图片/视频）

#### 约束
- 完全离线运行，无网络依赖
- 数据存储在应用私有目录
- 遵循 Android 权限规范

### 2.3 用户特征

- 主要用户：新生儿父母或照护者
- 技术水平：普通智能手机用户
- 使用场景：日常记录，时间碎片化

---

## 3. 功能规格

### 3.1 核心功能 (MVP)

| 功能ID | 功能名称 | 描述 | 优先级 |
|-------|---------|------|-------|
| F01 | 日记列表 | 按日期倒序展示所有日记 | P0 |
| F02 | 新增日记 | 创建日期+多字段+媒体附件 | P0 |
| F03 | 查看详情 | 展示日记完整信息与媒体 | P0 |
| F04 | 媒体拍摄 | 调用相机拍照 | P0 |
| F05 | 媒体选择 | 从相册选取图片/视频 | P0 |
| F06 | 删除日记 | 删除日记及关联媒体 | P1 |
| F07 | 日期筛选 | 按日期筛选日记 | P2 |

### 3.2 功能详细描述

#### F01: 日记列表
- 显示所有日记，按日期倒序排列
- 每条日记显示：日期、摘要（喂养/情绪/体重）、媒体缩略图数量
- 支持点击进入详情页
- 空状态显示引导提示

#### F02: 新增日记
- 日期选择器（默认当天）
- 喂养情况：多行文本输入
- 情绪状态：多行文本输入
- 体重：数字输入，单位 kg
- 备注：多行文本输入
- 媒体添加：支持拍照和从相册选择

#### F03: 查看详情
- 完整显示所有日记字段
- 媒体网格展示（最多3x3）
- 点击媒体全屏查看
- 支持编辑和删除操作

#### F04-F05: 媒体处理
- 拍照：调用系统相机
- 选图：从相册选择图片
- 选视频：从相册选择视频
- 支持多选
- 自动压缩优化存储

#### F06: 删除日记
- 确认对话框
- 级联删除关联媒体文件

---

## 4. 数据设计

### 4.1 实体关系图

```
┌─────────────────┐       1:N       ┌─────────────────────┐
│   DailyLog      │◄────────────────│  MediaAttachment    │
├─────────────────┤                 ├─────────────────────┤
│ id: Long (PK)   │                 │ id: Long (PK)       │
│ date: String    │                 │ logId: Long (FK)    │
│ feeding: String?│                 │ type: String        │
│ mood: String?   │                 │ path: String        │
│ weightKg: Float?│                 │ createdAt: Long     │
│ note: String?   │                 └─────────────────────┘
│ createdAt: Long │
└─────────────────┘
```

### 4.2 数据字典

#### DailyLog (日记)

| 字段名 | 类型 | 可空 | 说明 | 验证规则 |
|-------|------|-----|------|---------|
| id | Long | 否 | 主键，自增 | 自动生成 |
| date | String | 否 | 日期，格式 YYYY-MM-DD | 必填，有效日期 |
| feeding | String | 是 | 喂养情况描述 | 最大500字符 |
| mood | String | 是 | 情绪状态描述 | 最大500字符 |
| weightKg | Float | 是 | 体重，单位 kg | 正数，0.1-30范围 |
| note | String | 是 | 备注信息 | 最大2000字符 |
| createdAt | Long | 否 | 创建时间戳 | 自动生成 |

#### MediaAttachment (媒体附件)

| 字段名 | 类型 | 可空 | 说明 | 验证规则 |
|-------|------|-----|------|---------|
| id | Long | 否 | 主键，自增 | 自动生成 |
| logId | Long | 否 | 外键，关联 DailyLog | 必填，引用存在记录 |
| type | String | 否 | 类型: "image" 或 "video" | 枚举值 |
| path | String | 否 | 媒体文件绝对路径 | 必填，文件存在 |
| createdAt | Long | 否 | 创建时间戳 | 自动生成 |

### 4.3 索引设计

| 表名 | 索引字段 | 类型 | 用途 |
|-----|---------|-----|------|
| daily_logs | date | 普通索引 | 按日期排序查询 |
| daily_logs | createdAt | 普通索引 | 按创建时间排序 |
| media_attachments | logId | 普通索引 | 按日记查询媒体 |

### 4.4 约束设计

- MediaAttachment.logId 外键约束，级联删除
- date 字段唯一性约束（单宝宝场景下每天一条记录）

---

## 5. 界面设计

### 5.1 屏幕结构

```
MainActivity
└── NavHost
    ├── DiaryListScreen (首页/列表)
    ├── DiaryEntryScreen (新增日记)
    ├── DiaryDetailScreen (日记详情)
    └── MediaViewerScreen (媒体全屏查看)
```

### 5.2 导航流程

```
┌──────────────┐     ┌────────────────┐
│  DiaryList   │────►│ DiaryEntry     │
│  (首页列表)   │     │ (新增日记)      │
└──────┬───────┘     └───────┬────────┘
       │                      │
       ▼                      ▼
┌──────────────┐     ┌────────────────┐
│ DiaryDetail  │     │ 返回列表        │
│ (日记详情)    │     │ (自动刷新)      │
└──────┬───────┘     └────────────────┘
       │
       ▼
┌──────────────┐
│ MediaViewer  │
│ (媒体查看)    │
└──────────────┘
```

### 5.3 UI 组件设计

#### 5.3.1 日记卡片组件

```
┌────────────────────────────────────┐
│ 📅 2024-01-15                       │
├────────────────────────────────────┤
│ 🍽 喂养: 母乳 5次, 配方奶 2次        │
│ 😊 状态: 活泼好动                    │
│ ⚖️ 体重: 3.5kg                      │
├────────────────────────────────────┤
│ [图片缩略] [图片缩略] [视频缩略]      │
└────────────────────────────────────┘
```

#### 5.3.2 新增日记表单

| 字段 | 类型 | 必填 | 输入方式 | 键盘类型 |
|-----|------|-----|---------|---------|
| 日期 | Date | 是 | 日期选择器 | - |
| 喂养情况 | Text | 否 | 多行文本输入 | 默认 |
| 情绪状态 | Text | 否 | 多行文本输入 | 默认 |
| 体重 | Decimal | 否 | 单行输入 | 数字+小数点 |
| 备注 | Text | 否 | 多行文本输入 | 默认 |
| 媒体 | Media[] | 否 | 图片/视频选择器 | - |

### 5.4 视觉规范

| 元素 | 规格 |
|-----|------|
| 主色调 | #FFB6C1 (浅粉色，温馨感) |
| 辅助色 | #87CEEB (天蓝色) |
| 强调色 | #FF69B4 (深粉色) |
| 背景色 | #FFF5F7 (淡粉白) |
| 文字色 | #333333 (深灰) |
| 次要文字 | #666666 (中灰) |
| 圆角 | 12dp (卡片)，8dp (按钮) |
| 阴影 | elevation 4dp (卡片) |
| 间距基准 | 8dp |
| 媒体网格 | 3列，间距4dp |

### 5.5 组件状态

| 组件 | 默认态 | 按下态 | 禁用态 | 加载态 |
|-----|-------|-------|-------|-------|
| 按钮 | 主题色背景 | 90%透明度 | 50%透明度 | 显示进度条 |
| 卡片 | elevation 4dp | elevation 8dp | 灰度显示 | 显示骨架屏 |
| 输入框 | 边框 #CCCCCC | 边框主题色 | 背景灰色 | - |

---

## 6. 接口设计

### 6.1 用户接口

#### 6.1.1 主屏幕 (DiaryListScreen)

| 元素 | 操作 | 结果 |
|-----|------|------|
| FAB (+) | 点击 | 跳转新增日记页面 |
| 日记卡片 | 点击 | 跳转日记详情页 |
| 日记卡片 | 长按 | 显示删除选项 |

#### 6.1.2 新增日记页面 (DiaryEntryScreen)

| 元素 | 操作 | 结果 |
|-----|------|------|
| 日期选择器 | 点击 | 打开日期选择对话框 |
| 添加图片按钮 | 点击 | 打开图片选择器 |
| 添加视频按钮 | 点击 | 打开视频选择器 |
| 保存按钮 | 点击 | 验证并保存日记 |
| 返回按钮 | 点击 | 返回列表页 |

### 6.2 内部接口

#### 6.2.1 Repository 接口

```kotlin
interface DiaryRepository {
    fun getAllDiariesWithMedia(): Flow<List<DiaryWithMedia>>
    fun getDiaryWithMedia(id: Long): Flow<DiaryWithMedia?>
    suspend fun insertDiary(log: DailyLog): Long
    suspend fun insertMedia(media: MediaAttachment): Long
    suspend fun deleteDiary(log: DailyLog)
    suspend fun deleteMedia(media: MediaAttachment)
}
```

#### 6.2.2 MediaHelper 接口

```kotlin
class MediaHelper(private val context: Context) {
    fun saveImage(uri: Uri): String?
    fun saveVideo(uri: Uri): String?
    fun deleteFile(path: String): Boolean
    fun getThumbnail(path: String): Bitmap?
}
```

### 6.3 外部接口

#### 6.3.1 系统相机

- 使用 Activity Result API
- 请求码: REQUEST_IMAGE_CAPTURE / REQUEST_VIDEO_CAPTURE

#### 6.3.2 系统相册

- 使用 Activity Result API
- 请求码: REQUEST_PICK_IMAGE / REQUEST_PICK_VIDEO

---

## 7. 系统架构

### 7.1 架构分层

```
┌─────────────────────────────────────┐
│           UI Layer (Compose)        │
│   Screens, Components, ViewModels   │
├─────────────────────────────────────┤
│         Domain Layer                │
│      Use Cases, Repository          │
├─────────────────────────────────────┤
│          Data Layer                 │
│   Room Database, Media Storage      │
└─────────────────────────────────────┘
```

### 7.2 技术栈

| 层级 | 技术选型 | 版本 |
|-----|---------|------|
| 语言 | Kotlin | 1.9.x |
| UI框架 | Jetpack Compose | BOM 2024.02 |
| 架构模式 | MVVM + Clean Architecture | - |
| 本地数据库 | Room | 2.6.1 |
| 依赖注入 | Hilt | 2.50 |
| 异步处理 | Kotlin Coroutines + Flow | 1.7.x |
| 图片加载 | Coil | 2.5.0 |
| 导航 | Navigation Compose | 2.7.7 |
| 状态管理 | ViewModel + StateFlow | 2.7.0 |

### 7.3 模块划分

| 模块 | 职责 |
|-----|------|
| data/local/entity | Room 实体定义 |
| data/local/dao | Data Access Object |
| data/local | Database 实现 |
| data/repository | 数据仓库实现 |
| domain/model | 领域模型 |
| ui/screen | Compose 页面 |
| ui/component | 可复用组件 |
| ui/viewmodel | ViewModel |
| util | 工具类 |

### 7.4 依赖关系

```
ViewModel → Repository → DAO → Database
                ↓
           MediaHelper
```

---

## 8. 存储方案

### 8.1 数据库存储

| 数据库 | 版本 | 表数量 | 迁移策略 |
|-------|-----|-------|---------|
| AppDatabase | 1 | 2 | 自动迁移 |

### 8.2 文件存储

| 存储类型 | 路径 | 说明 |
|---------|------|------|
| 媒体文件 | /files/media/ | 应用私有目录 |
| 数据库 | Room 默认位置 | SQLite |

### 8.3 存储策略

- **图片格式**: JPEG/PNG
- **视频格式**: MP4 (H.264)
- **命名规则**: `{type}_{timestamp}.{ext}`
  - 示例: `img_1705312800000.jpg`, `vid_1705312801000.mp4`
- **文件大小限制**:
  - 图片: 单张 ≤ 10MB
  - 视频: 单个 ≤ 100MB

### 8.4 媒体处理流程

```
┌─────────────┐    ┌─────────────┐    ┌────────────────┐
│  相机拍摄    │    │  相册选择    │    │   其他来源      │
└──────┬──────┘    └──────┬──────┘    └───────┬────────┘
       │                   │                    │
       ▼                   ▼                    ▼
┌─────────────────────────────────────────────────────┐
│              MediaHelper.saveMedia()               │
│   - 复制到应用私有目录                               │
│   - 生成唯一文件名                                  │
│   - 返回保存后的文件路径                            │
└─────────────────────────┬───────────────────────────┘
                          │
                          ▼
                 ┌────────────────┐
                 │  数据库记录     │
                 │ MediaAttachment│
                 └────────────────┘
```

---

## 9. 错误处理

### 9.1 错误场景与处理

| 场景 | 处理方式 | 用户提示 |
|-----|---------|---------|
| 数据库写入失败 | 显示 Toast，保留用户输入 | "保存失败，请重试" |
| 媒体保存失败 | 显示错误提示，允许重试 | "媒体保存失败" |
| 权限被拒绝 | 显示引导去设置页面的提示 | "需要相机权限，请在设置中开启" |
| 文件不存在 | 从数据库删除对应记录 | (静默处理) |
| 存储空间不足 | 提示用户清理空间 | "存储空间不足，请清理" |
| 无效日期 | 表单验证失败 | "请选择有效日期" |
| 体重超出范围 | 表单验证失败 | "体重应在 0.1-30kg 之间" |

### 9.2 异常日志

- 使用 Timber 或 Android Log
- 仅在 Debug 版本输出详细日志
- 敏感信息脱敏处理

---

## 10. 安全与隐私

### 10.1 权限管理

| 权限 | 用途 | 请求时机 |
|-----|------|---------|
| CAMERA | 拍照 | 首次拍照前 |
| READ_MEDIA_IMAGES | 读取图片 | Android 13+ |
| READ_MEDIA_VIDEO | 读取视频 | Android 13+ |
| READ_EXTERNAL_STORAGE | 读取存储 | Android 12 及以下 |

### 10.2 权限请求流程

```
启动应用
    │
    ▼
检测所需权限
    │
    ├── 已授权 → 正常使用
    │
    └── 未授权 → 显示权限说明弹窗
                      │
                      ▼
              用户授权/拒绝
              │           │
              ▼           ▼
          继续使用    提示功能受限
```

### 10.3 数据安全

- 数据存储在应用私有目录
- 默认离线运行，无网络传输
- 未来可选：数据库加密 (SQLCipher)

---

## 11. 测试要求

### 11.1 测试覆盖

| 测试类型 | 覆盖范围 | 目标覆盖率 |
|---------|---------|----------|
| 单元测试 | DAO, Repository, ViewModel | 80% |
| 集成测试 | 数据库读写流程 | 100% |
| UI 测试 | 核心用户流程 | 关键路径 |

### 11.2 关键测试用例

| 用例ID | 描述 | 预期结果 |
|-------|------|---------|
| TC01 | 创建日记并保存 | 日记成功保存，可从列表查看 |
| TC02 | 添加图片附件 | 图片保存到私有目录，数据库有记录 |
| TC03 | 添加视频附件 | 视频保存到私有目录，数据库有记录 |
| TC04 | 查看日记详情 | 正确显示所有字段和媒体 |
| TC05 | 删除日记及媒体 | 日记和媒体文件同时删除 |
| TC06 | 权限正常流程 | 授权后正常使用 |
| TC07 | 权限拒绝流程 | 提示并引导去设置 |
| TC08 | 日期验证 | 无效日期提示错误 |
| TC09 | 体重范围验证 | 超出范围提示错误 |

### 11.3 兼容性测试

| 测试项 | 覆盖范围 |
|-------|---------|
| Android 版本 | 8.0, 10, 12, 13, 14 |
| 屏幕尺寸 | 手机 (小/中/大)，平板 |
| 分辨率 | hdpi, xhdpi, xxhdpi |

---

## 12. 项目里程碑

| 阶段 | 内容 | 目标时间 | 交付物 |
|-----|------|---------|-------|
| Phase 1 | 项目搭建，依赖配置 | Day 1 | 可编译的空项目 |
| Phase 2 | 数据层实现 (Entity, DAO, DB) | Day 2-3 | 数据库层代码 |
| Phase 3 | Repository & ViewModel | Day 4 | 业务逻辑层 |
| Phase 4 | 媒体存储工具 | Day 5 | MediaHelper |
| Phase 5 | UI 界面开发 | Day 6-8 | Compose 页面 |
| Phase 6 | 集成测试与调优 | Day 9-10 | 可发布版本 |

---

## 13. 附录

### 13.1 项目文件结构

```
NeonatalDiary/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/neonataldiary/
│   │   │   ├── NeonatalDiaryApp.kt
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── DailyLog.kt
│   │   │   │   │   │   └── MediaAttachment.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── DiaryDao.kt
│   │   │   │   │   │   └── MediaDao.kt
│   │   │   │   │   └── AppDatabase.kt
│   │   │   │   └── repository/
│   │   │   │       └── DiaryRepository.kt
│   │   │   ├── di/
│   │   │   │   └── AppModule.kt
│   │   │   ├── domain/
│   │   │   │   └── model/
│   │   │   │       └── DiaryWithMedia.kt
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   ├── navigation/
│   │   │   │   │   └── NavGraph.kt
│   │   │   │   ├── screen/
│   │   │   │   │   ├── DiaryListScreen.kt
│   │   │   │   │   ├── DiaryEntryScreen.kt
│   │   │   │   │   ├── DiaryDetailScreen.kt
│   │   │   │   │   └── MediaViewerScreen.kt
│   │   │   │   ├── component/
│   │   │   │   │   ├── DiaryCard.kt
│   │   │   │   │   ├── MediaPicker.kt
│   │   │   │   │   └── DatePicker.kt
│   │   │   │   └── viewmodel/
│   │   │   │       └── DiaryViewModel.kt
│   │   │   └── util/
│   │   │       └── MediaHelper.kt
│   │   └── res/
│   │       ├── values/
│   │       │   ├── colors.xml
│   │       │   ├── strings.xml
│   │       │   └── themes.xml
│   │       └── drawable/
│   │           └── ic_launcher_foreground.xml
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

### 13.2 扩展性预留

#### 13.2.1 多宝宝支持

如需支持多宝宝，需新增 BabyProfile 表：

```kotlin
@Entity(tableName = "baby_profiles")
data class BabyProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val birthDate: String,
    val gender: String? = null
)
```

DailyLog 表需增加 babyId 外键。

#### 13.2.2 云端备份

预留字段设计：

```kotlin
data class SyncInfo(
    val lastSyncTime: Long,
    val syncStatus: SyncStatus,
    val remoteId: String?
)
```

---

**文档结束**
