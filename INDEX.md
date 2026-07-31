# SurvivalAid 项目索引

作者: Rain_Clay · License: MIT
Fabric Carpet 扩展模组，支持 MC 1.21 → 1.21.11 + 26.1.2

---

## 目录结构

```
survival_aid/
├── gradlew / gradlew.bat / gradle/     # Gradle Wrapper (Gradle 9.5.1)
├── build_all.sh                         # 一键构建所有版本脚本
├── settings.gradle                      # 根项目 settings（未包含子项目）
├── gradle.properties                    # 根项目属性（未使用）
├── INDEX.md                             # 本文件
├── LICENSE / README.md
│
├── 1.21/          ✅ 已配置 wrapper 和 build.gradle（fabric-loom-remap）
├── 1.21.1/        ❌ 旧版 fabric-loom，缺 wrapper
├── 1.21.2/        ❌ 同上
├── 1.21.3/        ❌ 同上
├── 1.21.4/        ❌ 同上
├── 1.21.5/        ❌ 同上
├── 1.21.6/        ❌ 同上
├── 1.21.10/       ❌ 同上
├── 1.21.11/       ❌ 同上
├── 26.1.2/        ❌ 同上（Java 25）
│
├── build/                               # 构建输出目录
├── inspect_loom/                        # Loom 扩展检测项目
├── inspect.gradle                       # 检测脚本
└── inspect_loom.gradle
```

## 各版本配置总览

| 版本 | MC版本 | Yarn映射 | Loader | Fabric API | Carpet | Java |
|------|--------|----------|--------|------------|--------|------|
| 1.21 | 1.21 | 1.21+build.9 | 0.16.0 | 0.100.0+1.21 | 1.21-1.4.147+v240613 | 21 |
| 1.21.1 | 1.21.1 | 1.21.1+build.9 | 0.16.0 | 0.100.1+1.21.1 | 1.21-1.4.147+v240613 | 21 |
| 1.21.2 | 1.21.2 | 1.21.2+build.7 | 0.16.0 | 0.102.0+1.21.2 | 1.21.2-1.4.158+v241022 | 21 |
| 1.21.3 | 1.21.3 | 1.21.3+build.5 | 0.16.0 | 0.106.0+1.21.3 | 1.21.2-1.4.158+v241022 | 21 |
| 1.21.4 | 1.21.4 | 1.21.4+build.8 | 0.16.0 | 0.108.0+1.21.4 | 1.21.4-1.4.161+v241203 | 21 |
| 1.21.5 | 1.21.5 | 1.21.5+build.6 | 0.16.0 | 0.115.0+1.21.5 | 1.21.5-1.4.169+v250325 | 21 |
| 1.21.6 | 1.21.6 | 1.21.6+build.5 | 0.16.0 | 0.115.1+1.21.6 | 1.21.6-1.4.176+v250617 | 21 |
| 1.21.10 | 1.21.10 | 1.21.10+build.3 | 0.16.0 | 0.115.3+1.21.10 | 1.21.10-1.4.186+v251009 | 21 |
| 1.21.11 | 1.21.11 | 1.21.11+build.1 | 0.16.0 | 0.115.5+1.21.11 | 1.21.11-1.4.193+v251211 | 21 |
| 26.1.2 | 26.1.2 | 26.1.2+build.7 | **0.19.3** | 0.155.2+26.1.2 | 26.1+v260401 | **25** |

## 源码结构（每个版本相同）

```
src/main/java/com/survivalaid/
├── SurvivalAidCommands.java        # Carpet 命令注册
├── SurvivalAidExtension.java       # Carpet 扩展入口
├── SurvivalAidRuleCategories.java  # 规则分类
├── SurvivalAidRules.java           # Carpet 规则定义
├── SurvivalAidRuntime.java         # 运行时逻辑
├── SurvivalAidSettings.java        # 设置
├── SurvivalAidTntLikeBlocks.java   # TNT 类方块逻辑
├── SurvivalAidToolProtection.java  # 工具保护逻辑
├── SurvivalAidTranslations.java    # 翻译
├── SurvivalAidVisitors.java        # 访客逻辑
├── features/                       # 功能模块
└── mixin/                          # 19 个 Mixin 类
```

## 构建状态

### 已解决
- ✅ 插件兼容 Gradle 9.5.1 → 使用 `fabric-loom-remap` 1.17-SNAPSHOT
- ✅ Access widener 命名空间冲突 → `fabric-loom-remap` 自动处理
- ✅ Carpet Maven 依赖 → `carpet:fabric-carpet:VERSION`
- ✅ mappings 配置 → `mappings "net.fabricmc:yarn:${yarn_mappings}:v2"`

### 当前问题
- ❌ **Intermediary 命名编译** — 源码使用 intermediary 名 (`class_1297` 等)，但 Loom 把 Yarn 映射后的 jar 放在编译类路径上。`useIntermediateMappings = true` 在 `fabric-loom-remap` 上未生效
- ❌ **其他 9 个版本** — 仍使用旧版 `fabric-loom` 插件，需要统一更新为 `fabric-loom-remap` + 添加 gradle wrapper
- ❌ **Java 25 (26.1.2)** — 需要确认 Gradle/Loom 兼容性

### 待办事项
1. 修复 intermediary 编译（让 intermediary jar 进入编译类路径）— 进行中
2. 将修复后的 build.gradle 模板同步到所有 10 个版本
3. 为每个版本复制 gradlew/gradle wrapper
4. 测试所有版本构建
5. 创建 uber-jar（合并各版本 jar 到 META-INF/jars/）

## 参考路径

### 源代码反编译目录
`/storage/emulated/0/ide/survivalaid-src-mc*` (每个版本对应一个目录)

### 原始 jar
`/storage/emulated/0/ide/SurvivalAid-all-versions.jar`
`/storage/emulated/0/ide/SurvivalAid-extracted/META-INF/jars/` (各版本独立 jar)

### Gradle 缓存
- Loom 缓存: `.gradle/loom-cache/`
- 全局缓存: `~/.gradle/caches/fabric-loom/`
- Intermediary jar: `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-intermediary/`
