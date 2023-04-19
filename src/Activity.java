import java.util.Random;

public class Activity implements Buy, Sell, Build, Consume {
    private Player player;
    private ActivityType type;
    private CatalogProduct product;
    private int minQuantity;
    private int maxQuantity;
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
        int quantity = new Random().nextInt(maxQuantity - minQuantity ) + minQuantity;
        switch (type) {
            case BUY:
                buy(player, product, quantity);
                break;
            case SELL:
                sell(player, product, quantity);
                break;
            case BUILD:
                build(player, product, quantity);
                break;
            case CONSUME:
                consume(player, product, quantity);
                break;
            default:
                throw new IllegalArgumentException("Invalid activity type: " + type);
        }
        this.finished = true;
    }

    public boolean isFinished() {
        return this.finished;
    }
}
