package server.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckEntity {
    private String[] suits = {"H", "D", "C", "S"};
    private String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private List<CardEntity> _cards;

    public DeckEntity () {
        this._init();
    }

    public void _init() {
        _cards = new ArrayList();
        for (int i = 0 ; i < suits.length; i++) {
            for (int j = 0 ; j < values.length; j++) {
                _cards.add(new CardEntity(suits[i], j + 1, values[j]));
            }
        }
        Collections.shuffle(_cards);
    }

    public void set_cards(List<CardEntity> _cards) {
        this._cards = _cards;
    }
    public List<CardEntity> get_cards() {
        return this._cards;
    }
}
