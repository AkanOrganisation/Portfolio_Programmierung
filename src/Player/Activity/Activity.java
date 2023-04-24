package Player.Activity;

import Actions.Build;
import Actions.Buy;
import Actions.Consume;
import Actions.Sell;
import Catalog.CatalogProduct;
import Player.Player;

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
        int quantity = new Random().nextInt(maxQuantity - minQuantity ) + minQuantity;
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
}
