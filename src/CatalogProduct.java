/**
 * 
 * The CatalogProduct class represents a product in a catalog. It contains
 * information about the product's name, recommended price, and components that
 * make up the product. It also provides static methods to retrieve products by
 * name or ID.
 */
public class CatalogProduct {
	/**
	 * Static attribute to store all created instances.
	 */
	public static ArrayList<CatalogProduct> catalog = new ArrayList<>();
	/**
	 * The ID of the product.
	 */
	public int id;
	/**
	 * The name of the product.
	 */
	public String name;
	/**
	 * The recommended price of the product.
	 */
	public double recommendedPrice;
	/**
	 * The list of components that make up the product.
	 */
	public ArrayList<Component> components;

	/**
	 * Constructor for CatalogProduct class.
	 * 
	 * @param id               The ID of the product.
	 * @param name             The name of the product.
	 * @param recommendedPrice The recommended price of the product.
	 * @param components       The list of components that make up the product.
	 */
	@JsonCreator
	public CatalogProduct(@JsonProperty("id") int id, @JsonProperty("name") String name,
			@JsonProperty("recommendedPrice") double recommendedPrice,
			@JsonProperty("components") ArrayList<Component> components) {
		this.id = id;
		this.name = name;
		this.recommendedPrice = recommendedPrice;
		this.components = components;
// Add the newly created instance to the catalog
		catalog.add(this);
	}

	/**
	 * Retrieves a product by name from the catalog.
	 * 
	 * @param productName The name of the product to retrieve.
	 * @return The CatalogProduct object representing the product, or null if not
	 *         found.
	 */
	public static CatalogProduct getProductByName(String productName) {
		return catalog.stream().filter(product -> product.name.equals(productName)).findFirst().orElse(null);
	}

	/**
	 * Retrieves a product by ID from the catalog.
	 * 
	 * @param id The ID of the product to retrieve.
	 * @return The CatalogProduct object representing the product, or null if not
	 *         found.
	 */
	public static CatalogProduct getProductById(int id) {
		return catalog.stream().filter(product -> product.id == id).findFirst().orElse(null);
	}

	/**
	 * Retrieves the name of the product.
	 * 
	 * @return The name of the product.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves the recommended price of the product.
	 * 
	 * @return The recommended price of the product.
	 */
	public double getRecommendedPrice() {
		return recommendedPrice;
	}

	/**
	 * Retrieves the list of components that make up the product.
	 * 
	 * @return The list of components that make up the product.
	 */
	public List<Component> getComponents() {
		return this.components;
	}
}