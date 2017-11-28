package server.model.entity.algo;

import server.model.entity.CardEntity;
import server.model.entity.HandEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Algorithm {
    public abstract boolean shouldHold(HandEntity mHand, List<HandEntity> otherHands, boolean goingFirst);

    public double isStraight(HandEntity mHand) {
        double result = 0.0;
        int[] values = mHand.get_cards().stream().mapToInt(CardEntity::getValue).toArray();
        Arrays.sort(values);
        for (int i = 0; i < values.length - 1; i++){
            if (values[i] == values[i + 1] - 1) {
                result = 4.0 + (values[i + 1] * 0.01);
            } else {
                return 0.0;
            }
        }

        return result;
    }
    public double isFlush(HandEntity mHand) {
        double result = 0.0;

        List<String> suits = mHand.get_cards().stream().map(CardEntity::getSuit).collect(Collectors.toList());
        Collections.sort(suits);
        int[] values = mHand.get_cards().stream().mapToInt(CardEntity::getValue).toArray();
        Arrays.sort(values);
        String suit = suits.get(0);

        for (int i = 0; i < suits.size(); i++) {
            result = 5.0 + (values[i] * 0.01);

            if (!suits.get(i).equals(suit)) {
                return 0.0;
            }
        }

        return result;
    }

    public double isFullHouse(HandEntity mHand) {
        double result = 0.0;
        boolean one = false;
        boolean two = false;

        int[] values = mHand.get_cards().stream().mapToInt(CardEntity::getValue).toArray();
        Arrays.sort(values);

        for (int i = 0; i < values.length - 2; i++) {
            if (values[i] == values[i + 1] && values[i] == values[i + 2]) {
                one = true;
            }
        }

        if (values[3] == values[4]) {
            two = true;
        }

        if (one && two) {
            result = 6.0 + (values[values.length - 1] * 0.01);
        }

        return result;
    }

    public double isFourOfAKind(HandEntity mHand) {
        double result = 0.0;
        int counter = 0;

        int[] values = mHand.get_cards().stream().mapToInt(CardEntity::getValue).toArray();
        Arrays.sort(values);

        for (int i = 0; i < values.length - 3; i++) {
            if (values[i] == values[i + 1] && values[i] == values[i + 2] && values[i] == values[i + 3]) {
                result = 7.0 + (values[i] * 0.01);
            }
        }

        return result;
    }

    public double isStraightFlush(HandEntity mHand) {
        double result = 0.0;

        if (isStraight(mHand) > 4.0 && isFlush(mHand) > 5.0) {
            result = 8.0 + isStraight(mHand) - 4.0;
        }

        return result;
    }

    public double isPair(HandEntity mHand) {
        double result = 0.0;
        int[] values = mHand.get_cards().stream().mapToInt(CardEntity::getValue).toArray();
        Arrays.sort(values);

        for (int i = 0; i < values.length - 1; i++) {
            if (values[i] == values[i + 1]) {
                result = 1.0 + (values[i] * 0.01);
            }
        }

        return result;
    }

    public double isTwoPair(HandEntity mHand) {
        double result = 0.0;
        double value = 0.0;
        int counter = 0; // Number of pairs.
        int[] values = mHand.get_cards().stream().mapToInt(CardEntity::getValue).toArray();
        Arrays.sort(values);
        for (int i = 0; i < values.length - 1; i++) {
            if (values[i] == values[i + 1]) {
                counter++;

                value = values[i] * 0.01;
            }
        }

        if (counter == 2) {
            result = 2.0 + value;
        }

        return result;
    }
    public double isThreeOfAKind(HandEntity mHand) {
        double result = 0.0;
        int counter = 0;
        int[] values = mHand.get_cards().stream().mapToInt(CardEntity::getValue).toArray();
        Arrays.sort(values);
        for (int i = 0; i < values.length - 2; i++) {
            if (values[i] == values[i + 1] && values[i] == values[i + 2]) {
                result = 3.0 + (values[i] * 0.01);
            }
        }

        return result;
    }

    public double getRanking(HandEntity mHand) {
        double ranking = 0;

        if (isStraightFlush(mHand) > 8.0) {
            ranking = isStraightFlush(mHand);
        } else if (isFourOfAKind(mHand) > 7.0) {
            ranking = isFourOfAKind(mHand);
        } else if (isFullHouse(mHand) > 6.0) {
            ranking = isFullHouse(mHand);
        } else if (isFlush(mHand) > 5.0) {
            ranking = isFlush(mHand);
        } else if (isStraight(mHand) > 4.0) {
            ranking = isStraight(mHand);
        } else if (isThreeOfAKind(mHand) > 3.0) {
            ranking = isThreeOfAKind(mHand);
        } else if (isTwoPair(mHand) > 2.0) {
            ranking = isTwoPair(mHand);
        }   else if (isPair(mHand) > 1.0) {
            ranking = isPair(mHand);
        }

        return ranking;
    }
}
