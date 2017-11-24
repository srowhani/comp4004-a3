package server.model.entity;

public class CardEntity {
    private String suit;
    private float value;

    public CardEntity () {}

    public CardEntity (String suit, float value) {
        this.suit = suit;
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }
}
