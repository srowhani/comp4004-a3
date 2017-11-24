package server.model.entity.algo;

import server.model.entity.CardEntity;
import server.model.entity.HandEntity;


public class HighCardAlgorithm implements Algorithm {
    @Override
    public float calculatePercentile(HandEntity mHand, HandEntity field) {
        float prob = 0;
        for (CardEntity c : mHand.get_cards()) {
            if (c.getValue() > 10) {
                prob = 1;
            }
        }
        return prob;
    }
}
