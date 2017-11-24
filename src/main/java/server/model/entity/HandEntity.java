package server.model.entity;

import java.util.List;

public class HandEntity {
    private List<CardEntity> _cards;

    public List<CardEntity> get_cards() {
        return _cards;
    }

    public void set_cards(List<CardEntity> _cards) {
        this._cards = _cards;
    }
}
