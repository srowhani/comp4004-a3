package server.model.entity;

import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.List;

public class UserEntity implements PlayerEntity {
    private Session _session;
    private String _username;
    private int _balance;
    private boolean hasFolded;
    private HandEntity _hand;
    public UserEntity(Session session, String username) {
        hasFolded = false;
        _session = session;
         _username = username;
        _hand = new HandEntity();
    }

    public String get_username() {
        return _username;
    }

    @Override
    public HandEntity getHand() {
        return _hand;
    }

    @Override
    public void fold() {
        hasFolded = true;
    }

    @Override
    public boolean hasFolded() {
        return hasFolded;
    }

    public Session get_session() {
        return _session;
    }

    @Override
    public int getBalance() {
        return _balance;
    }

    @Override
    public void setBalance(int newBalance) {
        this._balance = newBalance;
    }

    @Override
    public String getType() {
        return "user";
    }
}
