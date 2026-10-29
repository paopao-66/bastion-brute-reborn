# Bastion Brute Reborn

A Minecraft Fabric mod that adds configurable Ancient Debris drops from Piglin Brutes and respawns brutes near bastion remnants.

Minecraft Fabric 模组：为猪灵蛮兵添加可配置的远古残骸掉落，并在堡垒遗迹附近重生蛮兵。

## Features / 功能

- Extra drops with a base chance plus a Looting bonus, configurable item/count, and player/wolf kill controls. Default chance is 2.5% plus 1% per Looting level, up to level 3.
- Respawn brutes with golden axes, using nearby loaded bastion references, preferred floor blocks and a nearby brute cap. Searches never request chunk loading/generation and stay below the world's logical height limit.
- 可配置物品、数量、抢夺加成及玩家/狼击杀条件的额外掉落。默认掉率为 2.5%，每级抢夺增加 1%，最多计入 3 级。
- 根据附近已加载堡垒引用重生手持金斧的蛮兵，支持偏好地面方块和附近数量上限；搜索不请求加载或生成区块，并受世界逻辑高度限制。

## Installation / 安装

Requires Minecraft **1.21.10**, **Fabric Loader >= 0.19.2**, **Fabric API**, and **Java 21**. Put the mod JAR and the matching Fabric API JAR in your instance/server's `mods/` directory.

需要 Minecraft **1.21.10**、**Fabric Loader >= 0.19.2**、**Fabric API** 和 **Java 21**。将模组 JAR 及匹配的 Fabric API JAR 放入游戏实例或服务端的 `mods/` 目录。

## Configuration / 配置

`config/bastion_brute_reborn.json` is created on first use. Edit it while the game/server is stopped, then restart. Existing malformed files are preserved: errors are logged and defaults are used for that run. Missing fields use defaults; obsolete fields are ignored. Numeric corrections are applied in memory. Probability totals of 1 or more always succeed.

配置文件 `config/bastion_brute_reborn.json` 在首次使用时创建。关闭游戏/服务端后修改，再重启生效。解析失败时记录错误、保留坏文件，本次运行使用默认值；缺少的字段用默认值，旧的多余字段忽略。数值钳制作用于内存配置；总掉率达到或超过 1 时必定成功。

| Name / 名称 | Default / 默认值 | Description / 说明 |
| --- | --- | --- |
| `enableAncientDebrisDrop` | `true` | Enable extra Piglin Brute drops. / 开启蛮兵额外掉落。 |
| `dropItem` | `"minecraft:ancient_debris"` | Drop item ID; invalid, missing, or air IDs warn and fall back to Ancient Debris. / 掉落物 ID；无效、未注册或空气会警告并回退远古残骸。 |
| `baseChance` | `0.025` | Base drop probability; clamped to [0, 1]. / 基础掉率，钳制到 [0, 1]。 |
| `lootingBonusPerLevel` | `0.01` | Added probability per effective Looting level; clamped to [0, 1]. / 每级有效抢夺增加的掉率，钳制到 [0, 1]。 |
| `maxLootingLevel` | `3` | Maximum effective Looting level; minimum 0. / 有效抢夺等级上限，最小 0。 |
| `dropAmount` | `1` | Item count per successful roll; minimum 1, capped at the item's max stack size. / 每次成功掉落数量，最小 1，不超过该物品最大堆叠数。 |
| `requirePlayerKill` | `true` | Require a player kill, with the optional wolf exception below. / 要求玩家击杀，可通过下项允许狼击杀。 |
| `allowFakePlayer` | `true` | Allow detected fake players when requiring player kills. / 要求玩家击杀时允许检测到的假玩家。 |
| `allowTamedWolfKill` | `false` | When requiring player kills, also accept a tamed wolf whose owner is a player. / 要求玩家击杀时，也接受主人为玩家的已驯服狼击杀。 |
| `respectDoMobLoot` | `true` | Suppress extra drops when doMobLoot is false. / doMobLoot 为 false 时不额外掉落。 |
| `enablePiglinBruteRespawn` | `true` | Enable Piglin Brute respawning. / 开启蛮兵重生。 |
| `spawnIntervalSeconds` | `5` | Ticks-based check interval in seconds; minimum 1. / 按游戏刻计时的检查间隔（秒），最小 1。 |
| `spawnAmountPerAttempt` | `1` | Maximum spawn attempts per player per check; recheck the cap before each brute; minimum 1. / 每轮每位玩家最多尝试生成的数量；每只前检查上限，最小 1。 |
| `nearbyPlayerCheckRadius` | `128` | Block radius for nearby brute counting and structure search (converted to chunks, at least 4 chunks); player position search uses at most 64 blocks; minimum 16. / 附近蛮兵计数及结构搜索半径（格，结构搜索换算为区块且至少 4 区块）；玩家附近位置搜索最多用 64 格，最小配置值 16。 |
| `maxBrutesNearPlayer` | `30` | Nearby brute cap per player; non-positive values block spawning. / 玩家附近蛮兵上限；非正数会阻止生成。 |
| `allowedDimensions` | `["minecraft:the_nether"]` | Dimensions permitting respawn checks; empty list disables checks everywhere. / 允许重生检查的维度；空列表表示全部禁用。 |
| `requirePlayerInsideStructure` | `true` | Require a nearby loaded chunk with a bastion reference; does not check exact bounding-box containment. / 要求附近已加载区块存在堡垒引用，不判断玩家是否精确位于包围盒内。 |
| `peacefulModeSpawning` | `false` | Allow mod spawn attempts on Peaceful; vanilla difficulty behavior still applies. / 允许和平模式下本模组尝试生成；原版难度行为仍生效。 |
| `preferBastionBlocks` | `true` | Prefer listed floor blocks in the first two searches, otherwise retain the first valid fallback; false returns the first valid position. The final broad fallback accepts any valid floor. / 前两层优先选择列表中的地面方块，否则保留首个合法回退；关闭时直接返回首个合法位置。最后的宽松回退接受任意合法地面。 |
| `preferredSpawnBlocks` | `["minecraft:blackstone", "minecraft:polished_blackstone", "minecraft:polished_blackstone_bricks", "minecraft:cracked_polished_blackstone_bricks", "minecraft:chiseled_polished_blackstone", "minecraft:gilded_blackstone"]` | Preferred floor block IDs. / 偏好地面方块 ID 列表。 |
| `maxSpawnPositionAttempts` | `200` | Random columns per first/second search; final fallback tries twice this count; minimum 1. / 前两层每层随机列尝试次数；最终回退尝试两倍次数，最小 1。 |
| `enableDebugLog` | `false` | Enable detailed spawn/drop info logs; warnings/errors remain enabled. / 开启生成和掉落详细日志；警告和错误始终记录。 |
| `debugForceSpawnNearPlayer` | `false` | Skip structure checks for debugging; dimension, difficulty, loaded chunks, height and brute cap still apply. / 调试时跳过结构判断；维度、难度、已加载区块、高度和数量上限仍生效。 |

## Build / 构建

Run with Java 21 / 使用 Java 21 执行：

```sh
./gradlew build
```

Artifacts / 构建产物：`build/libs/`.

## License / 许可证

MIT. See [LICENSE](LICENSE). / MIT，详见 [LICENSE](LICENSE)。
