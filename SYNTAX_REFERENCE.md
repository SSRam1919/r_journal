# Quick Notes - Syntax Reference Card

## 📝 Content Formatting

### ✅ Checklists
```
Syntax:     [ ] Unchecked item
Renders:    ☐ Unchecked item

Syntax:     [x] Checked item
Renders:    ☑️ Checked item (muted, strikethrough)
```

### • Bullet Lists
```
Syntax:     - Bullet item
Syntax:     * Bullet item  
Syntax:     • Bullet item
Renders:    • Bullet item
```

### 🔢 Numbered Lists
```
Syntax:     1. First item
Syntax:     2. Second item
Syntax:     3. Third item
Renders:    1. First item
            2. Second item
            3. Third item
```

### 📄 Regular Text
```
Syntax:     Any text without special prefix
Renders:    As-is
```

## 🎨 Color Palette

| Color | Hex | Use Case |
|-------|-----|----------|
| White | #FFFFFF | Default, general notes |
| Red | #F28B82 | Urgent, important |
| Orange | #FBBC04 | Warnings, attention |
| Yellow | #FFF475 | Highlights, reminders |
| Light Green | #CCFF90 | Completed, success |
| Cyan | #A7FFEB | Ideas, brainstorming |
| Light Blue | #CBF0F8 | Information, reference |
| Blue | #AECBFA | Work, professional |
| Purple | #D7AEFB | Personal, creative |
| Pink | #FDCFE8 | Fun, social |
| Brown | #E6C9A8 | Recipes, home |
| Gray | #E8EAED | Archive, low priority |

## 🔀 Layout Toggle

| Layout | Icon | Best For |
|--------|------|----------|
| Masonry | ⊞ Grid | Many short notes, visual browsing |
| List | ☰ Agenda | Fewer long notes, chronological |

## ⌨️ Quick Actions

| Action | Method |
|--------|--------|
| New Note | Tap FAB (+) |
| Edit Note | Tap note card |
| Delete Note | Tap trash icon on card |
| Save | Tap ✓ checkmark |
| Cancel | Tap ← back arrow |
| Search | Type in search bar |
| Toggle Layout | Tap layout icon (top-right) |

## 💡 Examples

### Shopping List
```
[ ] Groceries
  - Milk
  - Bread
[ ] Pharmacy
[x] Bank
```

### Project Tasks
```
1. Research
2. Design
3. Develop
[ ] Testing
[ ] Deploy
```

### Meeting Notes
```
Attendees:
- John
- Sarah

Action Items:
[ ] John: Update docs
[ ] Sarah: Review code
```

### Mixed Content
```
Project: Website Redesign

Goals:
- Modern design
- Fast performance
- Mobile-first

Tasks:
[ ] Wireframes
[ ] Mockups
[x] Client approval

Timeline:
1. Week 1: Research
2. Week 2-3: Design
3. Week 4-6: Development
```

## 🎯 Tips

1. **Space after brackets**: `[ ]` not `[]`
2. **Case insensitive**: `[x]` or `[X]` both work
3. **Mix freely**: Combine all types in one note
4. **Colors persist**: Saved with the note
5. **Layout persists**: Your preference is remembered

## 🚫 Common Mistakes

| ❌ Wrong | ✅ Correct |
|---------|-----------|
| `[]` | `[ ]` (space after bracket) |
| `[X]Task` | `[x] Task` (space after bracket) |
| `-Item` | `- Item` (space after dash) |
| `1.Item` | `1. Item` (space after period) |

## 📱 UI Elements

```
┌─────────────────────────────────────┐
│ Quick Notes              [Grid Icon]│  ← TopAppBar
├─────────────────────────────────────┤
│ 🔍 Search notes...                  │  ← Search Bar
├─────────────────────────────────────┤
│ ┌──────────┐ ┌──────────┐          │
│ │ Note 1   │ │ Note 2   │          │  ← Masonry Grid
│ │ [Color]  │ │ [Color]  │          │
│ │          │ │          │          │
│ └──────────┘ │          │          │
│              └──────────┘          │
│ ┌──────────┐ ┌──────────┐          │
│ │ Note 3   │ │ Note 4   │          │
│ │ [Color]  │ │ [Color]  │          │
│ └──────────┘ └──────────┘          │
│                                     │
│                          [+]        │  ← FAB
└─────────────────────────────────────┘
```

## 🔧 Technical Details

- **Storage**: Local SQLite database (Room)
- **Preferences**: DataStore
- **Layout**: Jetpack Compose
- **Grid**: LazyVerticalStaggeredGrid (2 columns)
- **Performance**: Lazy loading, stable keys
- **Offline**: 100% offline, no network required

---

**Print this card for quick reference! 📄**
