import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class History {

    private static class ProductRecord {
        int desiredSell;
        int sold;
        int desiredBuy;
        int bought;

        int desiredConsumption;
        int consumed;

        private ProductRecord(int bought, int sold, int desiredSell, int desiredBuy, int desiredConsumption, int consumed) {
            this.bought = bought;
            this.sold = sold;
            this.consumed = consumed;
            this.desiredSell = desiredSell;
            this.desiredBuy = desiredBuy;
            this.desiredConsumption = desiredConsumption;
        }
        private ProductRecord(int bought, int sold, int desiredSell, int desiredBuy) {
            new ProductRecord(bought, sold, desiredSell, desiredBuy, 0, 0);
        }
    }

    private final Map<Integer, Map<CatalogProduct, ProductRecord>> history;

    public History() {
        history = new ConcurrentHashMap<>();
    }

    public void addBuySellRecord(int round, CatalogProduct product, int bought, int sold, int desiredSell, int desiredBuy) {
        Map<CatalogProduct, ProductRecord> roundMap = history.computeIfAbsent(round, k -> new ConcurrentHashMap<>());
        ProductRecord record = roundMap.computeIfAbsent(product, k -> new ProductRecord(0, 0, 0, 0));
        record.desiredBuy += desiredBuy;
        record.bought += bought;
        record.desiredSell += desiredSell;
        record.sold += sold;
    }


    public ProductRecord getRecord(int round, CatalogProduct product) {
        return history.getOrDefault(round, new ConcurrentHashMap<>()).getOrDefault(product, new ProductRecord(0, 0, 0, 0));
    }

    public int getBought(int round, CatalogProduct product) {
        return getRecord(round, product).bought;
    }

    public int getSold(int round, CatalogProduct product) {
        return getRecord(round, product).sold;
    }

    public int getDesiredSell(int round, CatalogProduct product) {
        return getRecord(round, product).desiredSell;
    }

    public int getDesiredBuy(int round, CatalogProduct product) {
        return getRecord(round, product).desiredBuy;
    }
}

