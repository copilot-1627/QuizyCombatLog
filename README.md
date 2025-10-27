# QuizyCombatLog

[![Version](https://img.shields.io/badge/version-1.0.0-blue)](https://github.com/copilot-1627/QuizyCombatLog)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21+-green)](https://github.com/copilot-1627/QuizyCombatLog)
[![Discord](https://img.shields.io/discord/yourserverid?label=Discord)](https://discord.gg/UUaNzfZyc6)

A powerful and lightweight combat logging plugin that prevents players from logging out during PvP combat, now with **No-Combat Zone** functionality!

## ✨ Features

### 🗡️ Core Combat System
- **Anti-Combat Logging**: Prevents players from escaping combat by disconnecting
- **Configurable Combat Timer**: Set custom combat durations
- **Inventory Drop Protection**: Optional inventory clearing on combat log
- **Action Bar Display**: Real-time combat timer visualization
- **Multi-World Support**: Works across all server worlds

### 🛡️ No-Combat Zones (NEW!)
- **Protected Areas**: Define rectangular zones where combat cannot be initiated
- **Combat Configure Stick**: Interactive area selection tool for admins
- **Movement Restrictions**: Prevent combat players from entering protected zones
- **Persistent Storage**: YAML-based area configuration with automatic saving
- **Cross-World Compatible**: Areas work in any world/dimension

## 📋 Commands

### Core Commands
| Command | Description | Permission |
|---------|-------------|-----------|
| `/qcl help` | Show help message | - |
| `/qcl reload` | Reload plugin configuration | `quizycombatlog.reload` |
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
   - Right-click the **first corner** (lower corner)
   - Right-click the **second corner** (upper corner)
   - The area will be automatically saved and the stick removed

3. **Configure Entry Restrictions** (Optional)
   ```
   /qcl joining disable spawn
   ```
   Combat players will be pushed back when trying to enter.

### Managing Areas

- **List Areas**: Check `plugins/QuizyCombatLog/disabled-areas.yml`
- **Remove Area**: `/qcl remove disable.area <name>`
- **Toggle Entry**: `/qcl joining enable/disable <name>`

## ⚙️ Configuration

### config.yml
```yaml
# Combat timer in seconds
combat-duration: 15

# Drop inventory on combat log
drop-inventory-on-logout: true

# Messages (customizable)
messages:
  combat-started: "&c&l⚔ &8» &7You are now in combat!"
  combat-ended: "&a&l✓ &8» &7You are no longer in combat."
  # ... more messages
```

### disabled-areas.yml (Auto-generated)
```yaml
spawn:
  world: world
  corner1:
    x: -100.0
    y: 60.0
    z: -100.0
  corner2:
    x: 100.0
    y: 80.0
    z: 100.0
  joining_disabled: true
```

## 🔒 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `quizycombatlog.admin` | Access to all admin commands and area configuration | OP |
| `quizycombatlog.reload` | Reload plugin configuration | OP |

## 🚀 Installation

1. **Download** the latest QuizyCombatLog.jar
2. **Place** in your server's `plugins/` folder
3. **Restart** your server
4. **Configure** settings in `plugins/QuizyCombatLog/config.yml`
5. **Create** no-combat zones using `/qcl set disable.area <name>`

## 🛠️ Technical Details

### Requirements
- **Minecraft**: 1.21+ (Paper/Spigot)
- **Java**: 8+
- **Server Software**: Paper, Spigot, or compatible

### Features
- **Multi-threading Safe**: Concurrent data structures for performance
- **Memory Efficient**: Optimized area checking algorithms
- **Event-driven Architecture**: Minimal server performance impact
- **Persistent Data**: Automatic YAML configuration management
- **API Integration**: Uses modern Bukkit/Spigot APIs

### Area Detection Algorithm
```java
// Efficient 3D coordinate containment check
public boolean contains(Location location) {
    return x >= corner1.getX() && x <= corner2.getX() &&
           y >= corner1.getY() && y <= corner2.getY() &&
           z >= corner1.getZ() && z <= corner2.getZ();
}
```

## 🐛 Troubleshooting

### Common Issues

**Combat Configure Stick not working?**
- Ensure you have `quizycombatlog.admin` permission
- Check that you're right-clicking blocks (not air)
- Verify the stick has the correct lore text

**Areas not saving?**
- Check file permissions in `plugins/QuizyCombatLog/`
- Ensure sufficient disk space
- Review console for error messages

**Combat not prevented in areas?**
- Verify area boundaries include combat locations
- Check world names match exactly
- Ensure plugin reload after area creation

### Debug Commands
```
/qcl reload  # Reload configuration
```

Check console logs for detailed error information.

## 🤝 Support

- **Discord**: [Join our community](https://discord.gg/UUaNzfZyc6)
- **Issues**: [GitHub Issues](https://github.com/copilot-1627/QuizyCombatLog/issues)
- **Documentation**: This README and in-game `/qcl help`

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔄 Changelog

### v1.0.0
- ✅ Initial release with core combat logging functionality
- ✅ No-Combat Zones with Combat Configure Stick
- ✅ Area movement restrictions for combat players
- ✅ Comprehensive command system with tab completion
- ✅ Multi-world support and persistent YAML storage
- ✅ Performance optimizations and modern API usage

---

**Made with ❤️ by Quizy**