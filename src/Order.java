import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class Order {
    private final Player issuer;
    private final CountDownLatch completed;
    private final CatalogProduct product;
    private int quantity;
    private double priceUnit;

    public Order(Player issuer, CatalogProduct product, int quantity) {
        this.issuer = issuer;
        this.product = product;
        this.quantity = quantity;
        this.completed = new CountDownLatch(1);
    }

    public static Order newBuyOrder(Player issuer, CatalogProduct product, int quantityToBuy) {
        return new BuyOrder(issuer, product, quantityToBuy);
    }

    public static Order newSellOrder(Player issuer, CatalogProduct product, int quantityToSell) {
        return new SellOrder(issuer, product, quantityToSell);
    }

    private double calculatePrice() {
        // TODO: 17.04.2023 calculate price based on some logic in child classes. make this abstract
        return product.getRecommendedPrice();
        // Implementation to be defined
        // Abstract method to be implemented in subclasses
    }

    public abstract void execute(Player partner, int quantity);

    void finish() {
        this.completed.countDown();
    }

    public CatalogProduct getProduct() {
        return product;
    }


    public boolean isComplete() {
        return this.quantity == 0;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public double getPriceUnit() {
        return priceUnit;
    }

    public void waitUntilCompleted(int i, TimeUnit timeUnit) throws InterruptedException {
        completed.await(i, timeUnit);
    }

    public Player getIssuer() {
        return issuer;
    }


    private static class SellOrder extends Order {

        private SellOrder(Player issuer, CatalogProduct item, int quantity) {
            super(issuer, item, quantity);
            super.priceUnit = super.calculatePrice();
            Market.getInstance().addSellOrder(this);
        }

        @Override
        public void execute(Player partner, int soldQuantity) {
            double totalPrice = soldQuantity * getPriceUnit();

            // Reduce stock quantity and increase player's money
            super.issuer.getStock().removeProducts(getProduct(), soldQuantity);
            super.issuer.addMoney(totalPrice);
            super.quantity -= soldQuantity;
            Log.getInstance().addMessage(super.issuer.getName() + " Sold " + soldQuantity + " units of " + super.product.getName() + " for " + totalPrice + " to " + partner.getType() + " " + partner.getName());

            // Check if the order is complete
            if (super.quantity == 0) {
                finish();
            }
        }

    }

    private static class BuyOrder extends Order {

        private BuyOrder(Player issuer, CatalogProduct item, int quantity) {
            super(issuer, item, quantity);
            super.priceUnit = super.calculatePrice();
            Market.getInstance().addBuyOrder(this);
        }

        @Override
        public void execute(Player partner, int boughtQuantity) {
            double totalPrice = boughtQuantity * getPriceUnit();

            // Increase stock quantity and reduce player's money
            super.issuer.getStock().addProducts(getProduct(), boughtQuantity);
            super.issuer.removeMoney(totalPrice);
            super.quantity -= boughtQuantity;
            Log.getInstance().addMessage(super.issuer.getName() + " bought " + boughtQuantity + " units of " + super.product.getName() + " for " + totalPrice + " from " + partner.getType() + " " + partner.getName());
            // Check if the order is complete
            if (super.quantity == 0) {
                finish();
            }
        }

    }
}
