# 🧪 Testing Guide - Reset & Exit Features

## Quick Test Checklist

### ✅ Test 1: Basic Query → Reset Workflow
**Steps:**
1. Launch Wide-Path Pro
2. Verify welcome message appears
3. Enter query parameters:
   - Source: 100
   - Destination: 500
   - Departure: 450
   - Interval: 360
   - Budget: 45
4. Click "▶ Run Query"
5. Wait for results
6. Verify action buttons appear:
   - "🔄 New Query" (Blue)
   - "🚪 Exit System" (Red)
7. Verify input fields are disabled
8. Click "🔄 New Query"
9. Verify:
   - Welcome message redisplays
   - All fields reset to defaults
   - Input fields re-enabled
   - Original buttons restored

**Expected Result:** ✅ System cleanly resets for new query

---

### ✅ Test 2: Exit with Confirmation
**Steps:**
1. Run a query (any valid parameters)
2. After results appear, click "🚪 Exit System"
3. Verify confirmation dialog appears with:
   - 🚪 Icon
   - "Exit Wide-Path Pro?" title
   - Warning message
   - [Exit] and [Cancel] buttons
4. Click [Cancel]
5. Verify application remains open
6. Click "🚪 Exit System" again
7. Click [Exit]
8. Verify:
   - Fade-out animation plays
   - Application closes gracefully

**Expected Result:** ✅ Graceful shutdown with user confirmation

---

### ✅ Test 3: Menu Exit
**Steps:**
1. Launch application
2. Click menu: File → Exit
3. Verify same confirmation dialog appears
4. Test both [Cancel] and [Exit] options

**Expected Result:** ✅ Consistent exit behavior

---

### ✅ Test 4: Window Close Button
**Steps:**
1. Launch application
2. Click X (close button) on window
3. Verify confirmation dialog appears
4. Test [Cancel] - window stays open
5. Click X again
6. Click [Exit] - window closes

**Expected Result:** ✅ Confirmation prevents accidental closure

---

### ✅ Test 5: Success Animation
**Steps:**
1. Run a successful query
2. Observe status bar
3. Count flash animations (should be ~6 flashes)
4. Verify green success color appears

**Expected Result:** ✅ Visual feedback confirms success

---

### ✅ Test 6: Multiple Query Cycles
**Steps:**
1. Run Query 1 (Source: 10, Dest: 20)
2. Click "🔄 New Query"
3. Run Query 2 (Source: 30, Dest: 40)
4. Click "🔄 New Query"
5. Run Query 3 (Source: 50, Dest: 60)
6. Verify each reset works correctly

**Expected Result:** ✅ System handles multiple cycles without issues

---

### ✅ Test 7: Failed Query → Reset
**Steps:**
1. Enter invalid parameters (e.g., Source = Destination)
2. Run query
3. Verify error message displays
4. Verify action buttons still appear
5. Click "🔄 New Query"
6. Verify system resets properly

**Expected Result:** ✅ Reset works even after failed queries

---

### ✅ Test 8: Visualization Reset
**Steps:**
1. Run a query with valid path
2. Switch to "🗺️ Visualization" tab
3. Verify path is displayed
4. Switch back to results
5. Click "🔄 New Query"
6. Switch to visualization tab
7. Verify map is cleared (shows "No path to display")

**Expected Result:** ✅ Visualization clears on reset

---

### ✅ Test 9: Metrics Persistence
**Steps:**
1. Run Query 1
2. Note metrics in "📈 Metrics" tab
3. Click "🔄 New Query"
4. Check metrics tab
5. Run Query 2
6. Verify metrics updated

**Expected Result:** ✅ Metrics accumulate across resets

---

### ✅ Test 10: History Tracking
**Steps:**
1. Run Query 1
2. Check "🕐 History" tab
3. Click "🔄 New Query"
4. Run Query 2
5. Check history tab
6. Verify both queries are recorded

**Expected Result:** ✅ History persists across resets

---

## 🎨 Visual Elements to Verify

### Color Verification
- ✅ Run Query button: Green (#4CAF50)
- ✅ Clear Fields button: Gray (#9E9E9E)
- ✅ New Query button: Blue (#2196F3)
- ✅ Exit System button: Red (#F44336)

### Animation Verification
- ✅ Success flash: 6 pulses over ~600ms
- ✅ Fade out on exit: Smooth opacity decrease
- ✅ Panel transitions: Smooth button switching

### Text Verification
- ✅ Welcome message displays on startup
- ✅ Welcome message redisplays after reset
- ✅ Status messages update correctly:
  - "Executing query..."
  - "Query completed successfully in X ms"
  - "Resetting query session..."
  - "Ready for new query. All fields reset."
  - "Shutting down Wide-Path Pro..."

---

## 🐛 Edge Cases to Test

### Edge Case 1: Rapid Clicking
**Test:** Click "🔄 New Query" multiple times rapidly  
**Expected:** System handles gracefully, no crashes

### Edge Case 2: Exit During Query
**Test:** Start a query, try to exit while it's running  
**Expected:** Either blocks exit or handles safely

### Edge Case 3: Keyboard Shortcuts
**Test:** Press Ctrl+Enter after reset  
**Expected:** Runs new query correctly

### Edge Case 4: Window Resize During Animation
**Test:** Resize window during fade-out  
**Expected:** Animation completes properly

### Edge Case 5: Tab Switching During Reset
**Test:** Switch tabs while reset is processing  
**Expected:** No visual glitches

---

## 📊 Performance Checks

| Operation | Expected Time | Notes |
|-----------|---------------|-------|
| Reset Query | < 500ms | Includes 300ms pause for effect |
| Exit Animation | ~1000ms | Fade-out over 10 steps |
| Button Toggle | < 50ms | Instant visual feedback |
| Welcome Display | < 100ms | Text rendering |
| Success Flash | ~600ms | 6 pulses at 100ms each |

---

## 🎯 User Experience Validation

### UX Checklist:
- [ ] All buttons have appropriate hover effects
- [ ] Cursor changes to pointer over buttons
- [ ] Disabled inputs appear grayed out
- [ ] Status messages are clear and informative
- [ ] Confirmation dialog is not intrusive
- [ ] Animation timing feels natural (not too fast/slow)
- [ ] Color scheme is consistent and professional
- [ ] Icons enhance understanding of button function
- [ ] No UI elements overlap or misalign
- [ ] All text is readable and properly formatted

---

## 🚀 Automated Test Script (Pseudocode)

```java
// Test Suite for Reset & Exit Features
class ResetExitTest {
    
    @Test
    void testCompleteResetCycle() {
        // Launch app
        GuiLauncher app = new GuiLauncher();
        app.start();
        
        // Run query
        QueryParameters params = new QueryParameters(10, 20, 450, 360, 45);
        app.executeQuery(params);
        
        // Wait for completion
        waitFor(() -> !app.isQueryRunning());
        
        // Verify action buttons visible
        assertTrue(app.getInputPanel().isResetButtonVisible());
        assertTrue(app.getInputPanel().isExitButtonVisible());
        
        // Reset
        app.resetQuery();
        
        // Verify state
        assertTrue(app.getInputPanel().areInputsEnabled());
        assertTrue(app.getOutputPane().getText().contains("WIDE-PATH PRO - READY"));
    }
    
    @Test
    void testExitConfirmation() {
        GuiLauncher app = new GuiLauncher();
        app.start();
        
        // Trigger exit
        app.exitApplication();
        
        // Verify dialog shown
        assertTrue(DialogManager.isConfirmationDialogVisible());
        
        // Cancel
        DialogManager.clickCancel();
        assertTrue(app.isRunning());
        
        // Exit again and confirm
        app.exitApplication();
        DialogManager.clickExit();
        
        // Wait for graceful shutdown
        waitFor(() -> !app.isRunning());
    }
}
```

---

## 📝 Bug Report Template

If you encounter issues, report using this format:

```
**Issue Title:** [Brief description]

**Steps to Reproduce:**
1. Step 1
2. Step 2
3. Step 3

**Expected Behavior:**
[What should happen]

**Actual Behavior:**
[What actually happens]

**Screenshots:**
[If applicable]

**Environment:**
- OS: Windows/Mac/Linux
- Java Version: 21
- Wide-Path Version: 2.0

**Severity:** Critical / High / Medium / Low

**Additional Notes:**
[Any other relevant information]
```

---

## ✨ Success Criteria

The feature is fully functional when:
1. ✅ All 10 test cases pass
2. ✅ All edge cases handled gracefully
3. ✅ Performance meets expectations
4. ✅ UX checklist fully validated
5. ✅ No memory leaks after multiple resets
6. ✅ Animations are smooth on target hardware
7. ✅ User feedback is clear and timely
8. ✅ System state is consistent after operations

---

*Test early, test often! Quality is our priority.* 🎯
