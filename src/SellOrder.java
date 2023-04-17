public class SellOrder extends Order {
    public double minPricePerUnit;

    public SellOrder(Player issuer, CatalogProduct item, int quantity) {
        super(issuer, item, quantity);
        minPricePerUnit = priceUnit;
        Market.getInstance().addSellOrder(this);
    }

    @Override
    public void execute(int soldQuantity) {
        double totalPrice = soldQuantity * minPricePerUnit;

        // Reduce stock quantity and increase player's money
        issuer.stock.removeProducts(getItem(), soldQuantity);
        issuer.money += totalPrice;
        this.quantity -= soldQuantity;
        System.out.println("Sold " + soldQuantity + " units of " + getItem().name + " for " + totalPrice + " to " + issuer.type + " " + issuer.id);

        // Check if the order is complete
        if (this.quantity == 0) {
            finish();
        }
    }

    public double getMinPricePerUnit(){
        return this.minPricePerUnit;
    }
}
