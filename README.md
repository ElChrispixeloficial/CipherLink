# CipherLink v0.5

**Plataforma de comunicación privada con identificación única por CipherLink ID.**

[![Version](https://img.shields.io/badge/version-0.5.0-blue.svg)](https://github.com/ElChrispixeloficial/CipherLink/releases)
[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://developer.android.com/about/versions/nougat)
[![License](https://img.shields.io/badge/license-Proprietary-red.svg)](LICENSE)

---

## Capturas de Pantalla

<p align="center">
  <img src="screenshots/splash.png" width="250" />
  <img src="screenshots/home.png" width="250" />
  <img src="screenshots/chat.png" width="250" />
  <img src="screenshots/profile.png" width="250" />
</p>

---

## Características v0.5

### 🔑 CipherLink ID
- ID público corto y memorable: `CL-7A91F3`
- Generado automáticamente al registrarse
- Copiado al portapapeles con un toque
- Separado del identificador interno (UUID)

### 👥 Sistema de Contactos
- Buscar usuarios por CipherLink ID
- Añadir contactos desde la búsqueda
- Perfil básico visible al encontrar usuario
- Directorio local de contactos

### 💬 Mensajería Beta
- Envío y recepción de mensajes de texto
- Estado de entrega: ✓ Enviado → ✓✓ Entregado → ✓✓ Leído
- Iconos de estado en tiempo real
- Marcar mensajes como leídos al abrir chat

### 📞 Llamadas Beta
- Interfaz de llamada de voz completa
- Botones de Mute y Speaker
- Temporizador de duración en tiempo real
- Estados: Llamando → Conectando → En llamada

### 🔄 Sistema de Actualizaciones
- Detección automática de nuevas versiones via GitHub Releases
- Diálogo de actualización con notas de versión
- Opción de saltar versiones
- Intervalo de verificación: 6 horas

### 🔒 Seguridad
- Validación de versiones del sistema
- Auditoría de seguridad del dispositivo
- Verificación de Android KeyStore
- Detección de modo debug y emulador
- Claves privadas protegidas en Android Keystore

### 🎨 Personalización
- Tema claro/oscuro/sistema
- 6 colores de acento
- 6 fondos de chat
- Animaciones configurables
- Foto de perfil con recorte automático

---

## Arquitectura

```
CipherLink/
├── data/
│   ├── local/          # Room Database (4 migraciones)
│   └── repository/     # Auth, Chat, Contact, Profile, AI
├── security/           # KeyManager, VaultManager, NativeSecurityBridge
├── integrity/          # IdentityManager, IntegrityManager
├── ui/
│   ├── home/           # Lista de chats
│   ├── chat/           # Mensajería con estado de entrega
│   ├── searchuser/     # Búsqueda por CipherLink ID
│   ├── call/           # Interfaz de llamadas beta
│   ├── profile/        # Perfil con CipherLink ID
│   ├── aichat/         # CipherAI integrado
│   ├── update/         # Detección de actualizaciones
│   └── theme/          # Material3 theming
├── update/             # GitHub Releases update checker
└── utils/              # CipherLinkIdGenerator, HashUtils, etc.
```

---

## Base de Datos (Room v4)

| Tabla | Descripción |
|-------|-------------|
| `users` | Usuarios registrados con CipherLink ID |
| `chats` | Conversaciones |
| `messages` | Mensajes con estado de entrega |
| `contacts` | Directorio local de contactos |
| `user_profiles` | Perfiles de usuario |
| `ai_chats` | Conversaciones CipherAI |
| `ai_messages` | Mensajes CipherAI |
| `sessions` | Sesiones activas |

---

## Compilación

### Requisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Android SDK 35
- Kotlin 1.9.24

### Pasos
```bash
git clone https://github.com/ElChrispixeloficial/CipherLink.git
cd CipherLink
./gradlew assembleRelease
```

### Firma del APK
El APK se firma con:
- **JavaFirm V2 + V3** signing scheme
- Clave de firma en `keystore/release.jks`
- Compatible con Android 7.0+ (API 24)

---

## Permisos

| Permiso | Uso |
|---------|-----|
| `INTERNET` | Verificación de actualizaciones |
| `ACCESS_NETWORK_STATE` | Estado de conexión |
| `CAMERA` | Foto de perfil (opcional) |
| `READ_MEDIA_IMAGES` | Seleccionar foto de galería |

---

## Changelog

### v0.5.0 (2026-07-25)
- **Nuevo:** CipherLink ID público (formato CL-XXXXXX)
- **Nuevo:** Sistema de contactos con búsqueda por ID
- **Nuevo:** Estado de entrega de mensajes (✓/✓✓/leído)
- **Nuevo:** Interfaz de llamadas de voz (beta)
- **Nuevo:** Detección automática de actualizaciones
- **Nuevo:** Auditoría de seguridad del dispositivo
- **Mejorado:** Perfil muestra CipherLink ID copiable
- **Mejorado:** HomeScreen con botón "Find User"
- **Fix:** CipherLink ID display (sin prefijo duplicado)
- **Fix:** Copia al portapapeles con feedback visual

### v0.4.1 (2026-07-24)
- NDK C++ native security layer (optional)
- Chat background colors
- Recovery system (.clrecovery)
- Profile photo with EXIF rotation fix

### v0.4.0 (2026-07-23)
- SQLCipher encrypted database
- CipherAI foundation
- VaultManager central coordination
- DynamicNaming + FileRotation

### v0.3.0 (2026-07-22)
- Identity integrity system (RSA-2048 + HMAC-SHA256)
- Integrity verification on startup
- Auto-update fingerprints after data changes

### v0.2.0 (2026-07-21)
- Room database with full persistence
- Real registration/login with SHA-256+salt
- Home screen with chat list
- Create conversation screen
- Chat screen with message bubbles

### v0.1.0 (2026-07-20)
- Splash screen with shield animation
- Login and Register screens
- Material Design 3 theme (light/dark)
- Navigation with animations
- CipherLink branding colors

---

## Seguridad

- **Código cerrado** — Sin contribuidores externos
- **Android Keystore** — Claves RSA-2048 y AES-256-GCM en hardware
- **EncryptedSharedPreferences** — Preferencias sensibles cifradas
- **JNI Native Security** — SHA-256, HMAC-SHA256 en C++ con fallback Kotlin
- **Integrity Verification** — HMAC firmado de archivos de datos
- **Version Validation** — Verificación de integridad del APK

---

## Desarrollado por

**Chris** — [@ElChrispixeloficial](https://github.com/ElChrispixeloficial)

---

## Licencia

Propietaria. Todos los derechos reservados.
