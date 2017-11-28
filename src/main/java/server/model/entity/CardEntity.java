package server.model.entity;

public class CardEntity {
    private String suit;
    private int value;
    private String mappedValuetoString;
    private boolean publiclyVisible;
    public CardEntity () {}

    public CardEntity (String suit, int value, String mappedValuetoString) {
        this.publiclyVisible = false;
        this.suit = suit;
        this.value = value;
        this.mappedValuetoString = mappedValuetoString;
    }

    public int getValue() {
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
        if (publiclyVisible) {
            return this.mappedValuetoString + this.suit;
        }
        return "XX";
    }

    public boolean isPubliclyVisible() {
        return publiclyVisible;
    }

    public void setPubliclyVisible (boolean publiclyVisible) {
        this.publiclyVisible = publiclyVisible;
    }
}
