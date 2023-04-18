public class Component {
    int id;
    CatalogProduct product;
    int quantity;

    Component (CatalogProduct product, int quantity){
        this.id = product.id;
        this.product = product;
        this.quantity = quantity;
    }

    public CatalogProduct getProduct() {
        return this.product;
    }

    public int getQuantity() {
        return this.quantity;
    }


}
