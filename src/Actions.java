import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This code defines four interfaces: Buy, Build, Consume, and Sell, that can be
 * implemented by the Player class. The Buy interface defines methods for
 * creating buy orders and waiting for them to complete. The Build interface
 * extends the Buy interface and defines a method for building a product using
 * the player's stock of materials. The Consume interface extends the Buy
 * interface and defines a method for consuming a product from the player's
 * stock. The Sell interface extends the Build interface and defines a method
 * for selling a product, either from the player's existing stock or by building
 * more if necessary.
 */

interface Buy {
    /**
     * Creates a new buy order for the specified player, product, and quantity.
     *
     * @param player   the player who is placing the buy order
     * @param product  the product being bought
     * @param quantity the quantity of the product being bought
     * @return the new buy order
     */
    default Order buy(Player player, CatalogProduct product, int quantity) {
        return Order.newBuyOrder(player, product, quantity, calculateMaxPricePerUnit(player, product));

    }

    default Order buy(Player player, CatalogProduct product, int quantity, double maxPrice) {
        return Order.newBuyOrder(player, product, quantity, maxPrice);
    }

    default double calculateMaxPricePerUnit(Player player, CatalogProduct product) {
        return product.getRecommendedPrice() * (1 + player.getPriceTolerance());
    }

    /**
     * Waits for the specified buy order to complete.
     *
     * @param order the buy order to wait for
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    default void waitForBuyOrder(Order order, int timeMilliseconds) throws InterruptedException {
        order.waitUntilCompleted(timeMilliseconds, TimeUnit.MILLISECONDS);
    }
}

interface Build extends Buy {
    /**
     * Builds a product using the player's stock of materials.
     *
     * @param player   the player who is building the product
     * @param product  the product being built
     * @param quantity the quantity of the product being built
     * @throws InterruptedException if the thread is interrupted while waiting for
     *                              buy orders to complete
     */
    default void build(Player player, CatalogProduct product, int quantity) throws InterruptedException {
        if (player.getType() == Player.Type.SUPPLIER) {
            player.getStock().addProducts(product, quantity);
            return;
        }
        List<CatalogProduct.Component> components = product.getComponents();
        Map<CatalogProduct, Integer> requiredMaterials = new HashMap<>();

        // Count the required quantity of each component
        for (CatalogProduct.Component component : components) {
            CatalogProduct material = component.getProduct();
            int requiredQuantity = component.getQuantity() * quantity;
            requiredMaterials.merge(material, requiredQuantity, Integer::sum);
        }

        // Check if the player has enough materials to build the product
        Map<CatalogProduct, Integer> availableMaterials = player.getStock().getProductQuantities();
        boolean hasEnoughMaterials = true;
        ArrayList<Order> buyOrders = new ArrayList<>();
        for (Map.Entry<CatalogProduct, Integer> entry : requiredMaterials.entrySet()) {
            CatalogProduct material = entry.getKey();
            int requiredQuantity = entry.getValue();
            int availableQuantity = availableMaterials.getOrDefault(material, 0);
            if (availableQuantity < requiredQuantity) {
                // calculate the maximum price to pay for the material
                double maxBuyPrice = product.getComponentsPrice(material) / product.getComponentsPrice() * product.getRecommendedPrice();
                //System.out.println("maxBuyPrice: " + maxBuyPrice + " for " + material.getName() + " for " + product.getName() + " with components price: " + product.getComponentsPrice(material) + " and recommended price: " + product.getRecommendedPrice());
                // Not enough materials, buy more and then build
                buyOrders.add(buy(player, material, requiredQuantity - availableQuantity, maxBuyPrice));
                hasEnoughMaterials = false;
            }
        }

        if (!hasEnoughMaterials) {
            // Wait for the buy orders to complete
            while (buyOrders.size() > 0) {
                // todo: implement wait time calculation
                // waitForBuyOrder(buyOrders.get(0), calculateWaitTime(player, product));
                waitForBuyOrder(buyOrders.get(0), 20);
                buyOrders.remove(0);
            }
        }

        // Calculate how many products can be built
        int maxCanBuildQuantity = Integer.MAX_VALUE;
        for (Map.Entry<CatalogProduct, Integer> entry : requiredMaterials.entrySet()) {
            CatalogProduct material = entry.getKey();
            int requiredQuantity = entry.getValue();
            int availableQuantity = player.getStock().getProductQuantities().getOrDefault(material, 0);
            maxCanBuildQuantity = Math.min(maxCanBuildQuantity, availableQuantity / requiredQuantity);
        }
        if (maxCanBuildQuantity == Integer.MAX_VALUE) {
            return;
        }
        // Remove the required materials from the player's stock and add the built
        // product
        for (Map.Entry<CatalogProduct, Integer> entry : requiredMaterials.entrySet()) {
            CatalogProduct material = entry.getKey();
            int requiredQuantity = entry.getValue();
            player.getStock().removeProducts(material, requiredQuantity * maxCanBuildQuantity);
        }
        player.getStock().addProducts(product, maxCanBuildQuantity);
        if (maxCanBuildQuantity > 0){
            Log.getInstance().addMessage(player.getName() + " built " + maxCanBuildQuantity + " " + product.getName() + "s", Log.Level.INFO);
        }
    }

}

/**
 * The Consume interface extends the Buy interface and defines a default method
 * consume that allows a player to consume a certain quantity of a
 * CatalogProduct from their stock. If the player does not have enough stock of
 * the product, the method will attempt to buy the remaining quantity and wait
 * for the buy order to complete before consuming the product.
 */
interface Consume extends Buy {
    /**
     * Consumes a certain quantity of a CatalogProduct from a player's stock.
     *
     * @param player   the player who wants to consume the product
     * @param product  the product to be consumed
     * @param quantity the quantity of the product to consume
     * @throws InterruptedException if the thread is interrupted while waiting for a
     *                              buy order to complete
     */
    default void consume(Player player, CatalogProduct product, int quantity) throws InterruptedException {
        List<CatalogProduct.Product> products = player.getStock().getProducts(product);
        int availableQuantity = products.size();

        if (!(availableQuantity >= quantity)) {
            // Not enough stock, buy more and then consume
            waitForBuyOrder(buy(player, product, quantity - availableQuantity, calculateMaxPricePerUnit(player, product)), 20);
        }

        int consumed = player.getStock().removeProducts(product, quantity);
        if (consumed > 0)
            Log.getInstance().addMessage(player.getName() + "consumed " + consumed + " " + product.getName() + "s", Log.Level.INFO);
    }
}

/**
 * The Sell interface extends the Build interface and defines a default method
 * sell that allows a player to sell a certain quantity of a CatalogProduct from
 * their stock. If the player does not have enough stock of the product, the
 * method will attempt to build the remaining quantity and update the available
 * quantity before selling. The method then creates a new sell order for the
 * requested quantity of products or all the available products, whichever is
 * smaller.
 */
interface Sell extends Build {
    /*
     * Sells a certain quantity of a CatalogProduct from a player's stock.
     *
     * @param player the player who wants to sell the product
     *
     * @param product the product to be sold
     *
     * @param quantity the quantity of the product to sell
     *
     * @throws InterruptedException if the thread is interrupted while waiting for a
     * build order to complete
     */
    default void sell(Player player, CatalogProduct product, int quantity) throws InterruptedException {
        List<CatalogProduct.Product> products = player.getStock().getProducts(product);
        int availableQuantity = products.size();

        if (!(availableQuantity >= quantity)) {
            // Not enough products in stock, try to build
            int quantityToBuild = quantity - availableQuantity;
            build(player, product, quantityToBuild);

            // Get the updated quantity of available products
            products = player.getStock().getProducts(product);
            availableQuantity = products.size();

        }
        // Actions.Sell the requested quantity of products or all the available
        // products, whichever is smaller
        int quantityToSell = Math.min(quantity, availableQuantity);
        if (quantityToSell > 0)
            Order.newSellOrder(player, product, quantityToSell, calculateMinPricePerUnit(player, product));
    }

    default double calculateMinPricePerUnit(Player player, CatalogProduct product) {
        return product.getRecommendedPrice() * (1 - player.getPriceTolerance());
    }
}
