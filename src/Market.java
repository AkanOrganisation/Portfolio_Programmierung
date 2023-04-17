import java.util.*;

public class Market implements Runnable {
    private static Market instance = null;
    private final Map<CatalogProduct, SortedSet<BuyOrder>> buyOrders;
    private final Map<CatalogProduct, SortedSet<SellOrder>> sellOrders;

    private Market() {
        buyOrders = new HashMap<>();
        sellOrders = new HashMap<>();
    }

    public static Market getInstance() {
        if (instance == null) {
            instance = new Market();
        }
        return instance;
    }

    public synchronized void addBuyOrder(BuyOrder order) {
        CatalogProduct product = order.getItem();
        if (!buyOrders.containsKey(product)) {
            Comparator<BuyOrder> buyOrderComparator = Comparator.comparingDouble(BuyOrder::getMaxPricePerUnit).reversed();
            buyOrders.put(product, new TreeSet<>(buyOrderComparator));
        }
        buyOrders.get(product).add(order);
        this.notify();
    }

    public synchronized void addSellOrder(SellOrder order) {
        CatalogProduct product = order.getItem();
        if (!sellOrders.containsKey(product)) {
            Comparator<SellOrder> sellOrderComparator = Comparator.comparingDouble(SellOrder::getMinPricePerUnit);
            sellOrders.put(product, new TreeSet<>(sellOrderComparator));
        }
        sellOrders.get(product).add(order);
        this.notify();
    }


    @Override
    public void run() {
        while (!Main.finished) {
            synchronized (this) {
                try {
                    // wait for a new order to be added
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Match the orders
            matchOrders();
        }
    }

    private void matchOrders() {
        for (CatalogProduct product : CatalogProduct.catalog) {
            SortedSet<BuyOrder> buySet = buyOrders.get(product);
            SortedSet<SellOrder> sellSet = sellOrders.get(product);
            if (buySet == null || sellSet == null) {
                // no buy or sell orders for this product
                return;
            }
            while (!buySet.isEmpty() && !sellSet.isEmpty() && buySet.first().getMaxPricePerUnit() >= sellSet.first().getMinPricePerUnit()) {
                // execute a trade
                BuyOrder buyOrder = buySet.first();
                SellOrder sellOrder = sellSet.first();
                int quantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                buyOrder.execute(quantity);
                sellOrder.execute(quantity);
                if (buyOrder.isComplete()) {
                    buySet.remove(buyOrder);
                }
                if (sellOrder.isComplete()) {
                    sellSet.remove(sellOrder);
                }
            }
        }
    }
}