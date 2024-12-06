import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import player.card.Card;

public class CardGame {
    private int value; //num of players and decks
    private List<Player> players = new ArrayList<>(); //list of players in game
    private List<Deck> decks = new ArrayList<>(); //list of decks, one for each player

    public CardGame(int numPlayers, CardDeck cardDeck){
        this.value = numPlayers;
        setupGame(cardDeck);
    }

    private boolean validatePack(List<Card> pack) {
        return pack.size() == 8 * value; // ensure pack has exactly 8 * numPlayers cards
    }
    
    private void setupGame(CardDeck cardDeck) {
        if (!validatePack(cardDeck.getDeck())) {
            System.out.println("Invalid pack file. Please check the file and try again.");
            return;
        }
        createDeck(value); // create decks for the players
        createPlayers(value); //create players
        distributeInitialCards(cardDeck.getDeck()); //distributes initial hands to players, fills each deck with remaining cards
        startGame(); //start game
    }

    private void createDeck(int numPlayers){
        for (int i = 0; i < numPlayers; i++) {
            decks.add(new Deck(i+1)); // create a new deck for each player
        }
    }

    private void createPlayers(int numPlayers){
        for (int i = 0; i < numPlayers; i++){
            Deck lefDeck = decks.get(i);
            Deck righDeck = decks.get((i + 1) % numPlayers); 
            Player player = new Player(i + 1, lefDeck, righDeck); //preferred value is set as id
            players.add(player);
        }
    }

    private void distributeInitialCards(List<Card> cardDeck) {
        // distribute 4 cards to each player
        for (Player player : players) {
            for (int i = 0; i < 4; i++) {
                if (!cardDeck.isEmpty()) {
                    Card card = cardDeck.remove(0);
                    player.addCardToHand(card);
                    System.out.println("Dealt card " + card.getValue() + " to Player " + player.getId());
                }
            }
        }
    
        // distribute remaining cards to each deck
        int deckIndex = 0;
        while (!cardDeck.isEmpty()) {
            Deck deck = decks.get(deckIndex);
            Card card = cardDeck.remove(0);
            deck.addBottomCard(card);
            deckIndex = (deckIndex + 1) % decks.size(); // rotate to the next deck
        }
    
        // save the state of each deck
        for (Deck deck : decks) {
            deck.saveDeckToFile();
        }
    }
    
    // private void startGame(){
    //     players.forEach(Player::start); //start each player in a separate thread
    // }

    private void startGame() {
        players.forEach(player -> {
            if (player.getState() == Thread.State.NEW) { // Only start threads that are in the NEW state
                player.start();
            }
        });
    
        // Wait for a winner
        while (!Player.hasGameEnded()) {
            // Busy-wait until a player wins
        }
    
        // Interrupt all threads after a winner is declared
        players.forEach(Thread::interrupt);
        System.out.println("Game has ended. Interrupting all players.");
    }    

    public void displayGameState() {
        System.out.println("Current game state: " + players.size() + " players in the game.");
    }

    public static int checkNumOfPlayers(Scanner inputScanner) {
        int numPlayers;
        while (true) {
            System.out.print("Enter the number of players: ");
            if (inputScanner.hasNextInt()) {
                numPlayers = inputScanner.nextInt();
                inputScanner.nextLine();
                if (numPlayers > 0) {
                    return numPlayers; // valid input, exit the loop
                } else {
                    System.out.println("Invalid input. Enter a positive integer.");
                }
            } else {
                System.out.println("Invalid input. Enter an integer value.");
                inputScanner.next(); // clear invalid input
            }
        }
    }
    
    public static ArrayList getValidatedPackFile(Scanner inputScanner, int players) {
        while (true) {
            System.out.print("Enter the location of the pack to load: ");
            String packFilePath = inputScanner.nextLine().trim();

            // Validate file path
            if (packFilePath.isEmpty()) {
                System.out.println("Input cannot be empty. Try again.");
                continue;
            }
            if (!packFilePath.endsWith(".txt")) {
                System.out.println("Invalid file type. The file must have a '.txt' extension. Try again.");
                continue;
            }

            // Attempt to load the file
            File packFile = new File(packFilePath);
            if (!packFile.exists() || packFile.isDirectory() || !packFile.isFile()) {
                System.out.println("File not found or invalid. Try again.");
                continue;
            }

            // Validate file contents
            try (Scanner fileScanner = new Scanner(packFile)) {
                int lineCount = 0;

                ArrayList<Card> deck = new ArrayList<>();

                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    lineCount++;

                    // Ensure each line is a positive integer
                    try {
                        int value = Integer.parseInt(line);
                        if (value < 0) {
                            System.out.println("File contains a negative number. Try again.");
                            continue;
                        }
                        deck.add(new Card(value));
                    } catch (NumberFormatException e) {
                        System.out.println("File contains invalid input: " + line + ". Try again.");
                        continue;
                    }
                }

                // Check if the line count matches the expected value
                if (lineCount != 8 * players) {
                    System.out.println("File must contain exactly " + (8 * players) + " lines. Try again.");
                    continue;
                }

                // If all validations pass, return the file
                return deck;

            } catch (FileNotFoundException e) {
                System.out.println("Error reading the file. Try again.");
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Scanner inputScanner = new Scanner(System.in);
    
        // try {
            // get the number of players
            int numPlayers = checkNumOfPlayers(inputScanner);

            // get the valid input pack
            ArrayList<Card> pack = getValidatedPackFile(inputScanner, numPlayers);
    
            // initialize the card deck
            CardDeck cardDeck = new CardDeck(pack); // no file needed, generates cards programmatically
    
            //create and set up the card game
            CardGame cardGame = new CardGame(numPlayers, cardDeck);
    
            // display initial game state
            cardGame.displayGameState();
    
            // start game
            System.out.println("The game has started with " + numPlayers + " players.");
            cardGame.startGame();

            System.out.println("The game has ended. Thank you for playing!");
        // } catch (Exception e) {
        //    System.err.println("An error occurred during game setup: " + e.getMessage());
        //    e.printStackTrace();;
        // } finally {
        //    inputScanner.close();
        // }

    }

    public void finalizeGame() {
        decks.forEach(Deck::saveDeckToFile);
        System.out.println("Final deck states saved.");
    }    
}

/*
Manages flow of game 
Determines number of players
Prepares each deck 
(longest class)

Attributes:
value: integer that will represent number of player and decks
players: list of Player objects - represents each player in the game
Methods: 
CardGame(int numPlayers, String CardDeck)
createDeck(int numPlayers): ozoos 1 deck per player
createPlayers(int numPlayers): gives each player their right and left decks 
distributeInitialCards(): distributes initial hands to players, fills each deck with remaining cards
startGame(): starts game

main: 
requests num of players in game
location of valid input pack
*/