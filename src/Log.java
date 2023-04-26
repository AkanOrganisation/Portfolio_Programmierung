import java.util.*;

/**
 *
 * A singleton class that represents a log of messages for each round of a game.
 * Messages can be added to the current round, and all messages for a specific
 * round or for all rounds can be printed.
 */
public class Log {

    private final Map<Integer, List<String>> roundToMessagesMap;
    private int currentRound;

    /**
     * Private constructor to prevent instantiation. Initializes roundToMessagesMap
     * as a synchronized map and sets currentRound to 0.
     */
    private Log() {
        roundToMessagesMap = Collections.synchronizedMap(new HashMap<>());
        currentRound = 0;
    }

    /**
     * A private static inner class that holds a single instance of the Log class.
     */
    private static final class InstanceHolder {
        private static final Log instance = new Log();
    }

    /**
     * Static method that returns the singleton instance of the Log class.
     *
     * @return the singleton instance of the Log class.
     */
    public static Log getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Sets the current round to the given round.
     *
     * @param round the round to set as the current round.
     */
    public void setRound(int round) {
        currentRound = round;
    }

    /**
     * Adds a message to the current round.
     *
     * @param message the message to add to the current round.
     */
    public synchronized void addMessage(String message) {
        roundToMessagesMap.computeIfAbsent(currentRound, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(message);
    }

    /**
     * Gets all messages for the specified round.
     *
     * @param round the round for which to get all messages.
     * @return a synchronized list of all messages for the specified round.
     */
    public synchronized List<String> getMessages(int round) {
        return roundToMessagesMap.getOrDefault(round, new ArrayList<>());
    }

    /**
     * Prints all messages for the specified round.
     *
     * @param round the round for which to print all messages.
     */
    public synchronized void printMessagesForRound(int round) {
        List<String> messages = roundToMessagesMap.getOrDefault(round, new ArrayList<>());
        System.out.println("Round " + (round + 1) + ":");
        for (String message : messages) {
            System.out.println("\t" + message);
        }
    }

    /**
     * Prints all messages for all rounds.
     */
    public synchronized void printAllMessages() {
        for (Map.Entry<Integer, List<String>> entry : roundToMessagesMap.entrySet()) {
            System.out.println("Round " + entry.getKey() + ":");
            for (String message : entry.getValue()) {
                System.out.println("\t" + message);
            }
        }
    }
}
