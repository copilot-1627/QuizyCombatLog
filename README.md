# QuizyCombatLog

[![Version](https://img.shields.io/badge/version-1.0.0-blue)](https://github.com/copilot-1627/QuizyCombatLog)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21+-green)](https://github.com/copilot-1627/QuizyCombatLog)
[![Discord](https://img.shields.io/discord/yourserverid?label=Discord)](https://discord.gg/UUaNzfZyc6)

A powerful and lightweight combat logging plugin that prevents players from logging out during PvP combat, now with **No-Combat Zone** functionality!

## ✨ Features

### 🗡️ Core Combat System
- **Anti-Combat Logging**: Players die instantly when logging out during combat
- **Immediate Inventory Drop**: All items drop at logout location, not on rejoin
- **Configurable Combat Timer**: Set custom combat durations
- **Action Bar Display**: Real-time combat timer visualization
- **Multi-World Support**: Works across all server worlds
- **Knockback System**: Prevents combat players from entering safe zones

### 🛡️ No-Combat Zones
- **Protected Areas**: Define rectangular zones where combat cannot be initiated
- **Combat Configure Stick**: Interactive area selection tool for admins
- **Movement Restrictions**: Knockback system prevents combat entry
- **Persistent Storage**: YAML-based area configuration with automatic saving
- **Cross-World Compatible**: Areas work in any world/dimension
- **Entry Control**: Per-area settings for combat player restrictions

## 📋 Commands

### Core Commands
| Command | Description | Permission |
|---------|-------------|-----------|
| `/qcl help` | Show help message | - |
| `/qcl reload` | Reload plugin configuration AND areas | `quizycombatlog.reload` |
| `/qcl combattime <seconds>` | Set combat duration | `quizycombatlog.admin` |
| `/qcl inventorydrop <true/false>` | Toggle inventory drop on combat log | `quizycombatlog.admin` |

### No-Combat Zone Commands
| Command | Description | Permission |
|---------|-------------|-----------|
| `/qcl set disable.area <name>` | Create a no-combat zone with configuration stick | `quizycombatlog.admin` |
| `/qcl remove disable.area <name>` | Remove a no-combat zone | `quizycombatlog.admin` |
| `/qcl joining disable <name>` | Prevent combat players from entering area | `quizycombatlog.admin` |
| `/qcl joining enable <name>` | Allow combat players to enter area | `quizycombatlog.admin` |

## 🎯 No-Combat Zone Setup Guide

### Creating a Protected Area

1. **Get Configuration Stick**
   ```
   /qcl set disable.area spawn
   ```
   You'll receive a **Combat Configure Stick** with instructions.

2. **Define Area Boundaries**
   - **First right-click**: Sets corner #1 (displays coordinates)
   - **Second right-click**: Sets corner #2 and creates the area
   - The stick is **automatically removed** after area creation

3. **Configure Entry Restrictions** (Optional)
   ```
   /qcl joining disable spawn
   ```
   Combat players will be knocked back when trying to enter.

### Managing Areas

- **Reload Everything**: `/qcl reload` (reloads both config.yml and disabled-areas.yml)
- **Remove Area**: `/qcl remove disable.area <name>`
- **Toggle Entry**: `/qcl joining enable/disable <name>`

## ⚙️ Configuration

### config.yml - Fully Customizable Messages
```yaml
# Combat Settings
combat:
  duration: 15                    # Combat time in seconds
  inventory-drop: true            # Drop inventory on combat log
  unblocked-commands:             # Commands allowed during combat
    - "msg"
    - "tell"
    - "helpop"

# Action Bar Settings
actionbar:
  enabled: true                   # Enable/disable action bar
  format: "&c&lCOMBAT » &f{time}s remaining"

# All Messages Are Configurable!
messages:
  combat-started: "&4&l⚔ » &7You have entered &c&lcombat&7!"
  combat-ended: "&2&l✔ » &7You are no longer in &ccombat&7!"
  cannot-enter-area: "&4&l✖ » &7You cannot enter &e{areaName} &7while in combat!"
  # ... and many more!
```

### disabled-areas.yml (Auto-generated)
```yaml
spawn:
  world: world
  corner1: { x: -100.0, y: 60.0, z: -100.0 }
  corner2: { x: 100.0, y: 80.0, z: 100.0 }
  joining_disabled: true
```

## 🔧 Fixed Issues

✅ **Combat Logging**: Players now die **immediately** on logout, items drop at logout location  
✅ **Stick Selection**: Proper corner selection - first click = corner 1, second click = corner 2, then stick removal  
✅ **Full Message Config**: Every single message is now configurable in config.yml  
✅ **Complete Reload**: `/qcl reload` now reloads both config.yml AND disabled-areas.yml  
✅ **Knockback System**: Combat players are knocked back when trying to enter safe zones  

## 🛠️ Technical Details

### Combat Logging Behavior
- **Immediate Death**: Player dies instantly on logout during combat
- **Item Drop Location**: All items drop at the exact logout location
- **No Delay**: No waiting for rejoin - punishment is immediate
- **Broadcast Message**: Server announces the combat log

### Safe Zone System
- **3D Area Detection**: Precise coordinate-based containment
- **Knockback Physics**: Uses velocity vectors for realistic pushback
- **Multi-World Support**: Areas saved with world names
- **Persistent Data**: Automatic YAML file management

## 🔒 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `quizycombatlog.admin` | Access to all admin commands and area configuration | OP |
| `quizycombatlog.reload` | Reload plugin configuration and areas | OP |

## 🚀 Installation

1. **Download** the latest QuizyCombatLog.jar
2. **Place** in your server's `plugins/` folder
3. **Restart** your server
4. **Configure** messages in `plugins/QuizyCombatLog/config.yml`
5. **Create** no-combat zones using `/qcl set disable.area <name>`

## 📋 Example Workflow

```bash
# Create protected spawn area
/qcl set disable.area spawn
# Right-click first corner: "First corner set at (x, y, z)"
# Right-click second corner: "Area 'spawn' created!" + stick removed

# Prevent combat players from entering
/qcl joining disable spawn
# "Players in combat can no longer enter spawn."

# Test the system
# Player in combat tries to enter spawn area
# Result: Knocked back + "You cannot enter spawn while in combat!"

# Reload everything
/qcl reload
# "Configuration and areas reloaded successfully!"
```

## 🐛 Troubleshooting

**Combat logging not working?**
- Ensure `inventory-drop: true` in config.yml
- Check console for errors
- Verify players actually took damage from other players

**Stick not working?**
- Must have `quizycombatlog.admin` permission
- Must right-click on blocks (not air)
- Check that area name doesn't already exist

**Safe zones not working?**
- Verify area coordinates in `disabled-areas.yml`
- Check world names match exactly
- Use `/qcl reload` after making changes

## 🤝 Support

- **Discord**: [Join our community](https://discord.gg/UUaNzfZyc6)
- **Issues**: [GitHub Issues](https://github.com/copilot-1627/QuizyCombatLog/issues)
- **Documentation**: This README and in-game `/qcl help`

---

**Made with ❤️ by Quizy**  
**All issues fixed and fully functional!** 🎉