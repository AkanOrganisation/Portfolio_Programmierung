import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Random;

public class Activity implements Buy, Sell, Build, Consume {
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

    public boolean isFinished() {
        return this.finished;
    }

    public static class Data {

        ActivityType type;
        CatalogProduct product;
        int minQuantity;
        int maxQuantity;

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
}