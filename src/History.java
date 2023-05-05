import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The History class maintains a record of buying and selling activities, and the desired and actual consumption
 * of products in each round of a simulation.
 */
public class History {
    /**
     * ProductRecord class represents the buying and selling activities, and the desired and actual consumption
     * of a single product in a round of the simulation.
     */
    private static class ProductRecord {
        int desiredSell;
        int sold;
        int desiredBuy;
        int bought;
        int desiredConsumption;
        int consumed;

        /**
         * Constructs a new ProductRecord object with the given values.
         *
         * @param bought             the number of units bought
         * @param sold               the number of units sold
         * @param desiredSell        the desired number of units to sell
         * @param desiredBuy         the desired number of units to buy
         * @param desiredConsumption the desired number of units to consume
         * @param consumed           the number of units consumed
         */
        private ProductRecord(int bought, int sold, int desiredSell, int desiredBuy, int desiredConsumption, int consumed) {
            this.bought = bought;
            this.sold = sold;
            this.consumed = consumed;
            this.desiredSell = desiredSell;
            this.desiredBuy = desiredBuy;
            this.desiredConsumption = desiredConsumption;
        }

        /**
         * Constructs a new ProductRecord object with the given values, setting the desired consumption and consumed
         * fields to zero.
         *
         * @param bought      the number of units bought
         * @param sold        the number of units sold
         * @param desiredSell the desired number of units to sell
         * @param desiredBuy  the desired number of units to buy
         */
        private ProductRecord(int bought, int sold, int desiredSell, int desiredBuy) {
            new ProductRecord(bought, sold, desiredSell, desiredBuy, 0, 0);
        }
    }

    /**
     * The history Map stores the ProductRecord objects for each product in each round of the simulation.
     */
    private final Map<Integer, Map<CatalogProduct, ProductRecord>> history;

    /**
     * Constructs a new History object with an empty ConcurrentHashMap.
     */
    public History() {
        history = new ConcurrentHashMap<>();
    }

    /**
     * Adds a new ProductRecord object to the history Map for the given round and CatalogProduct.
     *
     * @param round       the round of the simulation
     * @param product     the CatalogProduct being bought and sold
     * @param bought      the number of units bought
     * @param sold        the number of units sold
     * @param desiredSell the desired number of units to sell
     * @param desiredBuy  the desired number of units to buy
     */
    public void addBuySellRecord(int round, CatalogProduct product, int bought, int sold, int desiredSell, int desiredBuy) {
        Map<CatalogProduct, ProductRecord> roundMap = history.computeIfAbsent(round, k -> new ConcurrentHashMap<>());
        ProductRecord record = roundMap.computeIfAbsent(product, k -> new ProductRecord(0, 0, 0, 0));
        record.desiredBuy += desiredBuy;
        record.bought += bought;
        record.desiredSell += desiredSell;
        record.sold += sold;
    }

    /**
     * Returns the ProductRecord for a given round and product.
     * If no record is found for the given round and product, a new ProductRecord is created with default values (0 for all fields).
     *
     * @param round   the round number for which the record is requested
     * @param product the CatalogProduct for which the record is requested
     * @return the ProductRecord for the given round and product
     */
    public ProductRecord getRecord(int round, CatalogProduct product) {
        return history.getOrDefault(round, new ConcurrentHashMap<>()).getOrDefault(product, new ProductRecord(0, 0, 0, 0));
    }

    /**
     * Returns the total quantity of the given product bought in the given round.
     *
     * @param round   the round number for which the quantity is requested
     * @param product the CatalogProduct for which the quantity is requested
     * @return the total quantity of the given product bought in the given round
     */
    public int getBought(int round, CatalogProduct product) {
        return getRecord(round, product).bought;
    }

    /**
     * Returns the total quantity of the given product sold in the given round.
     *
     * @param round   the round number for which the quantity is requested
     * @param product the CatalogProduct for which the quantity is requested
     * @return the total quantity of the given product sold in the given round
     */
    public int getSold(int round, CatalogProduct product) {
        return getRecord(round, product).sold;
    }

    /**
     * Returns the desired quantity of the given product to be sold in the given round.
     *
     * @param round   the round number for which the desired quantity is requested
     * @param product the CatalogProduct for which the desired quantity is requested
     * @return the desired quantity of the given product to be sold in the given round
     */
    public int getDesiredSell(int round, CatalogProduct product) {
        return getRecord(round, product).desiredSell;
    }

    /**
     * Returns the desired quantity of the given product to be bought in the given round.
     *
     * @param round   the round number for which the desired quantity is requested
     * @param product the CatalogProduct for which the desired quantity is requested
     * @return the desired quantity of the given product to be bought in the given round
     */
    public int getDesiredBuy(int round, CatalogProduct product) {
        return getRecord(round, product).desiredBuy;
    }
}

