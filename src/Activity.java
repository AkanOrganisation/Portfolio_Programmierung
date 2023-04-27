
/**

 This class represents an Activity that a Player can execute in the game.
 It implements the Buy, Sell, Build, and Consume interfaces.
 */
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Random;

public class Activity implements Buy, Sell, Build, Consume {
    /**
     * The player who is executing the Activity
     */
    private final Player player;

    /**
     * The type of Activity being executed
     */
    private final ActivityType type;

    /**
     * The CatalogProduct associated with the Activity
     */
    private final CatalogProduct product;

    /**
     * The minimum quantity of the CatalogProduct that can be involved in the Activity
     */
    private final int minQuantity;

    /**
     * The maximum quantity of the CatalogProduct that can be involved in the Activity
     */
    private final int maxQuantity;

    /**
     * A boolean representing whether the Activity has finished executing
     */
    private boolean finished;

    /**
     * Constructs an Activity with the specified parameters.
     *
     * @param player      the player executing the Activity
     * @param type        the type of Activity being executed
     * @param product     the CatalogProduct associated with the Activity
     * @param minQuantity the minimum quantity of the CatalogProduct involved in the Activity
     * @param maxQuantity the maximum quantity of the CatalogProduct involved in the Activity
     */

    private final Player player;
    private final ActivityType type;
    private final CatalogProduct product;
    private final int minQuantity;
    private final int maxQuantity;
    private boolean finished;


    public Activity(Player player, ActivityType type, CatalogProduct product, int minQuantity, int maxQuantity) {
        this.player = player;
        this.type = type;
        this.product = product;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.finished = false;

    }
    /**
     * Executes the Activity, involving a random quantity of the CatalogProduct.
     * Calls the appropriate method depending on the type of Activity being executed.
     *
     * @throws InterruptedException if the Activity is interrupted
     */
    public void execute() throws InterruptedException {
        int quantity = new Random().nextInt(maxQuantity - minQuantity) + minQuantity;
        switch (type) {
            case BUY -> buy(player, product, quantity);
            case SELL -> sell(player, product, quantity);
            case BUILD -> build(player, product, quantity);
            case CONSUME -> consume(player, product, quantity);
            default -> throw new IllegalArgumentException("Invalid activity type: " + type);
        }
        this.finished = true;
    }
/**
 * Returns a boolean representing whether the Activity has finished executing.
 *
 * @return true if the Activity has finished executing, false otherwise
 */
    publ
    public boolean isFinished() {
        return this.finished;
    }
    /**
     * A nested class representing the data needed to construct an Activity.
     */
    public static class Data {

        /**
         * The type of Activity being executed
         */
        ActivityType type;

        /**
         * The CatalogProduct associated with the Activity
         */
        CatalogProduct product;

        /**
         * The minimum quantity of the CatalogProduct involved in the Activity
         */
        int minQuantity;

        /**
         * The maximum quantity of the CatalogProduct involved in the Activity
         */
        int maxQuantity;
        /**
         * Constructs a Data object with the specified parameters.
         *
         * @param type        the type of Activity being executed
         * @param product     the CatalogProduct associated with the Activity
         * @param minQuantity the minimum quantity of the CatalogProduct involved in the Activity
         * @param maxQuantity the maximum quantity of the CatalogProduct involved in the Activity
         */
        @JsonCreator
        Data(@JsonProperty("type") String type,
             @JsonProperty("product") String product,
             @JsonProperty("min") int minQuantity,
             @JsonProperty("max") int maxQuantity) {
            this.type = ActivityType.fromName(type);
            this.product = CatalogProduct.getProductByName(product);
            this.minQuantity = minQuantity;
            this.maxQuantity = maxQuantity;
        }

        public ActivityType getType() {
            return type;
        }

        public CatalogProduct getProduct() {
            return product;
        }

        public int getMinQuantity() {
            return minQuantity;
        }

        public int getMaxQuantity() {
            return maxQuantity;
        }
    }

    /**

     This class represents an enumerated type of activity types that can be performed.
     The activity types are BUY, SELL, BUILD, and CONSUME.
     This class also contains a static method to convert a string representation of the activity type to its corresponding enumeration value.
     */
    public enum ActivityType {
        BUY,
        SELL,
        BUILD,
        CONSUME;
        /**
         This static method converts a string representation of an activity type to its corresponding enumeration value.
         @param name a string representation of the activity type to be converted.
         @return the corresponding enumeration value of the activity type.
         @throws IllegalArgumentException if the string representation does not match any of the defined activity types.
         */
        public static ActivityType fromName(String name) {
            for (ActivityType type : ActivityType.values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid activity type name: " + name);
        }
    }
}
