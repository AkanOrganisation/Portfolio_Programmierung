public abstract class Order {
    public Player issuer;
    private CatalogProduct item;
    public int quantity;
    public double priceUnit;

    public Order(Player issuer, CatalogProduct item, int quantity) {
        this.issuer = issuer;
        this.item = item;
        this.quantity = quantity;
        this.priceUnit = calculatePrice();
    }

    private double calculatePrice() {
        // TODO: 17.04.2023 calculate price based on some logic
        return item.recommendedPrice;
        // Implementation to be defined
    }

    // Abstract method to be implemented in subclasses
    public abstract void execute(int quantity);

    void finish(){
        // TODO: 17.04.2023  may be absolute
        // Implementation to be defined
    }

    public CatalogProduct getItem() {
        return item;
    }

    public boolean isComplete() {
        return this.quantity == 0;
    }

    public int getQuantity() {
        return this.quantity;
    }
}
