import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A singleton class that represents a log of messages for each round of a game.
 * Messages can be added to the current round, and all messages for a specific
 * round or for all rounds can be printed.
 */
public class Log {

    private final Map<Integer, List<LogMessage>> roundToMessagesMap;
    private int currentRound;

    /**
     * Private constructor to prevent instantiation. Initializes roundToMessagesMap
     * as a synchronized map and sets currentRound to 0.
     */
    private Log() {
        roundToMessagesMap = new ConcurrentHashMap<>();
        currentRound = 0;
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
    public synchronized void addMessage(String message, Level level) {
        roundToMessagesMap.computeIfAbsent(currentRound, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new LogMessage(message, level));
    }

    /**
     * Gets all messages for the specified round.
     *
     * @param round the round for which to get all messages.
     * @return a synchronized list of all messages for the specified round.
     */
    public synchronized List<LogMessage> getMessages(int round) {
        return roundToMessagesMap.getOrDefault(round, new ArrayList<>());
    }

    /**
     * Prints all messages for the specified round.
     *
     * @param round the round for which to print all messages.
     */
    public synchronized void printMessagesForRound(int round) {
        List<String> messages = roundToMessagesMap.getOrDefault(round, new ArrayList<>()).stream().filter(m -> m.getLevel() == Level.INFO).map(LogMessage::getMessage).toList();
        System.out.println("Round " + (round + 1) + ":");
        for (String message : messages) {
            System.out.println("\t" + message);
        }
    }

    /**
     * Prints all messages for all rounds.
     */
    public enum Level {
        INFO(0),
        WARNING(1),
        ERROR(2),
        DEBUG(3);

        private final int level;

        private Level(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * A private static inner class that holds a single instance of the Log class.
     */
    private static final class InstanceHolder {
        private static final Log instance = new Log();
    }

    private class LogMessage {
        private final String message;
        private final Level level;

        private LogMessage(String message, Level level) {
            this.message = message;
            this.level = level;
        }

        public String getMessage() {
            return message;
        }

        public Level getLevel() {
            return level;
        }

    }
}
