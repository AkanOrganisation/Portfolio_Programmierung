import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * An abstract class representing a generic order to buy or sell a specific
 * product in a market.
 */
public abstract class Order {
    private final Player issuer;
    private final CountDownLatch completed;
    private final CatalogProduct product;
    private int quantity;
    private double priceUnit;

    /**
     * Creates a new order with the given parameters.
     *
     * @param issuer   the player who issued the order
     * @param product  the product being bought or sold
     * @param quantity the quantity of the product to be bought or sold
     */
    private Order(Player issuer, CatalogProduct product, int quantity, double limitPrice) {
        this.issuer = issuer;
        this.product = product;
        this.quantity = quantity;
        this.priceUnit = calculatePrice(limitPrice);
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
    public static Order newBuyOrder(Player issuer, CatalogProduct product, int quantityToBuy, double limitPrice) {
        return new BuyOrder(issuer, product, quantityToBuy, limitPrice);
    }

    /**
     * Creates a new sell order with the given parameters.
     *
     * @param issuer         the player who issued the order
     * @param product        the product being sold
     * @param quantityToSell the quantity of the product to be sold
     * @return a new sell order
     */
    public static Order newSellOrder(Player issuer, CatalogProduct product, int quantityToSell, double minSellPrice) {
        return new SellOrder(issuer, product, quantityToSell, minSellPrice);
    }

    /**
     * Calculates the unit price of the product based on some logic that is to be
     * implemented in the child classes.
     *
     * @return the unit price of the product
     */
    protected abstract double calculatePrice(double limitPrice);


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

    /**
     * Returns the price unit of the product.
     *
     * @return the price unit of the product
     */
    public double getPriceUnit() {
        return priceUnit;
    }

    public void waitUntilCompleted(int i, TimeUnit timeUnit) throws InterruptedException {
        completed.await(i, timeUnit);
    }

    /**
     * Returns the issuer of the product.
     *
     * @return the issuer of the product
     */
    public Player getIssuer() {
        return issuer;
    }

    /**
     * The SellOrder class represents a sell order, which is a type of market order.
     * It extends the Order class and provides an implementation of the execute()
     * method for selling a product and updating the players' stocks and money.
     */
    private static class SellOrder extends Order {

        private SellOrder(Player issuer, CatalogProduct item, int quantity, double minSellPrice) {
            super(issuer, item, quantity, minSellPrice);
            Market.getInstance().addSellOrder(this);
            getIssuer().getHistory().addRecord(Main.getRound(), item, 0, 0, quantity, 0);

        }

        @Override
        protected double calculatePrice(double minSellPrice) {
            return calculateSellPrice(minSellPrice);
        }

        private double calculateSellPrice(double minSellPrice) {
            int currentRound = Main.getRound();
            double playerSoldPreviousRound = getIssuer().getHistory().getSold(currentRound - 1, getProduct());
            double marketSoldPreviousRound = Market.getInstance().getHistory().getSold(currentRound - 1, getProduct());
            double playerRatio = playerSoldPreviousRound / marketSoldPreviousRound;

            int playerDesiredQuantityLastRound = getIssuer().getHistory().getDesiredSell(currentRound - 1, getProduct());

            double recommendedPrice = getProduct().getRecommendedPrice();

            // Adjust the price based on the player ratio
            if (Double.isNaN(playerRatio)) {
                return recommendedPrice;
            }
            double quantityFactor = playerDesiredQuantityLastRound > 0 ? playerSoldPreviousRound / playerDesiredQuantityLastRound : 0;

            double adjustedPrice = recommendedPrice * (1 + playerRatio/10 - quantityFactor);

            // Round the price to cents
            adjustedPrice = Math.floor(adjustedPrice * 100) / 100.0;
            return Math.max(adjustedPrice, minSellPrice);
        }


        /**
         * Sells the product to the partner and updates the players' stocks and money
         * accordingly.
         *
         * @param partner      the player who is buying the product
         * @param soldQuantity the quantity of the product being sold
         */
        @Override
        public void execute(Player partner, int soldQuantity) {
            double totalPrice = soldQuantity * getPriceUnit();

            /*
             * Reduce stock quantity and increase player's money
             */
            super.issuer.getStock().removeProducts(getProduct(), soldQuantity);
            super.issuer.addMoney(totalPrice);
            super.quantity -= soldQuantity;
            super.issuer.getHistory().addRecord(super.issuer.getRound(), super.product, 0, soldQuantity, 0, 0);
            Market.getInstance().getHistory().addRecord(super.issuer.getRound(), super.product, 0, soldQuantity, 0, 0);
            Log.getInstance().addMessage(super.issuer.getName() + " Sold " + soldQuantity + " units of " + super.product.getName() + " for " + totalPrice + " to " + partner.getType() + " " + partner.getName());

            /*
             * Check if the order is complete
             */
            if (super.quantity == 0) {
                finish();
            }
        }

    }

    /**
     * The BuyOrder class represents a buy order, which is an order placed by a
     * player to buy a certain quantity of a catalog product at a certain price from
     * another player. It extends the Order class and implements the execute()
     * method to execute the order when it matches with a sell order in the market.
     */
    private static class BuyOrder extends Order {
        /**
         * Constructs a BuyOrder instance.
         *
         * @param issuer   the player who placed the buy order
         * @param item     the catalog product to be bought
         * @param quantity the quantity of the product to be bought
         */

        private BuyOrder(Player issuer, CatalogProduct item, int quantity, double maxPrice) {
            super(issuer, item, quantity, maxPrice);
            super.priceUnit = calculatePrice(maxPrice);
            Market.getInstance().addBuyOrder(this);
            getIssuer().getHistory().addRecord(Main.getRound(), item, 0, 0, 0, quantity);

        }

        @Override
        protected double calculatePrice(double maxPrice) {
            return calculateBuyPrice(maxPrice);
        }

        private double calculateBuyPrice(double maxPrice) {
            int currentRound = Main.getRound();
            double playerBoughtPreviousRound = getIssuer().getHistory().getBought(currentRound - 1, getProduct());
            double marketBoughtPreviousRound = Market.getInstance().getHistory().getBought(currentRound - 1, getProduct());
            double playerRatio = playerBoughtPreviousRound / marketBoughtPreviousRound;

            int playerDesiredQuantityLastRound = getIssuer().getHistory().getDesiredBuy(currentRound - 1, getProduct());

            double recommendedPrice = getProduct().getRecommendedPrice();

            // Adjust the price based on the player ratio and quantity factor
            if (Double.isNaN(playerRatio)) {
                return recommendedPrice;
            }
            double quantityFactor = playerDesiredQuantityLastRound > 0 ? playerBoughtPreviousRound / playerDesiredQuantityLastRound : 0;

            double adjustedPrice = recommendedPrice * (1 + playerRatio/10 + quantityFactor);


            // Round the price to cents
            adjustedPrice = Math.floor(adjustedPrice * 100) / 100.0;
            return Math.min(adjustedPrice, maxPrice);
        }

        /**
         * Executes the buy order when it matches with a sell order in the market. It
         * calculates the total price and updates the stock and money of the players
         * involved.
         *
         * @param partner        the player who placed the matching sell order
         * @param boughtQuantity the quantity of the product bought in the matching sell
         *                       order
         */
        @Override
        public void execute(Player partner, int boughtQuantity) {
            double totalPrice = boughtQuantity * getPriceUnit();

            /*
             * Increase stock quantity and reduce player's money
             */
            super.issuer.getStock().addProducts(getProduct(), boughtQuantity);
            super.issuer.removeMoney(totalPrice);
            super.quantity -= boughtQuantity;

            super.issuer.getHistory().addRecord(super.issuer.getRound(), super.product, boughtQuantity, 0, 0, 0);
            Market.getInstance().getHistory().addRecord(super.issuer.getRound(), super.product, boughtQuantity, 0, 0, 0);
            Log.getInstance().addMessage(super.issuer.getName() + " bought " + boughtQuantity + " units of " + super.product.getName() + " for " + totalPrice + " from " + partner.getType() + " " + partner.getName());
            /*
             * check if the order is complete
             */
            if (super.quantity == 0) {
                finish();
            }
        }

    }


}
