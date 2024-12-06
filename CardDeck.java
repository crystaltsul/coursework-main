import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import player.card.Card;

public class CardDeck {
    private final ArrayList<Card> deck;

    public CardDeck(ArrayList<Card> deck) {
        this.deck = deck;
        Collections.shuffle(deck); // shuffles deck to make order random
    }

    public List<Card> getDeck(){
        return deck;
    }

    /*
    public synchronized Card drawCard(){
        return deck.isEmpty() ? null : deck.remove(0); //draws top card from deck
    }
    */
}

/*
Represents the deck of cards in the game 
Creating, shuffling, distribute cards 

Attributes:
deck: ArrayList<Card> that will hold a full set of cards for the game
Methods: 
CardDeck(int numPlayers): constructor that initialises the deck with 8 * numPlayers cards in a round-robin manner (loop)
initializeDeck(int numPlayers): distribute deck with required number of cards in a loop
getDeck(): returns list of Card objects
drawCard(): allows players to draw card
 */
