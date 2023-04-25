
/**

An abstract class representing a generic order to buy or sell a specific product in a market.
*/
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class Order {
	private final Player issuer; // The player who issued the order
	private final CountDownLatch completed; // A latch to indicate whether the order is completed or not
	private final CatalogProduct product; // The product being bought or sold
	private int quantity; // The quantity of the product to be bought or sold
	private double priceUnit; // The unit price of the product

	/**
	 * Creates a new order with the given parameters.
	 *
	 * @param issuer   the player who issued the order
	 * @param product  the product being bought or sold
	 * @param quantity the quantity of the product to be bought or sold
	 */
	public Order(Player issuer, CatalogProduct product, int quantity) {
		this.issuer = issuer;
		this.product = product;
		this.quantity = quantity;
		this.completed = new CountDownLatch(1);
	}

	/**
	 * Creates a new buy order with the given parameters.
	 *
	 * @param issuer        the player who issued the order
	 * @param product       the product being bought
	 * @param quantityToBuy the quantity of the product to be bought
	 * @return a new buy order
	 */
	public static Order newBuyOrder(Player issuer, CatalogProduct product, int quantityToBuy) {
		return new BuyOrder(issuer, product, quantityToBuy);
	}

	/**
	 * Creates a new sell order with the given parameters.
	 *
	 * @param issuer         the player who issued the order
	 * @param product        the product being sold
	 * @param quantityToSell the quantity of the product to be sold
	 * @return a new sell order
	 */
	public static Order newSellOrder(Player issuer, CatalogProduct product, int quantityToSell) {
		return new SellOrder(issuer, product, quantityToSell);
	}

	/**
	 * Calculates the unit price of the product based on some logic that is to be
	 * implemented in the child classes.
	 *
	 * @return the unit price of the product
	 */
	private double calculatePrice() {
		// TODO: 17.04.2023 calculate price based on some logic in child classes. make
		// this abstract
		return product.getRecommendedPrice();
		// Implementation to be defined
		// Abstract method to be implemented in subclasses
	}

	/**
	 * Executes the order with the given partner and quantity.
	 *
	 * @param partner  the partner who executes the order
	 * @param quantity the quantity of the product to be bought or sold
	 */
	public abstract void execute(Player partner, int quantity);

	/**
	 * Decrements the count of the completed latch.
	 */
	void finish() {
		this.completed.countDown();
	}

	/**
	 * Returns the product being bought or sold.
	 *
	 * @return the product being bought or sold
	 */
	public CatalogProduct getProduct() {
		return product;
	}

	/**
	 * Returns true if the order is completed, false otherwise.
	 *
	 * @return true if the order is completed, false otherwise
	 */
	public boolean isComplete() {
		return this.quantity == 0;
	}

	/**
	 * Returns the quantity of the product to be bought or sold.
	 *
	 * @return the quantity of the product to be bought or sold
	 */
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
			Log.getInstance().addMessage(
					super.issuer.getName() + " Sold " + soldQuantity + " units of " + super.product.getName() + " for "
							+ totalPrice + " to " + partner.getType() + " " + partner.getName());

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
			Log.getInstance()
					.addMessage(super.issuer.getName() + " bought " + boughtQuantity + " units of "
							+ super.product.getName() + " for " + totalPrice + " from " + partner.getType() + " "
							+ partner.getName());
			// Check if the order is complete
			if (super.quantity == 0) {
				finish();
			}
		}

	}
}
