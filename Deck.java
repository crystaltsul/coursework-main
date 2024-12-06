import player.card.Card;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Deck {
    private final int deckNumber;
    private final List<Card> deckCards = new ArrayList<>(); //dynamic list for deck cards

    public Deck(int deckNumber) {
        this.deckNumber = deckNumber;
        createFile(); // create output file for deck
        System.out.println("Deck " + deckNumber + " initialized.");
    }
    

    public int getDeckNumber() {
        return deckNumber; //returns deck number
    }

    public synchronized int getDeckSize() {
        return deckCards.size(); //returns deck size
    }

    public String getFilename() {
        return "output/deck" + deckNumber + "_output.txt"; // filename for deck's output file
    }    

    public synchronized Card drawTopCard() {
        if (!deckCards.isEmpty()) { // check if deck has cards
            Card card = deckCards.remove(0);
            saveDeckToFile(); // update the file after modifying the deck
            return card; // return drawn card
        }
        return null; // return null if deck is empty 
    }
    
    public synchronized void addBottomCard(Card card) {
        deckCards.add(card); // add card to bottom 
        System.out.println("Card " + card.getValue() + " added to Deck " + deckNumber);
        saveDeckToFile(); // save updated deck to file 
    }    

    public void assignCard(Card card, int position) {
        // adds a card to a specified position in deck
        if (position >= 0 && position <= deckCards.size()){
            deckCards.add(position, card); // adds card to specified position 
        } else {
            throw new IndexOutOfBoundsException("Invalid position for card insertion.");
        }
    }

    public synchronized String getValueString() {
        if (deckCards.isEmpty()) { // check if deck is empty 
            System.out.println("Deck " + deckNumber + " is empty!"); // log if deck is empty 
        }
        StringBuilder value = new StringBuilder();
        for (Card card : deckCards) { // loop through all cards in deck 
            value.append(card.getValue()).append(" ");
        }
        if (value.length() > 0) {
            value.setLength(value.length() - 1); 
        }
        return value.toString();
    }    

    private void createFile() { // creates output file for deck 
        try {
            File file = new File(getFilename());
            System.out.println("Attempting to create file at: " + file.getAbsolutePath());
            if (file.createNewFile()) {
                System.out.println("File created for Deck " + deckNumber); // log success
            } else {
                System.out.println("File already exists for Deck " + deckNumber); // log if file already exists 
            }
        } catch (IOException e) {
            System.err.println("Error creating file for Deck " + deckNumber); // log if file creation fails 
            e.printStackTrace();
        }
    }

    public void writeFile() {
        try (FileWriter writer = new FileWriter(getFilename())) {
            //writes current contents of deck to its output file 
            //overwrites any existing contents in the file
            writer.write("deck" + deckNumber + " contents: " + getValueString());
        } catch (IOException e) {
            System.err.println("Error writing to file for deck " + deckNumber);
            e.printStackTrace();
        }
    }

    public synchronized void saveDeckToFile() { // saves deck's current contents to its output file 
        try (FileWriter writer = new FileWriter(getFilename())) {
            String content = "deck" + deckNumber + " contents: " + getValueString();
            writer.write(content); // write content to file 
            System.out.println("Deck " + deckNumber + " saved to file."); // log success
        } catch (IOException e) {
            System.err.println("Error saving contents to file for Deck " + deckNumber); // log error if saving fails 
            e.printStackTrace();
        }
    }    

}

/*
Attributes:
value: integer that will represent player (= deck) number (1, 2, 3…)
cards: a Queue<Card> that stores cards in deck

Methods:
addCard(Card card): adds card to deck, places it at end of the queue 
drawCard(): removes and returns top card of the deck. 
*/