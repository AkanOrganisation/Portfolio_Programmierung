import java.util.HashMap;
import java.util.Map;

public class History {

    private class ProductRecord {
        int desiredSell;
        int sold;
        int desiredBuy;
        int bought;

        private ProductRecord(int bought, int sold, int desiredSell, int desiredBuy) {
            this.bought = bought;
            this.sold = sold;
            this.desiredSell = desiredSell;
            this.desiredBuy = desiredBuy;
        }
    }

    private final Map<Integer, Map<CatalogProduct, ProductRecord>> history;

    public History() {
        history = new HashMap<>();
    }

    public void addRecord(int round, CatalogProduct product, int bought, int sold, int desiredSell, int desiredBuy) {
        Map<CatalogProduct, ProductRecord> roundMap = history.computeIfAbsent(round, k -> new HashMap<>());
        ProductRecord record = roundMap.computeIfAbsent(product, k -> new ProductRecord(0, 0, 0, 0));
        record.desiredBuy += desiredBuy;
        record.bought += bought;
        record.desiredSell += desiredSell;
        record.sold += sold;
    }


    public ProductRecord getRecord(int round, CatalogProduct product) {
        return history.getOrDefault(round, new HashMap<>()).getOrDefault(product, new ProductRecord(0, 0, 0, 0));
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

