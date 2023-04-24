package Actions;

import Catalog.CatalogProduct;
import Catalog.Product;
import Order.SellOrder;
import Player.Player;

import java.util.List;

public interface Sell extends Build {
    default void sell(Player player, CatalogProduct product, int quantity) throws InterruptedException {
        List<Product> products = player.stock.getProducts(product);
        int availableQuantity = products.size();

        if (!(availableQuantity >= quantity)) {
            // Not enough products in stock, try to build
            int quantityToBuild = quantity - availableQuantity;
            build(player, product, quantityToBuild);

            // Get the updated quantity of available products
            products = player.stock.getProducts(product);
            availableQuantity = products.size();

        }
        // Actions.Sell the requested quantity of products or all the available products, whichever is smaller
        int quantityToSell = Math.min(quantity, availableQuantity);
        if (quantityToSell > 0)
            new SellOrder(player, product, quantityToSell);
    }
}
