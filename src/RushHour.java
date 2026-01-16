/**
 * Represents a rush hour time period.
 */
public class RushHour {
    private int startMinutes;
    private int endMinutes;
    
    public RushHour(int startMinutes, int endMinutes) {
        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;
    }
    
    public int getStartMinutes() {
        return startMinutes;
    }
    
    public int getEndMinutes() {
        return endMinutes;
    }
    
    public boolean isInRushHour(int timeMinutes) {
        return timeMinutes >= startMinutes && timeMinutes < endMinutes;
    }
}
