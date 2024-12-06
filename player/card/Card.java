package player.card;

//class is thread safe
public class Card {
    private final int value;

    //constructs the card with a given value
    public Card(int value) {
        this.value = value;
    }

    //returns the value of the card
    public synchronized int getValue() {
        return value;
    }
}