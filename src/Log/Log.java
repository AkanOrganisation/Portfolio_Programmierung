package Log;


import java.util.*;

public class Log {

    private final Map<Integer, List<String>> roundToMessagesMap;
    private int currentRound;

    // Private constructor to prevent instantiation
    private Log() {
        roundToMessagesMap = Collections.synchronizedMap(new HashMap<>());
        currentRound = 0;
    }

    private static final class InstanceHolder {
        private static final Log instance = new Log();
    }

    // Static method to get the singleton instance
    public static Log getInstance() {
        return InstanceHolder.instance;
    }

    // Set the current round
    public void setRound(int round) {
        currentRound = round;
    }

    // Add a message to the current round
    public synchronized void addMessage(String message) {
        roundToMessagesMap.computeIfAbsent(currentRound, k -> Collections.synchronizedList(new ArrayList<>())).add(message);
    }

    // Get all messages for the specified round
    public synchronized List<String> getMessages(int round) {
        return roundToMessagesMap.getOrDefault(round, new ArrayList<>());
    }

    // Print all messages for a specific round
    public synchronized void printMessagesForRound(int round) {
        List<String> messages = roundToMessagesMap.getOrDefault(round, new ArrayList<>());
        System.out.println("Round " + (round+1) + ":");
        for (String message : messages) {
            System.out.println("\t" + message);
        }
    }

    // Print all messages for all rounds
    public synchronized void printAllMessages() {
        for (Map.Entry<Integer, List<String>> entry : roundToMessagesMap.entrySet()) {
            System.out.println("Round " + entry.getKey() + ":");
            for (String message : entry.getValue()) {
                System.out.println("\t" + message);
            }
        }
    }
}
