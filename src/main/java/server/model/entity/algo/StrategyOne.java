package server.model.entity.algo;

import server.model.entity.CardEntity;
import server.model.entity.HandEntity;
import server.model.entity.RoomEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrategyOne extends Algorithm {
    @Override
    public boolean shouldHold(HandEntity mHand, RoomEntity room, List<HandEntity> otherHands, boolean goingFirst) {
        if (getRanking(mHand) > 4) {
            return true;
        } else {
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
            if (isFullHouse(mHand) >= 6 && isFullHouse(mHand) <= 7) {
                return true;
            }
        }
        return false;
    }
}
