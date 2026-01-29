# Changelog

## Version 1.2

### New Features

#### Combat Restrictions System
A comprehensive system to restrict items and abilities while players are in combat:

- **Trident (Riptide) Restrictions**
  - Option to fully disable tridents with Riptide enchantment during combat
  - Configurable cooldown for tridents when not fully disabled
  - Velocity reduction as backup prevention if riptide somehow triggers while disabled

- **Elytra Restrictions**
  - Option to disable elytra gliding while in combat
  - Players cannot start gliding, but won't be forcefully unglided mid-flight

- **Ender Pearl Restrictions**
  - Option to fully disable ender pearls during combat
  - Configurable cooldown for pearls when not fully disabled
  - Uses native Minecraft cooldown indicator in hotbar

- **Firework Rocket Restrictions**
  - Option to fully disable firework rockets (elytra boosting) during combat
  - Configurable cooldown for fireworks when not fully disabled
  - Uses Paper's PlayerElytraBoostEvent for accurate detection

#### Native Cooldown Indicators
- All item cooldowns now display using Minecraft's native cooldown animation in the hotbar
- Disabled items show a persistent cooldown that refreshes while in combat
- Cooldowns are automatically cleared when leaving combat

#### Dynamic Config Updates
- Config changes now apply immediately to players already in combat
- Re-enabling an item mid-combat clears the disabled cooldown
- Disabling an item mid-combat applies the cooldown immediately

### New Config Options

```yaml
# Disable tridents (Riptide) while in combat
disable-tridents: true

# Cooldown for tridents while in combat (seconds, 0 = no cooldown)
# Only applies if disable-tridents is false
trident-cooldown: 10

# Disable elytras while in combat
disable-elytras: true

# Disable ender pearls while in combat
disable-pearls: false

# Cooldown for pearls while in combat (seconds, 0 = no cooldown)
# Only applies if disable-pearls is false
pearl-cooldown: 10

# Disable firework rockets while in combat
disable-fireworks: false

# Cooldown for fireworks while in combat (seconds, 0 = no cooldown)
# Only applies if disable-fireworks is false
firework-cooldown: 10
```

### Bug Fixes
- Fixed trident blocking where holding right-click would eventually allow riptide
- Fixed ender pearl cooldown not applying after throwing
- Fixed firework rockets not propelling players when cooldown was enabled
- Fixed cooldowns not clearing when items were re-enabled mid-combat
- Fixed cooldowns not applying when items were disabled mid-combat

---

## Version 1.1

### New Features

#### Ally System
- Players can now add allies who won't trigger combat when nearby
- `/ally add <player>` - Send an ally invite to another player
- `/ally remove <player>` - Remove a player from your allies
- `/ally list` - View your current allies
- `/ally accept <player>` - Accept a pending ally invite
- `/ally deny <player>` - Deny a pending ally invite
- Configurable maximum number of allies per player
- Ally invites expire after configurable duration
- Mutual ally system - both players must accept to become allies

### Bug Fixes
- Fixed various command handling issues
- Improved combat detection reliability
- Fixed edge cases with player disconnection during combat

---

## Version 1.0

### Initial Release
- Proximity-based combat detection system
- Combat timer with action bar display
- Zombie log system for combat logging
- Configurable radius, timer duration, and death type
- Admin commands for managing combat state
