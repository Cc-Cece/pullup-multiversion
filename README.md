# 🛩️ PullUp+

**PullUp+** is a Minecraft mod that provides **highly customizable voice alerts** and **HUD visual prompts** for Elytra flight.

This mod offers customizable voice warnings for Elytra flying in Minecraft. You can define different trigger logics, HUD texts, and audio effects based on local profile sets, server-synced profile sets, or profile sets generated via the PullUp Cloud Editor.

This mod is developed based on `MUYU_Twilighter/pullup_fabric`, with continuous maintenance and updates.

---

## Key Features

* **Smart Status Detection:** Supports using mathematical expressions to precisely detect player flight status (altitude, speed, pitch angle, collision distance, etc.).
* **Stereo Voice Alerts:** Automatically plays custom sound effects (such as the classic "*Pull up!*", "*Terrain ahead!*", "*Sink rate!*") when corresponding rules are triggered.
* **Visual HUD Prompts:** Supports pop-up highlighted text prompts in the center of the screen, with customizable colors and opacity.
* **Multi-Source Profile Support:** Supports **local configuration**, **server synchronization**, and one-click import from the **Cloud Editor**.
* **High Compatibility:** Supports Fabric, Forge, and NeoForge.

---

## Quick Start

1. Place the mod `.jar` file for your game version into your `mods` folder.
2. After launching the game for the first time, the mod will automatically generate the main configuration file and example profile sets in the `config/pullup/` directory.
3. **In Singleplayer or on the Client:** Enter `/pullupclient gui` to open the profile management interface.
4. **In Multiplayer:** If the mod is also installed on the server, the client can simply enter `/pullupclient grab` to automatically fetch and sync the server's flight alert rules.

> **Tip:**
> The mod is fully functional as a **client-only** mod. However, if installed on the server, it allows for server rule synchronization.

---

## Commands

### Client

* `/pullupclient gui` — Opens the intuitive profile GUI management interface.
* `/pullupclient status` — Views the current enabled status, number of loaded rules, and cloud connection status.
* `/pullupclient load <filename>` — Loads the specified local profile configuration file.
* `/pullupclient load default` — Restores and loads the built-in default profile.
* `/pullupclient grab` — Actively requests and syncs the current server profile.

### Server

* `/pullupserver load <filename>` — Loads the specified profile file for the entire server.
* `/pullupserver enablesend` — Allows broadcasting and applying the server profile configuration to joining clients.

---

## Visual Editor

Finding it too tedious to modify JSON configuration files manually? We provide a Visual Editor!

1. Enter `/pullupclient cloud edit` in-game, and an exclusive editing link will be generated for you.
2. Open the link in your browser to easily edit your rules, alert sounds, trigger ranges, and HUD text through a visual interface.
3. Click publish after editing, and copy the generated **short code**.
4. Enter `/pullupclient cloud import <short_code>` in-game to save the rules locally and apply them immediately!

> Editor URL: [https://pullup.akihito.dpdns.org/](https://pullup.akihito.dpdns.org/)

---

## License

* **Open Source License:** This fork is open-sourced under the `GPL-3.0-only` license.
* **Upstream Project:** Special thanks to the upstream author for their outstanding work: [MUYU_Twilighter/pullup_fabric](https://github.com/MUYUTwilighter/pullup_fabric) (MIT).
