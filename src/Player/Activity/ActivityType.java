package Player.Activity;

public enum ActivityType {
    BUY,
    SELL,
    BUILD,
    CONSUME;

    public static ActivityType fromName(String name) {
        for (ActivityType type : ActivityType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid activity type name: " + name);
    }
}
