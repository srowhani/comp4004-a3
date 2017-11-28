package server.model.entity;

import java.util.List;

public interface PlayerEntity {
    int getBalance();
    void setBalance(int newBalance);
    String getType();
    String get_username();
    HandEntity getHand();

    void fold();
    boolean hasFolded();
}
