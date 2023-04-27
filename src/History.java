import java.util.HashMap;
import java.util.Map;

public class History {
    private class ProductRecord {
        int bought;
        int sold;

        private ProductRecord(int bought, int sold) {
            this.bought = bought;
            this.sold = sold;
        }
    }

    private final Map<Integer, Map<CatalogProduct, ProductRecord>> history;

    public History() {
        history = new HashMap<>();
    }

    public void addRecord(int round, CatalogProduct product, int bought, int sold) {
        Map<CatalogProduct, ProductRecord> roundMap = history.computeIfAbsent(round, k -> new HashMap<>());
        ProductRecord record = roundMap.computeIfAbsent(product, k -> new ProductRecord(0, 0));
        record.bought += bought;
        record.sold += sold;
    }


    public ProductRecord getRecord(int round, CatalogProduct product) {
        return history.getOrDefault(round, new HashMap<>()).getOrDefault(product, new ProductRecord(0, 0));
    }

    public int getBought(int round, CatalogProduct product) {
        return getRecord(round, product).bought;
    }

    public int getSold(int round, CatalogProduct product) {
        return getRecord(round, product).sold;
    }
}

