# Orme Design System

## 1. Atmosphere & Identity

Orme is a warm travel journal: calm cream paper, deep botanical green, and
editorial orange headings. The signature is a horizontal photo rail that lets
destinations feel like postcards rather than utility list rows.

## 2. Color

| Role | Token | Value | Usage |
|---|---|---|---|
| Surface / page | `OrmeCream` | `#F2EDD5` | Screen and navigation background |
| App frame / outer gutter | `OrmeShell` | `#F7F7F7` | Recommendation screen outer frame |
| Surface / search | `OrmeSearchSurface` | `#F6F1DE` | Search field and soft controls |
| Text / brand | `OrmeGreen` | `#084A24` | Location, navigation, primary text |
| Accent / heading | `OrmeOrange` | `#F26716` | Editorial section headings |
| Accent / favorite | `OrmeHeartRed` | `#E9362C` | Favorite actions and selected hearts |
| Border / soft | `OrmeSoftBorder` | `#9BA68E` | Favorite-list affordance outline |
| Text / muted | `OrmeMutedText` | `#7B765F` | Placeholder and secondary metadata |

Colors are defined in `ui/theme/Color.kt`; screen code must use those tokens.

## 3. Typography

- Display and section headings: bundled `PlayfairDisplay`, bold/black.
- Functional text: Compose default sans-serif.
- Destination names: Playfair Display, 30sp, black.
- Section headings: Playfair Display, 18sp, black.
- Supporting metadata: sans-serif, 11sp, bold.

## 4. Spacing & Layout

- Base unit: 4dp.
- Screen gutter: 22-24dp.
- Search field height: 44dp with 24dp radius.
- Destination card: 273dp wide, 244dp tall, 24dp radius.
- Cards use horizontal rails with 18dp gaps; the page scrolls vertically.
- Bottom navigation remains owned by `Scaffold` and stays visible.

## 5. Components

### DestinationCard
- **Structure**: image, bottom readability gradient, favorite button, distance, destination name.
- **Variants**: recommended and nearby rails; favorite state.
- **States**: default, favorite, pressed, focus semantics, loading placeholder.
- **Accessibility**: card favorite action exposes destination and favorite state.

### SearchField
- **Structure**: rounded surface, search icon, editable single-line text.
- **States**: empty, focused, populated, no-results.
- **Accessibility**: labeled as destination search.

### FavoriteAction
- **Structure**: heart glyph inside a tappable surface.
- **Variants**: card overlay and header action.
- **States**: unselected, selected, pressed.
- **Accessibility**: button label includes destination or list purpose.

### RecommendationScreen
- **Frame**: `OrmeCream` fills the content surface edge-to-edge; no literal
  `Search` heading or white outer gutter is rendered.
- **Location row**: a 42dp row placed 40dp below the content top; the green
  pin, bold `Gunsan` label, and small green chevron share one vertical center.
- **Search control**: cream rounded pill with a green magnifier, muted
  `Where to next?` placeholder, soft elevation, and a separate red heart
  action.
- **Destination rails**: orange Playfair section headings, 300dp × 260dp
  image cards, 24dp radius, 16dp rail gap, 21dp rail inset, bottom gradient,
  white metadata/title, and white card-heart outlines.
- **Interaction states**: search filters both rails, card hearts toggle saved
  state, and the header heart opens saved destinations.

### DiaryRecordRail
- **Frame**: cream regional records surface with a horizontal cover rail,
  90% opaque so the map subtly shows through, preserved
  representative-photo action, and bottom add-record action.
- **Cards**: centered 210dp × 294dp rounded cover cards expose the saved
  record name directly below the cover; the next card remains partially
  visible with a 20dp gap while swiping, and covers fit without cropping.
- **Motion**: while swiping, the card nearest the rail center smoothly scales
  up to 1.14×; cards one card-gap away settle to 0.94×.
- **Creation**: the add-record action asks for a non-empty display name before
  cover/page selection and persists it beside the saved record assets.

### SocialProviderButton
- **Structure**: provider symbol and label centered in a 45dp, 12dp-radius full-width row.
- **Variants**: Google white, Naver green, Kakao yellow (`#FEE500`).
- **Accessibility**: every row exposes its provider and login action.

### DiaryCanvasElement
- **Text entry**: the text tool inserts a focused field directly on the page; it is not a modal flow.
- **Gestures**: one-finger drag moves text, photo, and emoji elements. Two-finger pinch resizes them uniformly from 0.5× through 3×.

### ProfileScreen
- **Structure**: editorial `Orme` header, circular profile image, account sections, and bottom logout action.
- **States**: photo placeholder and selected photo; all account rows are display-only in the current prototype.
- **Interaction**: only the profile-photo add button and logout action are interactive.

## 6. Motion & Interaction

- Favorite toggles use Compose press feedback only; no decorative animation.
- Navigation uses standard Compose navigation transitions.
- Search updates results as text changes.
- Reduced motion is respected by avoiding custom motion.
- Diary canvas edits use direct touch only; pinch scale has no animated settling state.

## 7. Depth & Surface

Use warm tonal surfaces with one soft shadow on the search field. Photo cards
are edge-to-edge with a bottom gradient for readable white labels.

## 8. Accessibility Constraints & Accepted Debt

- Interactive controls are at least 44dp where practical and expose semantic
  labels and selected state.
- White text sits on darkened photo gradients; brand green sits on cream.
- Accepted debt: favorites are in-memory for this prototype and are lost when
  the process is killed; persist them when account-backed travel data exists.
