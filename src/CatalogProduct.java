import java.util.ArrayList;
import java.util.List;

public class CatalogProduct {
    // Static attribute to store all created instances
    public static ArrayList<CatalogProduct> catalog = new ArrayList<CatalogProduct>();
    public int id;

    // Instance attributes
    public String name;
    public double recommendedPrice;
    public ArrayList<Component> components;

    /**
     * Constructor for CatalogProduct class.
     *
     * @param name the name of the product
     * @param recommendedPrice the recommended price of the product
     * @param components the list of components that make up the product
     */
    public CatalogProduct(int id, String name, double recommendedPrice, ArrayList<Component> components) {
        this.id = id;
        this.name = name;
        this.recommendedPrice = recommendedPrice;
        this.components = components;

        // Add the newly created instance to the catalog
        catalog.add(this);
    }

    public String getName() {
        return name;
    }

    public double getRecommendedPrice() {
        return recommendedPrice;
    }

    public List<Component> getComponents() {
        return this.components;
    }
}
