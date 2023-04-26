import java.util.*;

public class Market implements Runnable {
    private final Map<CatalogProduct, SortedSet<Order>> buyOrders;
    private final Map<CatalogProduct, SortedSet<Order>> sellOrders;
    private boolean newOrders;

    private Market() {
        buyOrders = new HashMap<>();
        sellOrders = new HashMap<>();
    }

    private static final class InstanceHolder {
        private static final Market instance = new Market();
    }

    public static Market getInstance() {
        return InstanceHolder.instance;
    }


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

    private synchronized void setNewOrders(boolean b) {
        newOrders = b;
    }


    private boolean gotNewOrders() {
        return newOrders;
    }

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
                    //Log.getInstance().addMessage("waiting for orders");
                    this.wait(100);
                    if (gotNewOrders()) {
                        setNewOrders(false);
                        // Match the orders
                        //Log.getInstance().addMessage("got new orders to match");
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


    private void matchOrders() {
        for (CatalogProduct product : CatalogProduct.getCatalog()) {
            SortedSet<Order> buySet = buyOrders.get(product);
            SortedSet<Order> sellSet = sellOrders.get(product);
            if (buySet == null || sellSet == null) {
                // no buy or sell orders for this product
                return;
            }
            while (!buySet.isEmpty() && !sellSet.isEmpty() && buySet.first().getPriceUnit() >= sellSet.first().getPriceUnit()) {
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

    public void clearOrders() {
        this.buyOrders.clear();
        this.sellOrders.clear();
    }
}