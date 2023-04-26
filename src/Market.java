import java.util.*;

/**
 * The Market class is responsible for managing buy and sell orders for various
 * products. It is implemented as a singleton and uses a HashMap to store the
 * buy and sell orders. The Market class is also Runnable and runs in a separate
 * thread. The class has methods to add buy and sell orders and a private method
 * to set the new order flag. There is also a method to check if there are any
 * new orders and a matchOrders method to match the buy and sell orders. The run
 * method waits for the game to start and then waits for new orders to be added
 * to the market. Once new orders are added, the matchOrders method is called to
 * match the buy and sell orders. If there are no new orders, the market is set
 * as finished and the thread exits.
 */
public class Market implements Runnable {
    /**
     * The buyOrders map stores the catalog products and their corresponding sorted
     * set of buy orders.
     */
    private final Map<CatalogProduct, SortedSet<Order>> buyOrders;
    /**
     * The sellOrders map stores the catalog products and their corresponding sorted
     * set of sell orders.
     */
    private final Map<CatalogProduct, SortedSet<Order>> sellOrders;
    /**
     * The newOrders boolean represents whether new orders have been added to the
     * market.
     */
    private boolean newOrders;

    /**
     * The constructor initializes the buyOrders and sellOrders maps as empty
     * HashMaps.
     */
    private Market() {
        buyOrders = new HashMap<>();
        sellOrders = new HashMap<>();
    }

    /**
     * The InstanceHolder class ensures that only one instance of the Market class
     * is created.
     */
    private static final class InstanceHolder {
        private static final Market instance = new Market();
    }

    /**
     * The getInstance method returns the single instance of the Market class.
     *
     * @return the single instance of the Market class
     */
    public static Market getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * The addBuyOrder method adds a buy order to the market. If the buyOrders map
     * does not contain the product, a new sorted set is created and added to the
     * map. The newOrders boolean is set to true and the thread is notified.
     *
     * @param order the buy order to be added
     */
    public synchronized void addBuyOrder(Order order) {
        CatalogProduct product = order.getProduct();
        if (!buyOrders.containsKey(product)) {
            Comparator<Order> buyOrderComparator = Comparator.comparingDouble(Order::getPriceUnit).reversed();
            buyOrders.put(product, new TreeSet<>(buyOrderComparator));
        }
        buyOrders.get(product).add(order);
        setNewOrders(true);
        this.notify();
    }

    /**
     * The addSellOrder method adds a sell order to the market. If the sellOrders
     * map does not contain the product, a new sorted set is created and added to
     * the map. The newOrders boolean is set to true and the thread is notified.
     *
     * @param order the sell order to be added
     */
    public synchronized void addSellOrder(Order order) {
        CatalogProduct product = order.getProduct();
        if (!sellOrders.containsKey(product)) {
            Comparator<Order> sellOrderComparator = Comparator.comparingDouble(Order::getPriceUnit);
            sellOrders.put(product, new TreeSet<>(sellOrderComparator));
        }
        sellOrders.get(product).add(order);
        setNewOrders(true);
        this.notify();
    }

    /**
     * The setNewOrders method sets the newOrders boolean to the given value.
     *
     * @param b the new value for newOrders
     */
    private synchronized void setNewOrders(boolean b) {
        newOrders = b;
    }

    /**
     * The gotNewOrders method returns the current value of newOrders.
     *
     * @return the current value of newOrders
     */
    private boolean gotNewOrders() {
        return newOrders;
    }

    /**
     * The run method runs the Market object on a separate thread. It waits for the
     * game to start and then waits for new orders to be added to the market. If new
     * orders are added, it matches the orders. If no new orders are added, it sets
     * the market as finished. If the thread is interrupted, it logs the crash with
     * a message.
     */
    @Override
    public void run() {
        // Wait until the game starts
        try {
            Synchronizer.waitGameStart();
        } catch (InterruptedException e) {
            Log.getInstance().addMessage("Market didn't open");
            throw new RuntimeException(e);
        }

        // Play the game
        while (!(Synchronizer.gameFinished())) {
            synchronized (this) {
                try {
                    // wait for a new order to be added
                    // Log.getInstance().addMessage("waiting for orders");
                    this.wait(100);
                    if (gotNewOrders()) {
                        setNewOrders(false);
                        // Match the orders
                        // Log.getInstance().addMessage("got new orders to match");
                        matchOrders();
                    } else {
                        Synchronizer.setMarketFinished();
                    }
                } catch (InterruptedException e) {
                    if (!Synchronizer.gameFinished()) {
                        Log.getInstance().addMessage("Market crashed");
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    /**
     * Matches the buy and sell orders for each product in the market. The method
     * iterates over all the products in the catalog, retrieves their corresponding
     * buy and sell orders, and matches them based on the price unit. If there are
     * no buy or sell orders for a product, the method returns without matching
     * orders for that product. If there is a match between a buy and sell order for
     * a product, a trade is executed with the minimum quantity between the buy and
     * sell orders. The buy and sell orders are then updated and removed from their
     * respective sets if they are complete.
     */
    private void matchOrders() {
        for (CatalogProduct product : CatalogProduct.getCatalog()) {
            SortedSet<Order> buySet = buyOrders.get(product);
            SortedSet<Order> sellSet = sellOrders.get(product);
            if (buySet == null || sellSet == null) {
                // no buy or sell orders for this product
                return;
            }
            while (!buySet.isEmpty() && !sellSet.isEmpty()
                    && buySet.first().getPriceUnit() >= sellSet.first().getPriceUnit()) {
                // execute a trade
                Order buyOrder = buySet.first();
                Order sellOrder = sellSet.first();
                int quantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                buyOrder.execute(sellOrder.getIssuer(), quantity);
                sellOrder.execute(buyOrder.getIssuer(), quantity);
                if (buyOrder.isComplete()) {
                    buySet.remove(buyOrder);
                }
                if (sellOrder.isComplete()) {
                    sellSet.remove(sellOrder);
                }
            }
        }
    }

    /**
     * The clearOrders method clears all buy and sell orders of the run.
     */
    public void clearOrders() {
        this.buyOrders.clear();
        this.sellOrders.clear();
    }
}