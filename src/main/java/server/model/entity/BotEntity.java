package server.model.entity;

import server.model.entity.algo.Algorithm;

public class BotEntity implements PlayerEntity {
    private String username;
    private Algorithm _algo;

    public BotEntity () {}

    public BotEntity (String username) {
        this.username = username;
    }

    public void setAlgorithm (Algorithm algo) {
        this._algo = algo;
    }

    @Override
    public String getType() {
        return "bot";
    }

    @Override
    public String get_username() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
