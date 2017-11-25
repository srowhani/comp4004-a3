package server.model.entity;

import java.util.List;

public interface PlayerEntity {
    String getType();
    String get_username();
    List<CardEntity> getHand();
}
