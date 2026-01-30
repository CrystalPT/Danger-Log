# ⚔️ Danger Log

> **Combat logging protection for Minecraft Paper 1.21+ servers**

---

## 📖 Overview

**Danger Log** is a powerful combat logging protection plugin that prevents players from escaping PvP situations by disconnecting. When a player logs out during combat, a zombie NPC spawns in their place — if the zombie dies, so does the player!

---

## ✨ Features

### 🎯 **Combat Timer System**
- Players within a configurable radius receive a combat timer in their action bar
- Timer displays as `20/20` and stays at max while enemies are nearby
- Timer counts down when players leave combat range
- Logging out with an active timer = **combat logging**

### 🧟 **Combat Log Protection**
| Mode | Description |
|------|-------------|
| **ZOMBIE** | Spawns a zombie NPC with the player's name. If killed, drops all loot. |
| **INSTANT** | Immediately kills the player upon combat logging. |

### 🤝 **Ally System**
- Team up with friends to avoid triggering each other's combat timers
- Clickable invite system with expiration
- Configurable max allies per player

### 💾 **Persistence**
- Zombies survive server restarts
- Player inventories are safely stored
- All data saved to files

### 🚫 **Combat Restrictions**
- **Disable Tridents** (Riptide) while in combat — no flying away!
- **Disable Elytras** while in combat — stay and fight!
- **Disable/Cooldown Ender Pearls** — configurable cooldown or full disable
- **Disable/Cooldown Firework Rockets** — prevent elytra boosting

### 🎮 **Smart Detection**
- Only affects **Survival** mode players
- Creative, Spectator, and Adventure players are ignored
- Bypass permission available for staff

---

## 📋 Commands

### 🔧 Admin Commands
*Requires `dangerlog.admin` permission (default: OP)*

| Command | Description |
|:--------|:------------|
| `/dangerlog on` | Enable the plugin |
| `/dangerlog off` | Disable the plugin |
| `/dangerlog reload` | Reload configuration |

### 👥 Ally Commands
*Available to all players*

| Command | Description |
|:--------|:------------|
| `/ally invite <player>` | Send an ally request |
| `/ally accept [player]` | Accept a pending invite |
| `/ally remove <player>` | Remove an ally |
| `/ally list` | View your allies |

---

## 🔑 Permissions

| Permission | Description | Default |
|:-----------|:------------|:--------|
| `dangerlog.admin` | Access admin commands | **OP** |
| `dangerlog.bypass` | Bypass combat log system | **false** |

---

## ⚙️ Configuration

```yaml
# ===========================================
#         DANGER LOG CONFIGURATION
# ===========================================

# ------ UPDATE CHECKER ------
# Check Modrinth for updates and notify ops on join
check-updates: true

# Modrinth project slug (change if you fork the plugin)
modrinth-slug: "dangerlog"

# ------ GENERAL SETTINGS ------
# Combat detection radius (in blocks)
radius: 50

# Combat timer duration (seconds)
timer-duration: 45

# ------ DEATH TYPE ------
# ZOMBIE = Spawns NPC zombie
# INSTANT = Kills player immediately
death-type: ZOMBIE

# ------ ZOMBIE SETTINGS ------
# How long before zombie auto-expires (-1 or 0 = never)
# Minimum: 10 seconds
zombie-log-timer-max: -1

# Being near a combat log zombie triggers combat timer
zombie-proximity-combat: true

# ------ ALLY SYSTEM ------
# Enable/disable allies
allow-allies: true

# Max allies per player (-1 or 0 = unlimited)
max-allies: 6

# Ally invite duration (seconds)
invite-duration: 120

# ------ COMBAT RESTRICTIONS ------
# Disable tridents (Riptide) while in combat
disable-tridents: true

# Trident cooldown in combat (seconds, 0 = no cooldown)
# Only applies if disable-tridents is false
trident-cooldown: 10

# Disable elytras while in combat
disable-elytras: true

# Disable ender pearls while in combat
disable-pearls: false

# Disable firework rockets while in combat
disable-fireworks: false

# Pearl cooldown in combat (seconds, 0 = no cooldown)
pearl-cooldown: 10

# Refresh combat timer when throwing a pearl
pearl-refresh-combat: true

# Firework cooldown in combat (seconds, 0 = no cooldown)
firework-cooldown: 10

# Disable wind charges while in combat
disable-wind-charges: false

# Wind charge cooldown in combat (seconds, 0 = no cooldown)
wind-charge-cooldown: 0

# Disable cobweb placement while in combat
disable-cobwebs: false

# Cobweb cooldown in combat (seconds, 0 = no cooldown)
cobweb-cooldown: 0

# Disable opening containers while in combat
# Includes: chests, ender chests, barrels, hoppers, shulker boxes, etc.
disable-containers: false

# ------ WEAPON RESTRICTIONS (1.21+) ------
# Disable spears while in combat
disable-spears: false

# Disable only spear lunge (requires ProtocolLib)
disable-spear-lunge: true

# Spear cooldown in combat (seconds, 0 = no cooldown)
spear-cooldown: 0

# Spear lunge cooldown (requires ProtocolLib)
spear-lunge-cooldown: 5

# Disable maces while in combat
disable-maces: false

# Mace cooldown in combat (seconds, 0 = no cooldown)
mace-cooldown: 5

# ------ GENERAL ------
# Plugin enabled state
enabled: true
```

---

## 🎬 How It Works

```
1️⃣  Two players enter combat range (30 blocks)
         ↓
2️⃣  Both see combat timer: [20/20]
         ↓
3️⃣  Timer stays at max while in range
         ↓
4️⃣  Player leaves range → timer counts down
         ↓
5️⃣  Player logs out with active timer
         ↓
    ┌─────────────────────────────────────┐
    │  ⚠️ COMBAT LOG DETECTED!            │
    │  Zombie spawns at logout location   │
    │  Chat: "Player has logged out in    │
    │         combat."                    │
    └─────────────────────────────────────┘
         ↓
6️⃣  If zombie is killed → loot drops
         ↓
7️⃣  Player rejoins → DEATH SCREEN
```

---

## 🧟 Zombie NPC Details

| Property | Value |
|:---------|:------|
| **Name** | Player's name (bold red) |
| **AI** | Disabled |
| **Burns in Sun** | No |
| **Health** | Synced with player (both ways) |
| **Equipment** | Wears player's armor, holds items |
| **Drops** | Equipped items + inventory on death |
| **Despawns** | Never (unless configured) |

### Health Synchronization
- Zombie spawns with the exact health the player had
- If player rejoins while zombie is alive, player's health matches zombie's current health
- Damage dealt to the zombie while offline affects the player when they return

---

## 📁 Data Files

| File | Purpose |
|:-----|:--------|
| `config.yml` | Plugin configuration |
| `allies.yml` | Stored ally relationships |
| `zombies.yml` | Active combat log zombies |

---

## 💡 Tips

- **For SMP servers:** Set `zombie-log-timer-max` to `-1` so zombies stay forever
- **For minigames:** Use `INSTANT` death-type for immediate punishment
- **For teams:** Enable allies with a reasonable `max-allies` limit
- **For staff:** Give `dangerlog.bypass` permission to moderators
- **Anti-escape:** Keep tridents and elytras disabled to prevent flying away
- **Pearl cooldown:** Set `pearl-cooldown` to 10+ seconds to limit pearl spam

---

## 📞 Support

Having issues? Contact the developer!

---

**Made with ❤️ by CrystalPT**
