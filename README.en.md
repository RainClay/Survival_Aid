# Survival Aid

中文版：[README.md](README.md)

A Carpet extension with a set of useful rules for survival servers. Needs Carpet, Fabric.
One repo, 11 Minecraft versions, each in its own directory, built separately.

## Supported versions

All release versions, 11 in total:

| Line | Count | Versions (= dir names) |
|---|---|---|
| 1.21.x | 9 | 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.10, 1.21.11 |
| 26.x | 2 | 26.1.2, 26.2 |

## Rules (33 total, category `survival_aid` in /carpet)

Player protection, 12:
- survivalAidCreeperGriefingControl: creeper blasts still hurt and knock entities back, just no block breaking
- survivalAidDeathCoordinateMessage: on death the player gets their own dimension and coordinates
- survivalAidVoidPlayerRescue: falling in the void auto-equips elytra and fires a rocket back out
- survivalAidVoidPlayerRescueY: y level that triggers the rescue
- survivalAidVoidPlayerRescueCooldown: rescue cooldown in ticks, 20 = 1s
- survivalAidLowDurabilityWarning: warns when main hand durability hits this value, 0 disables it
- survivalAidLowHealthGlow: player glows once health drops to the threshold
- survivalAidLowHealthGlowThreshold: the health threshold for the glow
- survivalAidPreventToolBreak: stops using a tool before it breaks
- survivalAidPreventToolBreakThreshold: the durability threshold for that
- survivalAidNoItemDespawn: dropped items never despawn on their own
- survivalAidNetherPortalSolid: since 1.21.5 the portal teleport check shrank to the middle pillar and teleporting barely triggers, this restores the full block check (1.21.5 only)

Performance, 4:
- survivalAidInstantItemPickup: items get picked up the moment you touch them, 0 ticks
- survivalAidInstantHopper: hoppers transfer every tick, 0 tick cooldown
- survivalAidNoCrammingEntities: listed entities are immune to cramming damage
- survivalAidStackingOptimizedEntities: listed entities skip the push calculation when they stack up

Gameplay tweaks, 8:
- survivalAidAutoEatFood: eats at low hunger, plain food only, never potions, milk, honey bottles, chorus fruit, suspicious stew or enchanted golden apples
- survivalAidAutoEatFoodThreshold: hunger threshold for auto eat
- survivalAidAutoEatFoodDebug: prints one line per second with why auto eat did or didn't eat, handy when it suddenly stops
- survivalAidDisableVillageCatSpawn: villages stop spawning cats at 5 occupied beds, witch hut cats are not affected
- survivalAidTntLikeBlocks: blocks explode like TNT when triggered by redstone, flint and steel or fire charge
- survivalAidNoEndermanGriefing: endermen can no longer pick up or place blocks
- survivalAidVisitorPlayers: listed players can't break, place or use blocks, and can't interact with entities
- survivalAidVisitorNoItemPickup: visitors can't pick up dropped items

Pickup whitelists, 3:
- survivalAidItemPickupFilter: global pickup whitelist, comma separated item ids, `none` allows everything
- survivalAidPlayerItemPickupWhitelist: restrict one player to the whitelisted items, managed with /survivalaid pickup
- survivalAidPlayerItemPickupWhitelistDebug: prints what got checked when a player touches an item, for whitelist debugging

Bot tools, 3:
- survivalAidBotMinecartPreserve: bots dismount before going offline or being killed, so the minecart doesn't vanish or spawn a second one
- survivalAidFakePlayerItemSearch: required for /survivalaid searchitem, finds which fake players carry an item
- survivalAidFakePlayerScanAll: how offline fake players are recognized. Off = by the survivalaid_fake tag (and tags them), on = scan all offline player data

Projection tools, 2:
- survivalAidProjectionFill: required for /survivalaid fill. Reads the single .litematic in the server's schematics/ dir, takes the needed blocks from the runner's inventory and fills every container within 4 blocks of the player
- survivalAidFillExcludeBlocks: item ids to skip when filling, comma separated, minecraft: prefix optional, defaults to excluding blue_ice

Other, 1:
- survivalAidWorkstationHighLight: sneak right-click a villager to highlight its workstation

## Commands (permission level 2)

    /survivalaid pickup allow <item> <player>  add a pickup whitelist entry for a player
    /survivalaid pickup deny <item> <player>   remove one of a player's pickup rules
    /survivalaid pickup clear [item]           no arg clears everything, with arg only that item
    /survivalaid pickup list                   show all current pickup rules
    /survivalaid searchitem <item>             which fake player carries this item, id or display name both work
    /survivalaid fill                          fill containers around the player from the selected Litematica projection

## Translations

Everything a player can see lives in the lang files, no hardcoded messages in the code:

    <version dir>/src/main/resources/assets/survival_aid/lang/
    ├── zh_cn.json
    └── en_us.json

Key prefixes:
- survival_aid.message.* runtime messages
- survival_aid.cmd.* command feedback
- survival_aid.msg.* debug
- survival_aid.rule.<rule>.name / .desc rule names and descriptions

To add a language, drop a json named after the language code into the same dir, e.g. ja_jp.json.

## Building

Each version dir is a standalone Loom project, needs JDK 21:

    cd 1.21.11
    bash gradlew build

Jars land in build/libs/. The first build of a version downloads that Minecraft and its mappings, after that it's cached.

Note: the sources are decompiled (JADX) from the released jar and cleaned up, the JADX comments at the top of the classes are expected.
