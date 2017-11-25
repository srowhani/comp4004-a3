package server.model.entity;

import server.model.entity.algo.Algorithm;

import java.util.ArrayList;
import java.util.List;

public class BotEntity implements PlayerEntity {
    private String username;
    private Algorithm _algo;
    private List<CardEntity> _hand;

    public BotEntity () {
        _hand = new ArrayList();
    }

    public BotEntity (String username) {
        this();
        this.username = username;
    }

    public void setAlgorithm (Algorithm algo) {
        this._algo = algo;
    }

    public Algorithm getAlgorithm () {
        return this._algo;
    }

    @Override
    public String getType() {
        return "bot";
    }

    @Override
    public String get_username() {
        return username;
    }

    @Override
    public List<CardEntity> getHand() {
        return _hand;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
