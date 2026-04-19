# 🚀 RedMagic Hardware Control

Root-level hardware control app for RedMagic devices.

---

## 🔧 Core Features
- Full control over:
  - Cooling fan
  - RGB LEDs (Fan, Logo, Shoulder)
  - Shoulder triggers
  - Liquid cooling pump
- Direct sysfs interaction using root
- Instant hardware response via shell execution

---

## 💡 Unified LED System
- Fully rewritten LED control engine
- Single unified pipeline for all LED zones:
  - Fan
  - Logo
  - Shoulder
- Shared logic for:
  - Color mapping
  - Effect mapping
  - Command generation

### Supported Effects
- Steady
- Breathe
- Flashing

---

## 🎨 LED Customization
- Per-zone LED control
- Full color selection
- Hardware-level enable/disable
- Reliable effect application across all zones

---

## ⚡ Real-Time LED Preview
- Toggleable live preview system
- Instantly applies:
  - Colors
  - Effects
- Can be disabled for performance or battery savings

---

## 🧠 Smart Cooling System

### 🌪️ Auto Fan Control
- Temperature-based fan curves:
  - Quiet
  - Balanced
  - Turbo
- Automatically adjusts fan speed based on CPU temperature (°F)

### 💧 Smart Pump Control
- Automatic pump profiles:
  - Slow
  - Medium
  - Quick

### ⏱️ Dynamic Polling
- Adaptive temperature checks:
  - ≥95°F → every 5 seconds
  - <95°F → every 10 seconds

---

## 📊 Temperature Monitoring
- Reads multiple thermal zones
- Automatic value normalization
- Outputs in Fahrenheit for UI + logic

---

## 🎮 Shoulder Trigger Controls
- Enable / disable triggers
- Root-level SAR interaction
- Tap injection support

---

## 🎚️ Background Services
- Fan LED service maintains LED state
- Prevents system overrides
- Auto-start/stop logic based on usage

---

## 💾 Persistent Settings
- Saves all hardware states:
  - LED configs
  - Fan curve
  - Pump profile
  - Preview toggle
- Restores everything on launch

---

## 🧼 UI Improvements
- Clean section-based layout
- Removed redundant controls
- Improved spacing and consistency
- Dedicated preview controls

---

## ⚠️ Requirements
- Root access required
- Designed for RedMagic devices

---

## 🛠️ Notes
This app directly interfaces with hardware nodes. Use responsibly.
