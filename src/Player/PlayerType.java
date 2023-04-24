package Player;

public enum PlayerType {
    MANUFACTURER,
    CONSUMER,
    SUPPLIER;

    public static PlayerType fromName(String name) {
        for (PlayerType type : PlayerType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid player type name: " + name);
    }
}
