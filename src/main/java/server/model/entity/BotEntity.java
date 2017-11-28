package server.model.entity;

import server.model.entity.algo.Algorithm;

import java.util.ArrayList;
import java.util.List;

public class BotEntity implements PlayerEntity {
    private String username;
    private Algorithm _algo;
    private int balance;
    private HandEntity _hand;
    private int playIndex = 0;
    private boolean hasFolded;

    public BotEntity () {
        hasFolded = false;
        _hand = new HandEntity();
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
    public int getBalance() {
        return balance;
    }

    @Override
    public void setBalance(int newBalance) {
        this.balance = newBalance;
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
    public HandEntity getHand() {
        return _hand;
    }

    @Override
    public void fold() {
        this.hasFolded = true;
    }
    @Override
    public boolean hasFolded () {
        return hasFolded;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPlayIndex(int playIndex) {
        this.playIndex = playIndex;
    }
}
