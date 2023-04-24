package Actions;

import Catalog.CatalogProduct;
import Catalog.Component;
import Order.BuyOrder;
import Player.Player;
import Player.PlayerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Build extends Buy {
    default void build(Player player, CatalogProduct product, int quantity) throws InterruptedException {
        if (player.type == PlayerType.SUPPLIER) {
            player.stock.addProducts(product, quantity);
            return;
        }
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
            int availableQuantity = player.stock.getProductQuantities().getOrDefault(material, 0);
            maxQuantity = Math.min(maxQuantity, availableQuantity / requiredQuantity);
        }
        if (maxQuantity == Integer.MAX_VALUE) {
            return;
        }
        // Remove the required materials from the player's stock and add the built product
        for (Map.Entry<CatalogProduct, Integer> entry : requiredMaterials.entrySet()) {
            CatalogProduct material = entry.getKey();
            int requiredQuantity = entry.getValue();
            player.stock.removeProducts(material, requiredQuantity * maxQuantity);
        }
        player.stock.addProducts(product, maxQuantity);
    }
}
