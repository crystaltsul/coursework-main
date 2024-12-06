import java.io.File;
import java.io.FileWriter; 
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import player.card.Card;

public class Player extends Thread { // Extend Thread to make Player a thread
    private final int id; 
    private final int preferredValue; 
    private final Deck leftDeck;
    private final Deck rightDeck;
    private List<Card> hand = new ArrayList<>();
    public static volatile boolean hasWon = false;

    // Constructor
    public Player(int id, Deck leftDeck, Deck rightDeck) {
        this.id = id;
        this.preferredValue = id; 
        this.leftDeck = leftDeck;
        this.rightDeck = rightDeck;
    }

    // main thread execution logic
    @Override
    public void run() {
        System.out.println("Player " + id + " started.");
        createLogFile(); // create log file for this player
        logInitialState(); // log the initial state of the player's hand

        while (!Thread.currentThread().isInterrupted() && !hasWon) {
            if (checkWinningHand()) {
                declareWinner(); // Declare victory if player has a winning hand
                logGameEnd(true, id); // log the win to the player's file
                break;
            }

            // if leftDeck has cards and rightDeck isn't full, play the turn
            if (leftDeck.getDeckSize() > 0 && rightDeck.getDeckSize() < 5) {
                int[] playedCards = drawAndDiscard(); // perform a draw and discard action
                logTurn(playedCards[1], leftDeck.getDeckNumber(), playedCards[0], rightDeck.getDeckNumber()); // log turn
            }
        }
    
        if (Thread.currentThread().isInterrupted()) {
            System.out.println("Player " + id + " was interrupted.");
        }
    }

    // creates a log file for the player
    private void createLogFile() {
        try {
            File file = new File("player_" + id + "_log.txt");
            if (file.createNewFile()) {
                System.out.println("Log file created for Player " + id);
            }
        } catch (IOException e) {
            System.err.println("Error creating log file for Player " + id);
        }
    }

    // logs the initial state of the player's hand
    private void logInitialState() {
        try (FileWriter writer = new FileWriter("player_" + id + "_log.txt")) {
            writer.write("Player " + id + " initial hand: " + summarizeHand() + "\n\n");
        } catch (IOException e) {
            System.err.println("Error writing initial state for Player " + id);
        }
    }

    // logs the details of a turn
    private void logTurn(int drawnCard, int fromDeck, int discardedCard, int toDeck) {
        try {
            String turnLog = "Player " + id + " draws " + drawnCard + " from Deck " + fromDeck + "\n" +
                             "Player " + id + " discards " + discardedCard + " to Deck " + toDeck + "\n" +
                             "Current Hand: " + summarizeHand() + "\n\n";
            Files.write(Path.of("player_" + id + "_log.txt"), turnLog.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error logging turn for Player " + id);
        }
    }

    // logs the end of the game for the player
    private void logGameEnd(boolean isWinner, int winnerId) {
        try {
            String endLog = isWinner
                    ? "Player " + id + " wins the game!\n"
                    : "Player " + winnerId + " has won. Player " + id + " exits.\n";
            endLog += "Final hand: " + summarizeHand() + "\n";
            Files.write(Path.of("player_" + id + "_log.txt"), endLog.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error logging game end for Player " + id);
        }
    }

    // draws a card from left deck and discards one to right deck
    private int[] drawAndDiscard() {
        Card drawnCard = leftDeck.drawTopCard(); // draw a card from left deck
        hand.add(drawnCard);
        System.out.println("Player " + id + " drew card " + drawnCard.getValue());

        // discard a non-preferred card
        Card discardedCard = null;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getValue() != preferredValue) {
                discardedCard = hand.remove(i); // remove non-preferred card from the hand
                rightDeck.addBottomCard(discardedCard); // add card to bottom of right deck
                System.out.println("Player " + id + " discarded card " + discardedCard.getValue());
                return new int[]{discardedCard.getValue(), drawnCard.getValue()};
            }
        }

        // if all cards match the preferred value, discard the first card
        discardedCard = hand.remove(0);
        rightDeck.addBottomCard(discardedCard); // add the card to bottom of right deck
        System.out.println("Player " + id + " discarded card " + discardedCard.getValue() + " (all cards matched preferred value).");
        return new int[]{discardedCard.getValue(), drawnCard.getValue()};
    }

    // checks if player has a winning hand
    private boolean checkWinningHand() {
        return hand.size() == 4 && hand.stream().allMatch(card -> card.getValue() == preferredValue);
    }

    // declares the player as winner
    private void declareWinner() {
        hasWon = true;
        System.out.println("Player " + id + " wins the game!");
    }

    public static boolean hasGameEnded() {
        return hasWon; // Provide access to the flag if needed
    }

    // summarizes the player's hand as a string of card values
    public String summarizeHand() {
        StringBuilder summary = new StringBuilder();
        for (Card card : hand) {
            summary.append(card.getValue()).append(" ");
        }
        return summary.toString().trim();
    }

    // adds a card to the player's hand
    public void addCardToHand(Card card) {
        if (card != null) {
            hand.add(card);
            System.out.println("Player " + id + " added card " + card.getValue() + " to their hand.");
        } else {
            System.out.println("Player " + id + " attempted to add a null card to their hand!");
        }
    }

    // returns the player's hand
    public List<Card> getHand() {
        return hand;
    }

    // returns the player's ID
    public int getPlayerId() {
        return id;
    }
}

/*
Player Class: 
Represents a player in the game 
Draw, discard cards in a loop until there is a winner 

Attributes:
id: unique identifier for players (player 1, player 2, …)
value: integer that will represent player number (1, 2, 3…)
leftDeck
rightDeck 
Methods:
run(): main method where player’s draw, check cards, discard cards
checkWinningHand(): checks if all the cards in player’s hands have the same value 
declareWinner(): declares a winner 
preferredCard(): player’s preferred card, based on their playerID
discardCard(): finds and discards card that doesn’t match the preferred value 
addCardToHand(Card card): add cards to player’s hand at start of game  
*/
