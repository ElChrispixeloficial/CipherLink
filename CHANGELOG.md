# Changelog

All notable changes to CipherLink will be documented in this file.

---

## [0.5.0] - 2026-07-25

### Added
- CipherLink ID system — unique user identification (format: `CL-XXXXXX`)
- Find Users screen — search by CipherLink ID
- Contacts system — local directory of added contacts
- Message delivery status — Sent / Delivered / Read indicators
- Voice call interface (beta) — basic calling with mute/speaker
- Auto-update checker — verifies new versions via GitHub Releases
- Security audit — device and app status validation

### Changed
- Profile screen displays CipherLink ID with copy button
- Home screen includes "Find User" quick action
- Improved message bubbles with delivery status icons

### Fixed
- CipherLink ID display (removed duplicate "CL-" prefix)
- Copy to clipboard now shows confirmation feedback
- Database migration properly adds new columns to existing tables

---

## [0.4.1] - 2026-07-24

### Added
- Native C++ security layer (optional, with Kotlin fallback)
- Chat background color presets (6 options)
- Recovery system for data backup packages
- Profile photo with automatic crop and resize

### Changed
- Updated to Room database v3

---

## [0.4.0] - 2026-07-23

### Added
- Encrypted database support
- AI chat assistant (CipherAI)
- Central vault manager for data coordination
- Dynamic file naming system

---

## [0.3.0] - 2026-07-22

### Added
- Identity verification system
- Integrity checks on app startup
- Automatic fingerprint updates after data changes

---

## [0.2.0] - 2026-07-21

### Added
- Room database with full persistence
- User registration and login
- Home screen with chat list
- Chat screen with message bubbles

---

## [0.1.0] - 2026-07-20

### Added
- Initial release
- Splash screen with animation
- Login and Register screens
- Material Design 3 theme (light/dark)
- Navigation with animations
