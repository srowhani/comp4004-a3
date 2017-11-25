package server.model.entity;

import java.util.List;

public class DealerEntity {
    private DeckEntity _deck;

    public DealerEntity () {
        this._deck = new DeckEntity();
    }

    public DeckEntity get_deck() {
        return _deck;
    }

    public void set_deck(DeckEntity _deck) {
        this._deck = _deck;
    }

    public void deal(List<CardEntity> hand) {
        hand.clear();
        for (int i = 0 ; i < 2; i++)
            hand.add(_deck.get_cards().remove(0));
    }
}
