<img src="https://i.imgur.com/reQIzd0.png" alt="Logo" align="right">
<div align="center">
  <h1>An extensive questing system</h1>
  <h3>Quests is the easy-to-use, open-source plugin for the creation and execution of quests on Minecraft servers. Players can take on multiple quests simultaneously, completing them for stellar rewards and unlockables.</h3>

[![Build Status](https://ci.codemc.org/job/PikaMug/job/Quests/badge/icon)](https://ci.codemc.org/job/PikaMug/job/Quests/)
[![Downloads](https://img.shields.io/spiget/downloads/3711)](https://www.spigotmc.org/resources/quests.3711/)
[![Rating](https://img.shields.io/spiget/stars/3711)](https://www.spigotmc.org/resources/quests.3711/)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/translate-quests/localized.svg)](https://crowdin.com/project/translate-quests)
[![Discord](https://img.shields.io/discord/506992958894243860)](https://discordapp.com/invite/QdJAv2G7qg)
[![Players](https://img.shields.io/bstats/players/9528)](https://bstats.org/plugin/bukkit/Quests%20Classic/9528)
[![Server](https://img.shields.io/bstats/servers/9528)](https://bstats.org/plugin/bukkit/Quests%20Classic/9528)
</div>

Download
---

Distributed through these fine vendors:
- CurseForge (https://www.curseforge.com/minecraft/bukkit-plugins/quests/)
- Modrinth (https://modrinth.com/plugin/quests.classic)

Usage
---

Read about usage in the documentation: https://pikamug.gitbook.io/quests/

Compile
---

Java 8 and Maven required: https://pikamug.gitbook.io/quests/master/plugin-compilation

Translate
---

Help localize into your language:
- Project (https://crowdin.com/project/translate-quests)
- Documentation (https://github.com/PikaMug/QuestsDoc)

About the Fabric module (`fabric/`)
---

The `fabric` module only supports newer Minecraft versions (floor is 26.1, the first unobfuscated release) and is
compiled against Mojang's official (mojmap) classes supplied by the `quests-minecraft-official` dependency and the
Fabric API. Since Minecraft 26.1 ships unobfuscated (official) names, the built jar loads directly on a vanilla-loader
(Fabric) server **without any remapping**. It has been boot-verified on Minecraft 26.1 (Fabric Loader 0.18.4,
Fabric API 0.145.4+26.1.1) and 26.1.2 (Fabric Loader 0.19.2, Fabric API 0.155.2+26.1.2). Running
`mvn -pl fabric -am package -DskipTests -B` produces:
- `fabric/target/quests-fabric-5.3.3.jar` — shaded, official-mapped build, runnable as-is.

Build requirements:
- JDK 25 (bytecode targets Java 25; see `fabric.mod.json` `"java": ">=25"`).
- The `quests-minecraft-official:26.1` artifact in the local Maven repository (Mojang-mapped, compile-only; the real
  server jar is provided by the Fabric loader at runtime).

Additional Fabric caveats:
- Kyori/adventure (`net.kyori:adventure-api:4.14.0`, provided) is a compile-only bridge for the
  `ConversationAPI.TimeClause.create(long, Component)` overload; only the `String` overload is used at runtime.
- The `quests-minecraft-official` dependency and the Fabric API sub-modules (`fabric-api-base`, `fabric-networking-api-v1`,
  etc., pinned to the Minecraft 26.1 / Fabric API 0.145.x line) in the local Maven repository are Mojang-mapped builds for
  compilation; they are compile-only and are not shipped.