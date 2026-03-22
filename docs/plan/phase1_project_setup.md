# Phase 1: 项目搭建与依赖配置

**阶段**: Phase 1  
**日期**: Day 1  
**目标**: 创建可编译的 Android 项目框架

---

## 1.1 阶段目标

- 创建 Android 项目结构
- 配置 Gradle 依赖
- 确保项目可编译运行

## 1.2 交付物

- 可编译的空项目
- 基础架构代码框架

## 1.3 详细任务

### 1.3.1 创建项目结构

```
NeonatalDiary/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/example/neonataldiary/
│           └── NeonatalDiaryApp.kt
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

### 1.3.2 配置 build.gradle.kts (项目级)

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
}
```

### 1.3.3 配置 build.gradle.kts (模块级)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.neonataldiary"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.neonataldiary"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-video:2.5.0")
}
```

### 1.3.4 配置 AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.neonataldiary">

    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />

    <application
        android:name=".NeonatalDiaryApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.NeonatalDiary">
        <activity
            android:name=".ui.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### 1.3.5 创建 Application 类

```kotlin
@HiltAndroidApp
class NeonatalDiaryApp : Application()
```

### 1.3.6 创建 MainActivity

```kotlin
@Composable
fun NeonatalDiaryApp() {
    MaterialTheme {
        Surface {
            // 占位，后续添加导航
            Text("NeonatalDiary")
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeonatalDiaryApp()
        }
    }
}
```

### 1.3.7 配置资源文件

**strings.xml**
```xml
<resources>
    <string name="app_name">新生儿日记</string>
</resources>
```

**themes.xml**
```xml
<resources>
    <style name="Theme.NeonatalDiary" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

## 1.4 验收标准

- [ ] 项目可编译通过
- [ ] 可在模拟器/真机上运行
- [ ] 显示空白应用界面

## 1.5 风险与注意事项

- 确保 Kotlin 与 Compose 版本兼容
- Gradle 与 AGP 版本需匹配
- Hilt 注解处理器配置正确

---

**阶段状态**: 待执行
