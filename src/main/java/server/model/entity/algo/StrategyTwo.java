package server.model.entity.algo;

import com.fasterxml.jackson.databind.ObjectMapper;
import server.model.StateUpdate;
import server.model.entity.CardEntity;
import server.model.entity.HandEntity;
import server.model.entity.RoomEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StrategyTwo extends Algorithm {
    @Override
    public boolean shouldHold(HandEntity mHand, RoomEntity room, List<HandEntity> otherHands, boolean goingFirst) {
        if (goingFirst) {
            room.getUsers().forEach(u -> {
                try {
                    u.get_session().getRemote().sendString(new ObjectMapper().writeValueAsString(new StateUpdate("strat_one")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return new StrategyOne().shouldHold(mHand, room, otherHands, goingFirst);
        } else {
            for (HandEntity hand : otherHands) {
                List<CardEntity> cards = hand.get_cards().stream().filter(c -> c.isPubliclyVisible()).collect(Collectors.toList());

                Map<String, Integer> suitCount = new HashMap();
                cards.forEach(c -> {
                    suitCount.put(c.getSuit(), suitCount.getOrDefault(c.getSuit(), 0) + 1);
                });
                int maxNumSame = 0;
                for (String key : suitCount.keySet()) {
                    if (suitCount.get(key) > maxNumSame) {
                        maxNumSame = suitCount.get(key);
                    }
                }
                if (maxNumSame > 2) {
                    Map<Integer, Integer> mc = new HashMap();
                    for (CardEntity _c : mHand.get_cards()) {
                        mc.put(_c.getValue(), mc.getOrDefault(_c.getValue(), 0) + 1);
                    }
                    int numCardsRemoved = 0;
                    for (int key : mc.keySet()) {
                        if (mc.get(key) < 2) {
                            CardEntity cardToRemove = mHand.get_cards().stream().filter(c -> c.getValue() == key).findFirst().get();
                            mHand.get_cards().remove(cardToRemove);
                            numCardsRemoved++;
                        }
                    }
                    room.getDealer().deal(mHand.get_cards(), numCardsRemoved, true);
                    return true;
                }
            }
            room.getUsers().forEach(u -> {
                try {
                    u.get_session().getRemote().sendString(new ObjectMapper().writeValueAsString(new StateUpdate("strat_one")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return new StrategyOne().shouldHold(mHand, room, otherHands, goingFirst);
        }
    }
}
