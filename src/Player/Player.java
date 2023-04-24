package Player;

import Log.Log;
import Player.Activity.Activity;
import Player.Activity.ActivityData;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Player {

    private final static ArrayList<Player> players = new ArrayList<>();

    private static int nextId = 0;
    final String name;
    public int id;
    public PlayerType type;
    public Stock stock;
    public double money = Double.POSITIVE_INFINITY;
    public ArrayList<Activity> activities;

    public Player(String name, PlayerType type, ArrayList<ActivityData> activities) {
        this.id = getNextId();
        this.name = name;
        this.type = type;
        this.activities = activities.stream().map(activityData -> new Activity(this, activityData.getType(), activityData.getProduct(), activityData.getMinQuantity(), activityData.getMaxQuantity())).collect(Collectors.toCollection(ArrayList::new));
        this.stock = new Stock();

        //add a reference to self
        addToList(this);
    }

    public static int getNumberOfActivePlayers() {
        return players.size();
    }

    public static void removePlayer(Player player) {
        players.remove(player);
    }


    private synchronized int getNextId() {
        return nextId++;
    }

    private synchronized static void addToList(Player player) {
        players.add(player);
    }

    public void playRound() throws InterruptedException {
        prioritizeActivities();
        for (Activity activity : activities) activity.execute();
    }

    public void prioritizeActivities() {
        //TODO:
        // Implementation to be defined
    }

    public void log(String message) {
        Log.getInstance().addMessage(message);
    }


    public String getName() {
        return name;
    }
}

