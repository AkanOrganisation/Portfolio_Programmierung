package Actions;

import Catalog.CatalogProduct;
import Order.BuyOrder;
import Player.Player;

import java.util.concurrent.TimeUnit;

public interface Buy {
    default BuyOrder buy(Player player, CatalogProduct product, int quantity) {
        return new BuyOrder(player, product, quantity);
    }

    default BuyOrder waitForBuyOrder(BuyOrder order) throws InterruptedException {
        order.completed.await(100, TimeUnit.MILLISECONDS);
        return order;
    }
}
