import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class CatalogProductLoader {

    public static ArrayList<CatalogProduct> loadCatalog(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Read JSON data from file
            CatalogProduct[] catalogProducts = mapper.readValue(new File(filePath), CatalogProduct[].class);

            // Add the catalog products to the CatalogProduct catalog
            for (CatalogProduct catalogProduct : catalogProducts) {
                CatalogProduct.catalog.add(catalogProduct);
            }

            /// Create components for the products
            for (CatalogProduct catalogProduct : CatalogProduct.catalog) {
                for (Component component : catalogProduct.components) {
                    CatalogProduct componentProduct = CatalogProduct.catalog.stream()
                            .filter(p -> p.id == component.id)
                            .findFirst()
                            .orElse(null);
                    if (componentProduct != null) {
                        Component componentToAdd = new Component(componentProduct, component.quantity);
                        catalogProduct.components.add(componentToAdd);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return CatalogProduct.catalog;
    }
}

class ComponentData {
    public int id;
    public int quantity;
}