import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


interface Buy {
    default Order buy(Player player, CatalogProduct product, int quantity) {
        return Order.newBuyOrder(player, product, quantity);
    }

    default void waitForBuyOrder(Order order) throws InterruptedException {
        order.waitUntilCompleted(100, TimeUnit.MILLISECONDS);
    }
}

interface Build extends Buy {
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
                // Not enough materials, buy more and then build
                buyOrders.add(buy(player, material, requiredQuantity - availableQuantity));
                hasEnoughMaterials = false;
            }
        }

        if (!hasEnoughMaterials) {
            // Wait for the buy orders to complete
            while (buyOrders.size() > 0) {
                waitForBuyOrder(buyOrders.get(0));
                buyOrders.remove(0);
            }
        }

        // Calculate how many products can be built
        int maxQuantity = Integer.MAX_VALUE;
        for (Map.Entry<CatalogProduct, Integer> entry : requiredMaterials.entrySet()) {
            CatalogProduct material = entry.getKey();
            int requiredQuantity = entry.getValue();
            int availableQuantity = player.getStock().getProductQuantities().getOrDefault(material, 0);
            maxQuantity = Math.min(maxQuantity, availableQuantity / requiredQuantity);
        }
        if (maxQuantity == Integer.MAX_VALUE) {
            return;
        }
        // Remove the required materials from the player's stock and add the built product
        for (Map.Entry<CatalogProduct, Integer> entry : requiredMaterials.entrySet()) {
            CatalogProduct material = entry.getKey();
            int requiredQuantity = entry.getValue();
            player.getStock().removeProducts(material, requiredQuantity * maxQuantity);
        }
        player.getStock().addProducts(product, maxQuantity);
    }
}

interface Consume extends Buy {
    default void consume(Player player, CatalogProduct product, int quantity) throws InterruptedException {
        List<CatalogProduct.Product> products = player.getStock().getProducts(product);
        int availableQuantity = products.size();

        if (!(availableQuantity >= quantity)) {
            // Not enough stock, buy more and then consume
            waitForBuyOrder(buy(player, product, quantity - availableQuantity));
        }

        player.getStock().removeProducts(product, quantity);
    }
}

interface Sell extends Build {
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
        // Actions.Sell the requested quantity of products or all the available products, whichever is smaller
        int quantityToSell = Math.min(quantity, availableQuantity);
        if (quantityToSell > 0)
            Order.newSellOrder(player, product, quantityToSell);
    }
}

