# FlexiRoute Navigator - Design System

## Color Palette

### Light Theme
```
Primary:       #2196F3 (Blue)
Primary Dark:  #1976D2
Primary Light: #64B5F6
Accent:        #FF5722 (Deep Orange)
Success:       #4CAF50 (Green)
Error:         #F44336 (Red)
Warning:       #FFC107 (Amber)
Info:          #03A9F4 (Light Blue)
Background:    #FAFAFA
Panel:         #F5F5F5
Foreground:    #212121
Border:        #E0E0E0
```

### Dark Theme
```
Primary:       #42A5F5
Primary Dark:  #2979FF
Accent:        #FF7043
Success:       #66BB6A
Error:         #EF5350
Warning:       #FFCA28
Background:    #121212
Panel:         #212121
Foreground:    #EEEEEE
Border:        #424242
```

### Oceanic Theme
```
Primary:       #0096C7 (Ocean Blue)
Accent:        #FF8A65
Background:    #0F2027
Panel:         #17303C
Foreground:    #ECEFf1
```

### Forest Theme
```
Primary:       #388E3C (Forest Green)
Accent:        #FF9800
Background:    #1B261D
Panel:         #263228
Foreground:    #E8F5E9
```

### Sunset Theme
```
Primary:       #F4511E (Sunset Orange)
Accent:        #9C27B0
Background:    #211619
Panel:         #302023
Foreground:    #FFF5EE
```

## Typography

### Font Families
- **Primary**: Segoe UI
- **Monospace**: Consolas, Monaco
- **Emoji**: Segoe UI Emoji

### Font Sizes
- **H1**: 28px, Bold
- **H2**: 20px, Bold
- **H3**: 16px, Bold
- **Body**: 13px, Regular
- **Small**: 11px, Regular
- **Caption**: 10px, Regular

### Font Weights
- **Regular**: 400
- **Bold**: 700

## Spacing

### Grid System
Base unit: 8px

- **xs**: 4px
- **sm**: 8px
- **md**: 16px
- **lg**: 24px
- **xl**: 32px
- **xxl**: 48px

## Components

### Buttons

#### Primary Button
```
Background: Primary color
Text: White
Height: 40-45px
Padding: 12px 24px
Border Radius: 4px
Font: Bold, 13px
Shadow: 0 2px 4px rgba(0,0,0,0.2)
Hover: Elevation +2dp
Active: Elevation -1dp
```

#### Secondary Button
```
Background: Transparent
Text: Primary color
Border: 2px solid primary
Height: 40-45px
Padding: 12px 24px
Border Radius: 4px
```

### Cards

```
Background: Panel color
Border: 1px solid border color
Border Radius: 8-15px
Shadow: 0 2px 8px rgba(0,0,0,0.1)
Padding: 20px
Hover: Shadow increases to 0 4px 16px
```

### Input Fields

```
Height: 35-40px
Border: 1px solid border color
Border Radius: 4px
Padding: 8px 12px
Font: 13px Regular
Focus: Border color changes to primary
```

### Panels

```
Background: Panel color
Border: Optional
Padding: 15-20px
Margin: 10-15px between panels
```

## Elevation (Shadows)

```
Level 1: 0 1px 2px rgba(0,0,0,0.12)
Level 2: 0 2px 4px rgba(0,0,0,0.16)
Level 3: 0 4px 8px rgba(0,0,0,0.20)
Level 4: 0 8px 16px rgba(0,0,0,0.24)
Level 5: 0 16px 24px rgba(0,0,0,0.28)
```

## Animations

### Timing Functions
- **Standard**: ease-in-out
- **Deceleration**: cubic-bezier(0.0, 0.0, 0.2, 1)
- **Acceleration**: cubic-bezier(0.4, 0.0, 1, 1)
- **Sharp**: cubic-bezier(0.4, 0.0, 0.6, 1)

### Durations
- **Fast**: 150ms
- **Normal**: 300ms
- **Slow**: 500ms
- **Very Slow**: 800ms

### Common Animations
- Fade in/out
- Slide in/out
- Scale up/down
- Elevation changes
- Color transitions

## Icons

### Style
- Emoji-based for cross-platform consistency
- 16-24px size
- Aligned with text baseline

### Common Icons
- 🎯 Target/Source
- 🏁 Destination/Goal
- 🕐 Time
- ⏱ Duration
- 💰 Budget
- 📊 Statistics
- 📈 Charts
- 🗺️ Map
- 🔍 Search
- ⚙️ Settings
- ✓ Success
- ✕ Error
- ⚠ Warning
- ℹ Information

## States

### Default
Normal appearance

### Hover
- Cursor: pointer
- Slight color change
- Elevation increase (for cards)

### Active/Pressed
- Elevation decrease
- Darker color

### Focus
- Border highlight (primary color)
- Outline for accessibility

### Disabled
- Opacity: 0.5
- Cursor: not-allowed
- Grayscale (optional)

## Accessibility

### Color Contrast
- Text on background: ≥4.5:1 (WCAG AA)
- Large text: ≥3:1
- Interactive elements: ≥3:1

### Touch Targets
- Minimum size: 44x44px
- Spacing between targets: ≥8px

### Focus Indicators
- Visible outline
- 2-3px width
- High contrast color

## Best Practices

1. **Consistency**: Use design tokens throughout
2. **Hierarchy**: Clear visual hierarchy with size, weight, color
3. **Whitespace**: Generous spacing for readability
4. **Contrast**: Ensure sufficient contrast ratios
5. **Feedback**: Immediate visual response to actions
6. **Error Prevention**: Validate inputs early
7. **Progressive Disclosure**: Show advanced options on demand
8. **Responsiveness**: Adapt to different screen sizes
