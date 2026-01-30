# Changelog

## Version 1.3

### New Features

#### New Combat Restrictions
New combat restrictions added:

- **Wind Charge Restrictions**
  - Option to fully disable wind charges during combat
  - Configurable cooldown for wind charges when not fully disabled
  - Uses native Minecraft cooldown indicator in hotbar

- **Cobweb Restrictions**
  - Option to fully disable cobweb placement during combat
  - Configurable cooldown for cobweb placement when not fully disabled
  - Uses native Minecraft cooldown indicator in hotbar

- **Container Restrictions**
  - Option to fully disable opening containers during combat
  - Includes: chests, ender chests, barrels, hoppers, droppers, dispensers, shulker boxes, trapped chests, furnaces, blast furnaces, smokers, brewing stands

- **Pearl Refresh Combat**
  - New option to refresh combat timer to max when throwing an ender pearl
  - Prevents players from pearling away while their timer counts down
  - Enabled by default

- **Combat Log Zombie Proximity**
  - Players near combat log zombies now trigger the combat timer
  - Works just like being near another player
  - Prevents avoiding combat by logging near a zombie
  - Enabled by default

- **Update Checker**
  - Automatically checks Modrinth for new versions on server startup
  - Notifies opped players or those with `dangerlog.admin` permission on join
  - Clickable download link in the notification message
  - Can be disabled in config

- **Zombie Equipment System**
  - Combat log zombies now visually wear the player's armor
  - Zombie holds player's main hand and offhand items
  - Equipment drops naturally when zombie is killed (100% drop chance)
  - Equipment persists through server restarts

- **Health Synchronization**
  - Zombie spawns with the exact health the player had when logging out
  - When player rejoins (zombie alive), player's health syncs to zombie's current health
  - If zombie was damaged while player was offline, player returns with reduced health

#### Weapon Restrictions (Minecraft 1.21+)
New combat restrictions for Minecraft 1.21 weapons:

- **Mace Restrictions**
  - Option to fully disable maces during combat
  - Configurable cooldown for mace attacks when not fully disabled
  - Uses native Minecraft cooldown indicator in hotbar

- **Spear Restrictions**
  - Option to fully disable spears during combat
  - Option to disable only the spear lunge ability (allows normal damage)
  - Configurable cooldown for spear attacks
  - Configurable cooldown for spear lunge ability
  - Uses native Minecraft cooldown indicator in hotbar

#### ProtocolLib Integration (Optional)
- ProtocolLib is now a soft dependency for advanced spear detection
- Spear lunge detection requires ProtocolLib to be installed
- Plugin functions normally without ProtocolLib (spear lunge features disabled)
- Warning logged if spear lunge features are enabled but ProtocolLib is missing

### New Config Options

```yaml
# Check for updates on Modrinth and notify ops on join
check-updates: true

# Modrinth project slug (change if you fork the plugin)
modrinth-slug: "dangerlog"

# Disable wind charges while in combat
disable-wind-charges: false

# Wind charge cooldown in seconds (0 = no cooldown)
wind-charge-cooldown: 0

# Disable cobweb placement while in combat
disable-cobwebs: false

# Cobweb cooldown in seconds (0 = no cooldown)
cobweb-cooldown: 0

# Disable opening containers while in combat
disable-containers: false

# Refresh combat timer when throwing an ender pearl
pearl-refresh-combat: true

# Whether being near a combat log zombie triggers combat
zombie-proximity-combat: true

# Disable spears while in combat
disable-spears: false

# Disable only the spear lunge ability (requires ProtocolLib)
disable-spear-lunge: false

# Spear cooldown in seconds (0 = no cooldown)
spear-cooldown: 0

# Spear lunge cooldown in seconds (requires ProtocolLib)
spear-lunge-cooldown: 5

# Disable maces while in combat
disable-maces: false

# Mace cooldown in seconds (0 = no cooldown)
mace-cooldown: 5
```

### Technical Changes
- Added ProtocolLib as soft dependency
- New packet listeners for spear detection
- Improved cooldown management for new weapon types

---

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
