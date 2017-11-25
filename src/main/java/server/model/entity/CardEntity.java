package server.model.entity;

public class CardEntity {
    private String suit;
    private int value;
    private String mappedValuetoString;

    public CardEntity () {}

    public CardEntity (String suit, int value, String mappedValuetoString) {
        this.suit = suit;
        this.value = value;
        this.mappedValuetoString = mappedValuetoString;
    }

    public float getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public String getMappedValuetoString() {
        return mappedValuetoString;
    }

    @Override
    public String toString () {
        return this.mappedValuetoString + this.suit;
    }
}
