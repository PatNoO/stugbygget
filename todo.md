# StugBygget — Season TODO (Summer 2026)

## App — Code TODOs

- [x] **Gallery** — Wire camera FAB to actually capture a photo (`GalleryScreen.kt`)
- [x] **Gallery / Todos / Planning** — Wire `SommarPhotoPicker` selected URIs to the ViewModel and upload to Firebase Storage
- [x] **Shopping** — Wire `CompareShoppingPricesUseCase` to show per-store price totals in the shopping UI

---

## App — Features to Polish

- [x] **Planning** — Add edit/delete support for existing phases
- [x] **Todos** — Add edit/delete support for existing todos + checkbox toggle + detail popup
- [x] **Gallery** — Add full-screen photo viewer when tapping a photo
- [x] **Gallery** — Add ability to delete photos
- [x] **Gallery** — Simplified to category-only filter (Alla bilder / Innan / Under / Efter), room and phase fields removed
- [x] **Materials** — Seed default Swedish materials (Fasadfärg, Träolja, Innerfärg etc.) on first empty load
- [x] **Materials** — Search is live and filters catalog in real time
- [x] **Materials** — Add to "Hemma" directly from search results (pre-fills name)
- [x] **Materials** — Store price search: opens Byggmax / Bauhaus / Hornbach in browser for any catalog material
- [ ] **Materials** — Test live price quotes with real supplier data in Firestore
- [ ] **Budget** — Add ability to create/edit budget entries from the app (currently read-only from Firestore)
- [ ] **AI Chat** — Test Stugan AI with renovation-specific prompts

---

## App — Before Season Launch

- [ ] Set up Firebase project with production credentials (replace dev config)
- [ ] Add all team members to the Firebase project (Auth + Firestore security rules)
- [ ] Seed Firestore with initial phases and budget data for the project
- [ ] Test app on all team members' devices (Android versions)
- [ ] Enable Firebase offline persistence and test with poor connectivity at the cottage
- [ ] Add push notifications for assigned todos (optional)

---

## Renovation — Practical Todos

- [ ] Finalize phase plan and enter all phases in the Planning screen
- [ ] Assign todos to team members for each phase
- [ ] Create shopping lists per phase in the Shopping screen
- [ ] Enter material specs and get price quotes in the Materials screen
- [ ] Take "Before" photos of every room and upload to the Gallery
- [ ] Set a budget per phase in Firestore
- [ ] Add all contractors and suppliers to the Contacts screen
