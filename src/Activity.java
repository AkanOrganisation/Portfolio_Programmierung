
/**

This class represents an activity that a player can execute in the game.
An activity can be of several types, and can involve buying, selling, building, or consuming a product
from the game's catalog.
An activity has a minimum and maximum quantity, which are used to determine the amount of product involved in the activity.
The class implements the Buy, Sell, Build, and Consume interfaces, which define the methods for performing each type of activity.
The class also includes a static nested class called Data, which is used for deserializing Activity objects from JSON.
*/

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Random;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Random;

public class Activity implements Buy, Sell, Build, Consume {
	private final Player player;
	private final ActivityType type;
	private final CatalogProduct product;
	private final int minQuantity;
	private final int maxQuantity;
	private boolean finished;

	/**
	 * Constructor for creating a new Activity object.
	 *
	 * @param player      the player who is performing the activity
	 * @param type        the type of activity to perform (buy, sell, build, or
	 *                    consume)
	 * @param product     the product involved in the activity
	 * @param minQuantity the minimum quantity of product involved in the activity
	 * @param maxQuantity the maximum quantity of product involved in the activity
	 */

	public Activity(Player player, ActivityType type, CatalogProduct product, int minQuantity, int maxQuantity) {
		this.player = player;
		this.type = type;
		this.product = product;
		this.minQuantity = minQuantity;
		this.maxQuantity = maxQuantity;
		this.finished = false;

	}

}

	/**
	 * Executes the activity by calling the appropriate method from the
	 * corresponding interface (Buy, Sell, Build, or Consume). The quantity of
	 * product involved in the activity is determined randomly between the minimum
	 * and maximum quantity.
	 *
	 * @throws InterruptedException if the thread is interrupted while waiting for a
	 *                              lock
	 */
	public void execute() throws InterruptedException {
		int quantity = new Random().nextInt(maxQuantity - minQuantity) + minQuantity;
		switch (type) {
		case BUY -> buy(player, product, quantity);
		case SELL -> sell(player, product, quantity);
		case BUILD -> build(player, product, quantity);
		case CONSUME -> consume(player, product, quantity);
		default -> throw new IllegalArgumentException("Invalid activity type: " + type);
		}
		this.finished = true;
	}

	/**
	 * Checks whether the activity has been executed and completed.
	 *
	 * @return true if the activity has been completed, false otherwise
	 */
	public boolean isFinished() {
		return this.finished;
	}

	/**
	 * A class representing the data necessary for an activity. An activity is an
	 * action that can be performed by a player, such as buying, selling, building,
	 * or consuming a product.
	 */
	/**
	 * A class representing the data necessary for an activity. An activity is an
	 * action that can be performed by a player, such as buying, selling, building,
	 * or consuming a product.
	 */
	public static class Data {

		ActivityType type;
		CatalogProduct product;
		int minQuantity;
		int maxQuantity;

		/**
		 * Constructor for creating a new Data object.
		 *
		 * @param type        the type of activity to perform (buy, sell, build, or
		 *                    consume)
		 * @param product     the product involved in the activity
		 * @param minQuantity the minimum quantity of product involved in the activity
		 * @param maxQuantity the maximum quantity of product involved in the activity
		 */
		@JsonCreator
		Data(@JsonProperty("type") String type, @JsonProperty("product") String product,
				@JsonProperty("min") int minQuantity, @JsonProperty("max") int maxQuantity) {
			this.type = ActivityType.fromName(type);
			this.product = CatalogProduct.getProductByName(product);
			this.minQuantity = minQuantity;
			this.maxQuantity = maxQuantity;
		}

		/**
		 * Returns the type of the activity.
		 *
		 * @return the activity type
		 */
		public ActivityType getType() {
			return type;
		}

		/**
		 * Returns the product involved in the activity.
		 *
		 * @return the product
		 */
		public CatalogProduct getProduct() {
			return product;
		}

		/**
		 * Returns the minimum quantity of product involved in the activity.
		 *
		 * @return the minimum quantity
		 */
		public int getMinQuantity() {
			return minQuantity;
		}

		/**
		 * Returns the maximum quantity of product involved in the activity.
		 *
		 * @return the maximum quantity
		 */
		public int getMaxQuantity() {
			return maxQuantity;
		}
	}

	/**
	 * An enum representing the types of activities that can be performed.
	 */
	public enum ActivityType {
		BUY, SELL, BUILD, CONSUME;

		/**
		 * Returns the activity type that corresponds to the given name.
		 *
		 * @param name the name of the activity type
		 * @return the activity type
		 * @throws IllegalArgumentException if the name is not a valid activity type
		 */
		public static ActivityType fromName(String name) {
			for (ActivityType type : ActivityType.values()) {
				if (type.name().equalsIgnoreCase(name)) {
					return type;
				}
			}
			throw new IllegalArgumentException("Invalid activity type name: " + name);
		}
	}
}}
