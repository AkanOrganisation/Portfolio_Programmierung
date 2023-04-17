import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stock {
    private Map<CatalogProduct, List<Product>> stock = new HashMap<>();

    public void removeProducts(CatalogProduct catalogProduct, int quantity) {
        if (stock.containsKey(catalogProduct)) {
            List<Product> products = stock.get(catalogProduct);
            if (quantity <= products.size()) {
                for (int i = 0; i < quantity; i++) {
                    products.remove(0);
                }
            }
        }
    }

    public void addProducts(CatalogProduct catalogProduct, int quantity) {
        List<Product> products = stock.computeIfAbsent(catalogProduct, k -> new ArrayList<>());
        for (int i = 0; i < quantity; i++) {
            Product product = new Product();
            products.add(product);
        }
    }

    public List<Product> getProducts(CatalogProduct catalogProduct) {
        return stock.getOrDefault(catalogProduct, new ArrayList<>());
    }

    public Map<CatalogProduct, List<Product>> getStock() {
        return stock;
    }

    public Map<CatalogProduct, Integer> getProductQuantities() {
        Map<CatalogProduct, Integer> productQuantities = new HashMap<>();
        for (CatalogProduct product : stock.keySet()) {
            int quantity = stock.get(product).size();
            productQuantities.merge(product, quantity, Integer::sum);
        }
        return productQuantities;
    }
}
