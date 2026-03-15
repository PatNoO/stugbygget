# StugBygget

> A full-featured Android app for coordinating a Swedish summer cottage renovation — Summer 2026.

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth_·_Firestore_·_Storage-FFCA28?logo=firebase&logoColor=black)
![ARCore](https://img.shields.io/badge/ARCore-1.50-34A853?logo=google&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-green)

---

## What is StugBygget?

StugBygget ("Cottage Builder") is a personal project app built to manage a real summer-cottage renovation in Sweden. It covers every phase of the project: planning timelines, tracking todos, capturing photos, budgeting, comparing material prices, measuring rooms with AR, and coordinating deliveries — all from one app.

It is also a portfolio project showcasing Android development with modern Jetpack Compose architecture, Firebase backend integration, and ARCore.

---

## Screenshots

> _Screenshots will be added once the physical renovation begins. Placeholder previews below._

| Planning | Todos | Gallery |
|---|---|---|
| `[Planning screen]` | `[Todos screen]` | `[Gallery screen]` |

| AI Chat | Room Planner | AR Measure |
|---|---|---|
| `[AI screen]` | `[Room planner]` | `[AR measurement]` |

| Materials | Shopping | Budget |
|---|---|---|
| `[Materials screen]` | `[Shopping screen]` | `[Budget screen]` |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture (UI → ViewModel → UseCase → Repository) |
| Async | Kotlin Coroutines + StateFlow |
| Dependency Injection | Manual `AppContainer` (Hilt planned) |
| Auth | Firebase Authentication (email/password) |
| Database | Cloud Firestore (real-time, offline-capable) |
| File Storage | Firebase Storage |
| Backend Logic | Firebase Cloud Functions (TypeScript) |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Camera | CameraX |
| Augmented Reality | Google ARCore |
| Image Loading | Coil |
| AI Assistant | Claude API via Retrofit |
| Maps / Routing | Google Maps SDK |

---

## Architecture

```
app/
├── core/                      # Shared utilities, CameraX surface
├── data/
│   ├── firebase/              # Firestore data sources, FCM service
│   └── repository/            # Repository implementations
├── di/                        # AppContainer (manual DI)
├── domain/
│   ├── model/                 # Framework-independent domain models
│   ├── repository/            # Repository interfaces
│   └── usecase/               # Business logic use cases
├── feature/
│   ├── aichat/                # Stugan AI assistant
│   ├── armeasure/             # AR measurement tool
│   ├── auth/                  # Email/password authentication
│   ├── budget/                # Budget tracking
│   ├── gallery/               # Photo gallery
│   ├── logistics/             # Transport & delivery planning
│   ├── materials/             # Smart materials & live prices
│   ├── planning/              # Renovation timeline
│   ├── roomplanner/           # 2D room layout editor
│   ├── shopping/              # Shopping lists
│   └── todos/                 # Task management
├── navigation/                # NavHost, routes, transitions
└── ui/
    ├── components/            # Shared Sommar design system components
    └── theme/                 # Colors, typography, shapes, animations
```

### Data flow

```
Composable  ──►  ViewModel  ──►  UseCase  ──►  Repository  ──►  Firestore / API
                    │                                                    │
                 UiState  ◄────────────────────────────────────────────
```

---

## Features

### Modules

| Module | Status | Description |
|---|---|---|
| Planning / Timeline | ✅ Done | 8-phase renovation timeline with progress rings and date tracking |
| Todos | ✅ Done | Task list with priority, assignee, phase linking, and filter chips |
| Photo Gallery | ✅ Done | Before/during/after gallery with Firebase Storage upload |
| Stugan AI | ✅ Done | Chat interface powered by Claude API with markdown rendering |
| Room Planner 2D | ✅ Done | Drag-and-drop 2D room layout editor with furniture catalogue |
| AR Measurement | ✅ Done | ARCore-based distance measurement with export to Room Planner |
| Smart Materials | ✅ Done | Material spec sheets, unit calculator, live price comparison |
| Shopping Lists | ✅ Done | Multi-list shopping manager with budget estimates |
| Budget | ✅ Done | Phase and category budget tracking with over-budget warnings |
| Logistics Planning | ✅ Done | Transport recommendation engine (own car / trailer / delivery) |

### Infrastructure

| Feature | Status |
|---|---|
| Firebase Auth (email/password) | ✅ Done |
| Offline sync with Firestore | ✅ Done |
| Push notifications (FCM) | ✅ Done |
| Navigation transitions | ✅ Done |
| Dark mode support | ✅ Done |
| Price scraping Cloud Function | 🚧 Planned (SB70) |
| Price comparison Cloud Function | 🚧 Planned (SB71) |
| Transport calculator Cloud Function | 🚧 Planned (SB71) |
| End-to-end testing | 🚧 Planned (SB72) |

---

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 11+
- Android device or emulator with API 26+
- A Firebase project (Auth + Firestore + Storage + Cloud Functions enabled)
- A Google Cloud project with ARCore and Maps SDK enabled

### 1. Clone the repository

```bash
git clone https://github.com/PatNoO/stugbygget.git
cd stugbygget
```

### 2. Firebase configuration

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package name `com.example.stugbygget`
3. Download `google-services.json` and place it at `app/google-services.json`

> **Never commit `google-services.json` — it is in `.gitignore`.**

### 3. API keys

Create a `local.properties` file in the project root (already in `.gitignore`) and add:

```properties
MAPS_API_KEY=your_google_maps_api_key_here
PROJECT_ID=your_firestore_project_id_here
```

The Claude API key for the AI assistant is stored in Firebase Remote Config under the key `claude_api_key`. Set it in your Firebase project:

```
Remote Config key: claude_api_key
Value: your_anthropic_api_key_here
```

### 4. Enable Firebase services

In your Firebase console, enable:
- **Authentication** → Email/Password provider
- **Firestore** → Create database (start in test mode, then apply security rules)
- **Storage** → Enable default bucket
- **Cloud Functions** → Enable billing (Blaze plan required)
- **Cloud Messaging** → No extra setup needed

### 5. Run the app

```bash
./gradlew assembleDebug
```

Or open in Android Studio and press **Run**.

---

## Firestore Data Model

All data is scoped under `projects/{projectId}/`:

```
projects/{projectId}/
├── phases/           # Renovation phases (timeline)
├── todos/            # Todo items
├── photos/           # Photo metadata (files in Storage)
├── rooms/            # Room layouts (furniture positions)
├── measurements/     # AR measurements
├── materials/        # Material specs and prices
├── prices/           # Live price quotes from suppliers
├── shopping_lists/   # Shopping lists and items
├── budget/           # Budget entries per phase/category
├── transport/        # Logistics inputs and results
└── chat/             # AI conversation history

users/{userId}/       # FCM tokens (for push notifications)
```

---

## Design System — Svensk Sommar

The UI uses a custom design system called **Svensk Sommar** built on top of Material 3.

### Color palette

| Name | Hex | Usage |
|---|---|---|
| Falu Red | `#8B2E16` | Primary — headers, buttons |
| Lake Blue | `#2E6B8A` | Secondary — info, links |
| Meadow Green | `#4A7C59` | Success, completed states |
| Midsummer Gold | `#D4A843` | Accents, warnings |
| Wood Warm | `#6B4C30` | Neutral, materials |
| Cream Background | `#FEFCF6` | App background |

### Components

`SommarCard`, `SommarHeaderCard`, `SommarStatCard`, `SommarBadge`, `SommarFilterChip`, `SommarButton`, `SommarOutlineButton`, `SommarProgressBar`, `SommarProgressRing`, `SommarTimelineIcon`, `SommarInfoBox`, `SommarSectionTitle`, `SommarShimmerCard`, `SommarErrorCard`

### Typography

- **Display / Headings**: Fraunces (serif)
- **Body / UI**: Instrument Sans
- **Measurements / Data**: JetBrains Mono

---

## Roadmap

- [ ] **SB70** — Scheduled Cloud Function for daily price scraping from Swedish building suppliers (Byggmax, Bauhaus, Hornbach)
- [ ] **SB71** — Callable Cloud Functions for price optimiser (`price-compare`) and live transport cost calculator using Google Maps Routes API (`transport-calc`)
- [ ] **SB72** — End-to-end testing pass across all 9 modules
- [ ] **Future** — Hilt dependency injection, multi-user/invite support, widget for daily progress

---

## License

MIT — see [LICENSE](LICENSE) for details.
