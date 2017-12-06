package server.model.entity.algo;

import server.model.entity.CardEntity;
import server.model.entity.HandEntity;
import server.model.entity.PlayerEntity;
import server.model.entity.RoomEntity;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Algorithm {
    public abstract boolean shouldHold(HandEntity mHand, RoomEntity room, List<HandEntity> otherHands, boolean goingFirst);

    public double isStraight(HandEntity mHand) {
        double result = 0.0;
        int[] values = mHand.get_cards().stream().mapToInt(CardEntity::getValue).toArray();
        Arrays.sort(values);

        if (values[0] == 1 && values[1] != 2) {
            if (values[values.length - 1] == 13) {
                values[0] = 14;
                Arrays.sort(values);
            } else {
                return 0;
            }
        }

        for (int i = 0; i < values.length - 1; i++) {
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

        int[] values = mHand.get_cards().stream().mapToInt(CardEntity::getValue).toArray();
        Arrays.sort(values);

        Map<Integer, Integer> f = new HashMap();
        for (int value : values) {
            f.put(value, f.getOrDefault(value, 0) + 1);
        }


        if (f.containsValue(3) && f.containsValue(2)) {
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
        int[] values = mHand.get_cards().stream().mapToInt(CardEntity::getValue).toArray();
        Arrays.sort(values);
        for (int i = 0; i < values.length - 2; i++) {
            if (values[i] == values[i + 1] && values[i] == values[i + 2]) {
                result = 3.0 + (values[i] * 0.01);
            }
        }

        return result;
    }

    public int compare (PlayerEntity u1, PlayerEntity u2) {
        double d1, d2;
        HandEntity h1 = u1.getHand();
        HandEntity h2 = u2.getHand();

        d1 = u1.hasFolded() ? -1 : getRanking(h1);
        d2 = u2.hasFolded() ? -1 : getRanking(h2);

        if (d1 > d2) {
            return 1;
        } else if (d2 > d1) {
            return -1;
        } else {
            Optional<CardEntity> maxCard1 = h1.get_cards().stream().max((c, v) -> v.getValue() - c.getValue());
            if (maxCard1.isPresent()) {
                Optional<CardEntity> maxCard2 = h2.get_cards().stream().max((c, v) -> v.getValue() - c.getValue());
                if (maxCard2.isPresent()) {
                    int a = maxCard1.get().getValue();
                    int b = maxCard2.get().getValue();
                    if (a > b) {
                        return 1;
                    } else if (b > a){
                        return -1;
                    } else {
                        Map<String, Integer> suitRank = new HashMap();
                        suitRank.put("S", 4);
                        suitRank.put("H", 3);
                        suitRank.put("C", 2);
                        suitRank.put("D", 1);

                        int c = suitRank.get(maxCard1.get().getSuit());
                        int d = suitRank.get(maxCard2.get().getSuit());

                        if (c > d) {
                            return 1;
                        }
                        else if (d > c) {
                            return -1;
                        }
                        return 0;
                    }
                } else {
                    return 1;
                }
            } else {
                return -1;
            }
        }
    }
    public double getRanking(HandEntity mHand) {
        double ranking = 0;
        if (isRoyalFlush(mHand) > 9.0) {
            ranking = isRoyalFlush(mHand);
        } else if (isStraightFlush(mHand) > 8.0) {
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

    private double isRoyalFlush(HandEntity mHand) {
        OptionalInt optionalInt = mHand.get_cards().stream().filter(c -> c.getValue() > 1).mapToInt(CardEntity::getValue).min();

        if (optionalInt.isPresent()) {
            int minValueCard = optionalInt.getAsInt();
            if (minValueCard == 10 && isStraightFlush(mHand) > 8.0) {
                return 1 + isStraightFlush(mHand);
            }
        }
        return 0;
    }
}
