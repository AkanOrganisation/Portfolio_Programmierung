import java.util.concurrent.CountDownLatch;

public abstract class Order {
    public Player issuer;
    public CountDownLatch completed;
    private final CatalogProduct item;
    public int quantity;
    public double priceUnit;

    public Order(Player issuer, CatalogProduct item, int quantity) {
        this.issuer = issuer;
        this.item = item;
        this.quantity = quantity;
        this.priceUnit = calculatePrice();
        this.completed = new CountDownLatch(1);
    }

    private double calculatePrice() {
        // TODO: 17.04.2023 calculate price based on some logic
        return item.recommendedPrice;
        // Implementation to be defined
    }

    // Abstract method to be implemented in subclasses
    public abstract void execute(Player partner ,int quantity);

    void finish(){
        this.completed.countDown();
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
