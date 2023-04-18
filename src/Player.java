import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class Player implements Runnable {

    public static ArrayList<Player> players = new ArrayList<>();
    private static int nextId = 1;
    public int id;
    public PlayerType type;
    public Stock stock;
    public double money = Double.POSITIVE_INFINITY;
    public boolean roundFinished = false;
    public ArrayList<Activity> activities;
    private CountDownLatch latch;

    public Player(String name, PlayerType type, ArrayList<ActivityData> activities) {
        this.id = getNextId();
        this.type = type;
        this.activities = activities.stream().map(activityData -> new Activity(this, activityData.type, activityData.product, activityData.minQuantity, activityData.maxQuantity )).collect(Collectors.toCollection(ArrayList::new));
        this.stock = new Stock();

        //add a reference to self
        addToList(this);
    }


    private synchronized int getNextId() {
        return id++;
    }

    private synchronized static void addToList(Player player) {
        players.add(player);
    }

    public void playRound() throws InterruptedException {
        prioritizeActivities();
        for(Activity activity : activities)activity.execute();
        latch.countDown();
        roundFinished = true;
    }

    public void prioritizeActivities() {
        // Implementation to be defined
    }

    public void log(String message) {
        // Implementation to be defined
    }

    public boolean isRoundFinished(){
        return roundFinished;
    }
    private void setHasFinishedTurn(boolean b) {
        this.roundFinished = true;
    }
    @Override
    public void run() {
        while (!Main.finished) {
            synchronized (Main.class) {
                try {
                    Main.class.wait(); // Wait for notification from Main class
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            // Play the round
            try {
                playRound();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Mark turn as finished
            setHasFinishedTurn(true);
        }
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }
}
