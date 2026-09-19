# Survival Aid

英文版：[README.en.md](README.en.md)

Carpet 拓展，给生存服务器加了一批实用规则。需要 Carpet，Fabric 环境。
一个仓库 11 个版本，每个版本一个目录，各自独立构建。

## 支持版本

全是正式版，共 11 个：

| 系列 | 数量 | 版本（同目录名） |
|---|---|---|
| 1.21.x | 9 | 1.21、1.21.1、1.21.2、1.21.3、1.21.4、1.21.5、1.21.6、1.21.10、1.21.11 |
| 26.x | 2 | 26.1.2、26.2 |

## 规则（共 35 条，/carpet 里分类叫 survival_aid）

玩家保护，13 条：
- survivalAidCreeperGriefingControl：苦力怕爆炸照旧造成伤害和击退，就是不再炸方块
- survivalAidDeathCoordinateMessage：死亡时把维度和坐标发给本人
- survivalAidVoidPlayerRescue：掉进虚空自动穿上鞘翅、放烟花火箭救回来
- survivalAidVoidPlayerRescueY：掉到这条 Y 以下触发救援
- survivalAidVoidPlayerRescueCooldown：救援冷却，tick 计，20 = 1 秒
- survivalAidLowDurabilityWarning：主手耐久到这个值就提醒，设 0 关闭
- survivalAidLowHealthGlow：血量到这个阈值自动发光
- survivalAidLowHealthGlowThreshold：发光用的血量阈值
- survivalAidPreventToolBreak：耐久快没了就自动停手，工具不会用坏
- survivalAidPreventToolBreakThreshold：停手用的耐久阈值
- survivalAidNoItemDespawn：掉落物不再自然消失
- survivalAidNetherPortalSolid：1.21.5 把地狱门传送判定缩成了中间那根柱子，很难传，这条恢复完整方块判定（只在 1.21.5 生效）
- survivalAidZombieFrightenGolem：原版要村民最近睡过觉才允许生成铁傀儡，这条豁免睡眠要求，僵尸恐吓（村民恐慌）期间满足人数就能一直生傀儡

性能优化，4 条：
- survivalAidInstantItemPickup：掉落物碰到就捡，0 tick
- survivalAidInstantHopper：漏斗每个 tick 都传，0 tick 冷却
- survivalAidNoCrammingEntities：名单里的实体不吃挤压伤害
- survivalAidStackingOptimizedEntities：名单里的同种实体叠在一起时跳过互推计算

玩法调整，9 条：
- survivalAidAutoEatFood：饥饿值低了自动吃，只吃普通食物，药水、牛奶、蜂蜜瓶、紫颂果、迷之炖煲、附魔金苹果都不吃
- survivalAidAutoEatFoodThreshold：自动进食的饥饿值阈值
- survivalAidAutoEatFoodDebug：每秒输出一条自动进食为什么吃/不吃的日志，排查突然不吃的时候用
- survivalAidDisableVillageCatSpawn：村庄占满 5 张床不再自然刷猫，女巫小屋的猫不受影响
- survivalAidTntLikeBlocks：方块被红石、打火石、火焰弹触发后像 TNT 一样炸
- survivalAidNoEndermanGriefing：末影人不能再搬方块、放方块
- survivalAidVisitorPlayers：名单里的玩家不能破坏、放置、使用方块，也不能跟实体交互
- survivalAidVisitorNoItemPickup：访客不能捡掉落物
- survivalAidVillagerInstantLevelUp：交易时经验条满了立刻升级并当场刷新交易窗口，不用等关窗后的延迟（对应 MC-310787）

拾取白名单，3 条：
- survivalAidItemPickupFilter：全局拾取白名单，逗号分隔物品 ID，设 none 就是全放行
- survivalAidPlayerItemPickupWhitelist：给某个玩家限制只能捡白名单里的东西，用 /survivalaid pickup 管
- survivalAidPlayerItemPickupWhitelistDebug：玩家碰到掉落物时输出检测结果，配白名单排查用

假人工具，3 条：
- survivalAidBotMinecartPreserve：假人下线或被 kill 前先自动下车，矿车不会消失也不会刷出第二辆
- survivalAidFakePlayerItemSearch：开了才能用 /survivalaid searchitem 搜哪个假人身上有指定物品
- survivalAidFakePlayerScanAll：离线假人的识别方式。关着用 survivalaid_fake 标签认假人并打标签，开着直接扫全部离线玩家数据

投影工具，2 条：
- survivalAidProjectionFill：开了才能用 /survivalaid fill。读服务器 schematics/ 目录下唯一的 .litematic 投影，从执行者背包拿对应数量的方块，填进玩家周围 4 格内的所有容器
- survivalAidFillExcludeBlocks：fill 时跳过的物品 id，逗号分隔，可省 minecraft: 前缀，默认排除 blue_ice

其他，1 条：
- survivalAidWorkstationHighLight：潜行右键村民，高亮它绑定的工作站

## 命令（权限等级 2）

    /survivalaid pickup allow <item> <player>  给玩家加一条拾取白名单
    /survivalaid pickup deny <item> <player>   删玩家的一条拾取规则
    /survivalaid pickup clear [item]           不带参数清全部，带参数只清那个物品
    /survivalaid pickup list                   看当前所有拾取规则
    /survivalaid searchitem <item>             搜哪个假人带着这个物品，物品 ID 或中文名都行
    /survivalaid fill                          按当前选中的 Litematica 投影填玩家周围的容器

## 翻译

玩家能看到的文字全在语言文件里，代码里没有写死的消息：

    <版本目录>/src/main/resources/assets/survival_aid/lang/
    ├── zh_cn.json
    └── en_us.json

键前缀：
- survival_aid.message.* 运行时消息
- survival_aid.cmd.* 命令反馈
- survival_aid.msg.* 调试
- survival_aid.rule.<规则名>.name / .desc 规则名和描述

规则名/描述另外还有一份 carpet.rule.<规则名>.name / .desc 的拷贝，不是冗余：老版本的 Carpet（比如 1.4.147）查的是 carpet.rule. 前缀，新版本查的是 survival_aid.rule.（注册名）前缀，两套都得留，否则老 Carpet 启动直接 NPE。

规则翻译还有一条兜底通道：SurvivalAidTranslations 启动时把 jar 里的语言文件读进内存，通过 Carpet 扩展接口 canHasTranslations 提供给 Carpet。规则名/描述以这份内存表为准，不依赖游戏内的语言文件加载链（被其他 mod 接管时也能正常显示）。所以语言文件是唯一的数据源：改文案只改 json，两套键和内存表都跟着变。

加新语言就往同目录丢一份按语言代码命名的 json，比如 ja_jp.json。

## 构建

每个版本目录是独立的 Loom 工程，要 JDK 21：

    cd 1.21.11
    bash gradlew build

产物在 build/libs/。每个版本第一次构建会下载对应版本的 Minecraft 和映射，之后就走缓存。

说明：源码是从发布 jar 反编译（JADX）出来整理的，类头上那些 JADX 注释是正常的。
