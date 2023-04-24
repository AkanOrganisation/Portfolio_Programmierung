package Actions;

import Catalog.CatalogProduct;
import Catalog.Product;
import Player.Player;

import java.util.List;

public interface Consume extends Buy {
    default void consume(Player player, CatalogProduct product, int quantity) throws InterruptedException {
        List<Product> products = player.stock.getProducts(product);
        int availableQuantity = products.size();

        if (!(availableQuantity >= quantity)) {
            // Not enough stock, buy more and then consume
            waitForBuyOrder(buy(player, product, quantity - availableQuantity));
        }

        player.stock.removeProducts(product, quantity);
    }
}
