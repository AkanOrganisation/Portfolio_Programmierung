package Player;

import Player.Activity.ActivityData;
import Synchronizer.Synchronizer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PlayerController implements Runnable {

    private static final ArrayList<PlayerController> playerControllers = new ArrayList<>();

    private final String name;
    private final PlayerType type;
    private final ArrayList<ActivityData> activities;

    @JsonCreator
    PlayerController(@JsonProperty("name") String name,
                     @JsonProperty("type") String type,
                     @JsonProperty("activities") ArrayList<ActivityData> activities) {
        this.name = name;
        this.type = PlayerType.fromName(type);
        this.activities = activities;

        playerControllers.add(this);
    }

    // Read JSON data from file
    public static void loadFromJsonFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(new File(filePath), PlayerController[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getNumberOfPlayers() {
        return playerControllers.size();
    }

    public static ArrayList<PlayerController> getPlayersControllers() {
        return playerControllers;
    }


    @Override
    public void run() {
        Player player = new Player(this.name, this.type, activities);

        // Notify the player is loaded
        Synchronizer.notifyPlayerLoaded();

        // Wait until the game starts
        try {
            Synchronizer.waitGameStart();
        } catch (InterruptedException e) {
            player.log("Player %s left before the game started".formatted(this.name));
            throw new RuntimeException(e);
        }

        // Play the game until finished
        while (!Synchronizer.gameFinished()) {
            try {
                // Wait for a new round
                Synchronizer.waitRoundStarted();

                // Play the round
                player.log("Player %s starting a new round".formatted(this.name));
                player.playRound();

                // Mark turn as finished
                Synchronizer.notifyPlayerFinishedRound();
                player.log("Player %s finished the round".formatted(this.name));

                // Wait for round's end
                Synchronizer.waitRoundFinished();

            } catch (InterruptedException e) {
                if (!Synchronizer.gameFinished()) {
                    player.log("Player %s left before the game finished".formatted(this.name));
                    Player.removePlayer(player);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

}
