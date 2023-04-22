import java.util.*;

public class Market implements Runnable {
    private static Market instance = null;
    private final Map<CatalogProduct, SortedSet<BuyOrder>> buyOrders;
    private final Map<CatalogProduct, SortedSet<SellOrder>> sellOrders;
    private boolean newOrders;

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
        setNewOrders(true);
        this.notify();
    }

    public synchronized void addSellOrder(SellOrder order) {
        CatalogProduct product = order.getItem();
        if (!sellOrders.containsKey(product)) {
            Comparator<SellOrder> sellOrderComparator = Comparator.comparingDouble(SellOrder::getMinPricePerUnit);
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
                buyOrder.execute(sellOrder.issuer, quantity);
                sellOrder.execute(buyOrder.issuer, quantity);
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