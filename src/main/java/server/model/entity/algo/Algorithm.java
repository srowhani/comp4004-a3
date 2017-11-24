package server.model.entity.algo;

import server.model.entity.HandEntity;

public interface Algorithm {
    float calculatePercentile(HandEntity mHand, HandEntity field);
}
