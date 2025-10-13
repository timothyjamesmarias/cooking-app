# Culinary Multitool — Feature & Module Overview

A personal, native-first cooking assistant built in Kotlin (Compose Multiplatform + Spring).  
Focus: offline-first ergonomics, AI-assisted creativity, and modular expansion.

---

## 1. Core Goals

- Serve as a **personal culinary intelligence tool** (not a social app).
- Function **offline-first**, with optional backend sync.
- Use **Kotlin end-to-end** for shared logic and learning.
- Integrate with **LLM services** for contextual cooking assistance.
- Provide a **consistent, themed UI** via a custom design system.
- Enable gradual expansion into a full-fledged product if desired.

---

## 2. High-Level Architecture

| Layer | Technology | Notes                                               |
|-------|-------------|-----------------------------------------------------|
| UI | Compose Multiplatform (Desktop + Mobile) | Native-first, shared code                           |
| Shared Logic | Kotlin Multiplatform shared module | Recipes, pantry logic, conversions                  |
| Backend | Spring Boot | LLM orchestration, sync, semantic search, api layer |
| Persistence | SQLDelight / Room | Offline cache and state                             |
| AI | OpenAI API / Local LLM | Structured JSON prompts, caching                    |

---

## 3. Core Modules

### 3.1 Recipe Library
**Purpose:** Central repository for all personal recipes.

- CRUD for recipes (title, ingredients, steps, tags)
- Recipe parsing (text → structured format via LLM)
- Import from websites or plaintext
- Scaling (serving size adjustment)
- Image attachments
- Favorite and tag system

---

### 3.2 Pantry Tracker
**Purpose:** Maintain an up-to-date list of owned ingredients.

- Track items, quantities, units, expiration dates
- Manual or barcode-based entry
- Smart categorization (fridge, freezer, pantry)
- Integration with Recipe Library: “What can I make?”
- Expiration reminders (local notifications)

---

### 3.3 Shopping List
**Purpose:** Generate grocery lists from recipes and meal plans.

- Auto-generate from selected recipes
- Merge and normalize duplicate items
- Unit conversions (oz → g → ml)
- Group by category (produce, dairy, etc.)
- Mark purchased items locally
- Export to text / share sheet

---

### 3.4 Substitution Engine
**Purpose:** Suggest alternatives when ingredients are missing.

- Static substitution database (common pairs)
- Contextual suggestions via LLM (based on recipe)
- Explain reasoning: “Why this works”
- Flavor similarity model (embedding-based)

---

### 3.5 Cooking Assistant
**Purpose:** Interactive step-by-step guidance during cooking.

- Step viewer with progress indicators
- Timers (multi-step parallel)
- Voice input/output (“Next step,” “How long left?”)
- Offline speech support (optional)
- Adaptive layout for hands-free kitchen use

---

### 3.6 Meal Planner
**Purpose:** Plan meals and automate grocery/recipe organization.

- Calendar integration (per day/meal slot)
- Weekly overview
- Auto-generate shopping list
- Nutrition summary (optional)
- “Regenerate next week’s plan” via LLM

---

### 3.7 Knowledge & Insight Layer
**Purpose:** Centralized semantic memory for recipes and history.

- Vector store of recipe embeddings
- Search: “Find recipes with cardamom + orange”
- Historical insight: “What did I cook last winter?”
- Optional backend sync for persistent state

---

## 4. Auxiliary Modules

### 4.1 Design System
- Custom `CulinaryTheme` extending MaterialTheme
- Figma Tokens → JSON → Kotlin theme codegen
- Three variants: Light, Dark, Seasonal
- Token categories: color, typography, spacing, shape
- AI-assisted palette generation (via MCP or CLI)

### 4.2 AI / LLM Integration
- Schema-based structured prompts
- Use backend as proxy for LLM calls
- Caching for repeated requests
- Examples:
    - “Make this vegan”
    - “Suggest sides for roast chicken”
    - “Explain this step in simpler terms”

### 4.3 Sync & Backup (optional)
- Cloud sync via Spring Boot API
- Local-first data model with background merging
- JSON export/import for user data

---

## 5. MVP Scope

Minimum viable product should include:

- Recipe Library (CRUD + import)
- Pantry Tracker (manual entry + “What can I make?”)
- Substitution Engine (static + basic LLM)
- Basic `CulinaryTheme` (one color scheme)
- Offline persistence
- Compose Desktop + Android support

Stretch goals:
- Shopping List auto-generation
- Step-by-step Assistant
- Light/Dark themes

---

## 6. Future Extensions

- Nutritional database integration
- Smart kitchen device support (IoT timers, scales)
- Family sharing / multi-user sync
- “Culinary Journal” mode (meal logging)
- Seasonal theme switching (AI-assisted)
- Recipe graph visualization (ingredients ↔ flavor network)

---

## 7. Development Priorities

1. Shared data model & serialization schema
2. Compose UI foundation + theme
3. Recipe & pantry core logic
4. Offline DB + LLM integration
5. MVP release for personal daily use
6. Polish + design iterations
7. Evaluate productization potential

---

## 8. Guiding Principles

- **Local-first**: must remain useful offline.
- **Ergonomic**: frictionless UX for in-kitchen use.
- **Aesthetic**: cohesive, calm, timeless visual tone.
- **Extensible**: codebase designed for modular feature growth.
- **Educational**: serve as a learning lab for Kotlin, Compose, and AI integration.

---

*Document version: 2025-10-12*