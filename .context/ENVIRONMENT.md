# 在 macOS 上初始化 Flutter 开发环境

> updated_by: Cascade - Claude-Sonnet-4.5
> updated_at: 2026-04-17 22:46:00

---

## Java 环境与编译目标

- Mac 默认 Java：Amazon Corretto 17。
- Android Studio 与其 Gradle 构建使用内置 JBR 21。
- 项目 `app`、`deviceinfo` 模块的 Java `sourceCompatibility` / `targetCompatibility` 和 Kotlin `jvmTarget` 均为 17。

---

## Android 开发工具链

| 组件 | 配置约定 |
|---|---|
| Java / JDK | Mac 默认使用 Amazon Corretto 17；项目 Java / Kotlin 编译目标为 17 |
| Android Studio | 通过 JetBrains Toolbox 管理，使用内置 JBR 21 运行 IDE 和 Gradle |
| Android SDK | 使用 `~/Library/Android/sdk`，通过 Android Studio 的 SDK Manager 管理 |
| Gradle | 使用项目根目录的 Gradle Wrapper，当前版本为 8.10.2，无需全局安装 |
| Android Gradle Plugin（AGP） | 在项目根目录 `build.gradle.kts` 中声明，当前版本为 8.7.3 |
| Kotlin | 通过 Gradle 插件管理，当前版本为 2.1.0，无需单独安装 |

**Gradle JDK 设置**：

- Android Studio → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK。
- 本项目使用 `GRADLE_LOCAL_JAVA_HOME`，由 `.gradle/config.properties` 中的 `java.home` 指向 Android Studio 内置 JBR 21。
- 命令行运行 Gradle 时使用 `JAVA_HOME`，未设置时使用 PATH 中的 Java；当前 Mac 默认解析到 Corretto 17。

**基础检查**（在项目根目录执行）：

```bash
java -version
javac -version
sh ./gradlew --version
```

Android Studio / SDK 的安装、环境变量及许可配置，以及 Gradle / Maven 镜像配置，见后续对应章节。

---

## Android Studio / Android SDK 环境

### Android Studio（必须由 JetBrains Toolbox 管理）

以当前 Mac 的安装为基准：

- 通过 JetBrains Toolbox 安装和管理；Toolbox 位于 `/Applications/JetBrains Toolbox.app`。
- 当前版本：**Android Studio Narwhal 4 Feature Drop 2025.1.4**（构建号 `AI-251.27812.49.2514.14217341`；`CFBundleShortVersionString` 为 `2025.1`）。
- 当前安装路径：`~/Applications/Android Studio Narwhal 4 Feature Drop 2025.1.4.app`；升级后以 Toolbox 实际安装路径为准。
- 命令行启动使用 `studio`，入口为 `~/Library/Application Support/JetBrains/Toolbox/scripts/studio`，由 Toolbox 生成和维护。
- Toolbox 的 `scripts` 目录已在 PATH 中。
- 内置运行时为 **JBR 21.0.8**；本项目 Gradle 使用该 JDK，Java / Kotlin 编译目标保持 **17**。

### Android SDK

以当前 Mac 的安装为基准，通过 Android Studio 的 SDK Manager 管理：

- SDK 路径：`~/Library/Android/sdk`；项目 `local.properties` 中的 `sdk.dir` 指向同一路径。
- 最低支持版本目标：**Android 8.0 及以上（API 26，`minSdk = 26`）**；`compileSdk = 36`、`targetSdk = 36` 保持不变。
- 当前 `app`、`deviceinfo` 代码仍为 `minSdk = 21`；本次仅更新文档目标，代码调整由后续 Agent 处理。

| 组件 | 当前已安装 |
|---|---|
| SDK Platforms（API） | 21、22、28、31、33、34、35、36 |
| Build-Tools | 30.0.3、34.0.0、35.0.0、36.0.0 |
| Platform-Tools（adb） | 37.0.0 |
| Command-line Tools | 19.0，位于 `cmdline-tools/19.0/` |
| Android Emulator | 36.5.10 |
| 模拟器系统镜像 | API 33、35，Google Play ARM64（`arm64-v8a`） |

**环境变量**（已配置于 `~/.bashrc`）：

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$PATH:$ANDROID_HOME/emulator"
export PATH="$PATH:$ANDROID_HOME/platform-tools"
```

`ANDROID_SDK_ROOT` 未设置。`adb`、`emulator` 已在 PATH 中；`sdkmanager` 使用完整路径调用。

**SDK 许可**：`$ANDROID_HOME/licenses/` 已有许可记录；后续需要检查或接受许可时执行：

```bash
"$ANDROID_HOME/cmdline-tools/19.0/bin/sdkmanager" --licenses
```

---

## Gradle / Maven 环境

以项目内配置为准，通过 Android Studio 完成 Gradle 同步、构建和运行。Gradle 使用 Maven 仓库下载依赖，无需另行安装 Gradle 或 Maven。

### Android Studio 的 Gradle 设置

在 Settings → Build, Execution, Deployment → Build Tools → Gradle 中：

- Gradle 分发选择项目 **Wrapper**，版本由 `gradle/wrapper/gradle-wrapper.properties` 管理。
- Gradle JDK 使用 **GRADLE_LOCAL_JAVA_HOME**，当前由 `.gradle/config.properties` 指向 Android Studio 内置 **JBR 21**；项目编译目标为 **17**。
- 首次同步或下载新依赖时关闭 Gradle Offline Mode，允许联网下载。

### 项目构建配置

以下路径均相对于项目根目录：

| 配置文件 | 当前设置 |
|---|---|
| `gradle/wrapper/gradle-wrapper.properties` | Gradle **8.10.2**，从阿里云下载 `gradle-8.10.2-all.zip` |
| `build.gradle.kts` | AGP **8.7.3**、Kotlin **2.1.0** 及其他构建插件 |
| `gradle.properties` | Gradle JVM 最大堆内存 **2 GB**、UTF-8，以及 Android / Kotlin 构建选项 |

### Maven 仓库配置

| 配置位置 | 当前仓库及用途 |
|---|---|
| `settings.gradle.kts` → `pluginManagement.repositories` | 插件解析：优先阿里云 google / central / gradle-plugin，其后保留 Google、Maven Central、Gradle Plugin Portal |
| `settings.gradle.kts` → `dependencyResolutionManagement.repositories` | 模块依赖：优先阿里云 google / central，其后保留 Google、Maven Central，以及荣耀、华为、JitPack 仓库 |
| `build.gradle.kts` → `buildscript.repositories` | TheRouter 构建插件依赖：阿里云 central、Maven Central |

- 模块依赖仓库采用 `FAIL_ON_PROJECT_REPOS`，统一在 `settings.gradle.kts` 管理，不在模块或 `allprojects` 中重复声明。
- 镜像及仓库地址维护在项目内；当前 Mac 未配置全局 Gradle `gradle.properties` 或初始化脚本。
- Gradle 仓库以以上配置为准，Maven 的 `~/.m2/settings.xml` 不控制本项目的仓库解析。
- 官方源和厂商仓库是当前配置的一部分，下载时可能直连对应站点。

### 在 Android Studio 中验证

1. 打开项目根目录（包含 `settings.gradle.kts` 的目录）。
2. 执行 **Sync Project with Gradle Files**，在 Build 窗口确认同步成功。
3. 在 **Build Variants** 中选择 `devDebug` 或 `prdDebug`，通过 Build 菜单构建。
4. 选择 `app` 运行配置及模拟器或真机，点击 **Run**，确认应用启动。

首次同步会下载 Gradle 和依赖，缓存由 Gradle 管理，无需手动预缓存。
