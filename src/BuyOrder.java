public class BuyOrder extends Order {
    public double maxPricePerUnit;

    public BuyOrder(Player issuer, CatalogProduct item, int quantity) {
        super(issuer, item, quantity);
        maxPricePerUnit = priceUnit;
        Market.getInstance().addBuyOrder(this);
    }

    @Override
    public void execute(int boughtQuantity) {
        double totalPrice = boughtQuantity * maxPricePerUnit;

        // Increase stock quantity and reduce player's money
        issuer.stock.addProducts(getItem(), boughtQuantity);
        issuer.money -= totalPrice;
        this.quantity -= boughtQuantity;
        System.out.println("Bought " + boughtQuantity + " units of " + getItem().name + " for " + totalPrice + " from " + issuer.type + " " + issuer.id);
        // Check if the order is complete
        if (this.quantity == 0) {
            finish();
        }
    }

    public double getMaxPricePerUnit(){
        return this.maxPricePerUnit;
    }
}