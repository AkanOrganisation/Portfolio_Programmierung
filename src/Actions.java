import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface Buy{
    default BuyOrder buy(Player player, CatalogProduct product, int quantity) throws InterruptedException {
        BuyOrder order = new BuyOrder(player, product, quantity);
        //waitBuyOrder(order);
        return order;
    }

    default void waitForBuyOrder(BuyOrder order) throws InterruptedException {
        synchronized (order) {
            while (!order.isComplete()) {
                order.wait();
            }
        }
        if (Thread.currentThread().isInterrupted()) {
            // If the thread was interrupted, propagate the interrupt.
            throw new InterruptedException();
        }
    }
}

interface Consume extends Buy {
    default void consume(Player player, CatalogProduct product, int quantity) throws InterruptedException {
        List<Product> products = player.stock.getProducts(product);
        int availableQuantity = products.size();

        if (!(availableQuantity >= quantity)) {
            // Not enough stock, buy more and then consume
            waitForBuyOrder(buy(player, product, quantity - availableQuantity));
        }
        for (int i = 0; i < quantity; i++) {
            products.remove(0);
        }
    }
}

interface Build extends Buy {
    default void build(Player player, CatalogProduct product, int quantity) throws InterruptedException {
        List<Component> components = product.getComponents();
        Map<CatalogProduct, Integer> requiredMaterials = new HashMap<>();

        // Count the required quantity of each component
        for (Component component : components) {
            CatalogProduct material = component.getProduct();
            int requiredQuantity = component.getQuantity() * quantity;
            requiredMaterials.merge(material, requiredQuantity, Integer::sum);
        }

        // Check if the player has enough materials to build the product
        Map<CatalogProduct, Integer> availableMaterials = player.stock.getProductQuantities();
        boolean hasEnoughMaterials = true;
        ArrayList<BuyOrder> buyOrders = new ArrayList<>();
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
            while(buyOrders.size() > 0) {
                waitForBuyOrder(buyOrders.get(0));
                buyOrders.remove(0);
            }
        }

        // Calculate how many products can be built
        int maxQuantity = Integer.MAX_VALUE;
        for (Map.Entry<CatalogProduct, Integer> entry : requiredMaterials.entrySet()) {
            CatalogProduct material = entry.getKey();
            int requiredQuantity = entry.getValue();
            int availableQuantity = player.stock.getProductQuantities().getOrDefault(material, 0);
            maxQuantity = Math.min(maxQuantity, availableQuantity / requiredQuantity);
        }
        if(maxQuantity==Integer.MAX_VALUE){return;}
        // Remove the required materials from the player's stock and add the built product
        for (Map.Entry<CatalogProduct, Integer> entry : requiredMaterials.entrySet()) {
            CatalogProduct material = entry.getKey();
            int requiredQuantity = entry.getValue();
            player.stock.removeProducts(material, requiredQuantity * maxQuantity);
        }
        player.stock.addProducts(product, maxQuantity);
    }
}

interface Sell extends Build {
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
        // Sell the requested quantity of products or all the available products, whichever is smaller
        int quantityToSell = Math.min(quantity, availableQuantity);
        if (quantityToSell == 0)return;
        new SellOrder(player, product, quantityToSell);
    }
}
