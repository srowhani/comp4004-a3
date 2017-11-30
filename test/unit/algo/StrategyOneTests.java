package unit.algo;

import org.junit.BeforeClass;
import org.junit.Test;
import server.model.entity.BotEntity;
import server.model.entity.CardEntity;
import server.model.entity.HandEntity;
import server.model.entity.RoomEntity;
import server.model.entity.algo.StrategyOne;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class StrategyOneTests {
    private static BotEntity b;
    @BeforeClass
    public static void setup () {
        b = new BotEntity();
        b.setAlgorithm(new StrategyOne());
    }

    @Test
    public void hasStraightOrBetterHold () {
        HandEntity mHand = new HandEntity();
        List<CardEntity> cards = new ArrayList();
        cards.add(new CardEntity("C", 1, "A"));
        cards.add(new CardEntity("D", 2, "2"));
        cards.add(new CardEntity("C", 3, "3"));
        cards.add(new CardEntity("C", 4, "4"));
        cards.add(new CardEntity("H", 5, "5"));
        mHand.set_cards(cards);

        RoomEntity mRoom = new RoomEntity();

        boolean shouldHold = b.getAlgorithm().shouldHold(mHand, mRoom, new ArrayList(), false);
        assertTrue(shouldHold);

        HandEntity mHand2 = new HandEntity();
        List<CardEntity> cards2 = new ArrayList();
        cards2.add(new CardEntity("D", 1, "A"));
        cards2.add(new CardEntity("D", 2, "2"));
        cards2.add(new CardEntity("D", 3, "3"));
        cards2.add(new CardEntity("D", 4, "4"));
        cards2.add(new CardEntity("D", 5, "5"));
        mHand2.set_cards(cards2);

        RoomEntity mRoom2 = new RoomEntity();

        boolean shouldHold2 = b.getAlgorithm().shouldHold(mHand2, mRoom2, new ArrayList(), false);
        assertTrue(shouldHold2);
    }

    @Test
    public void triesForFullHouseFoldsIfNot () {
        HandEntity mHand = new HandEntity();
        List<CardEntity> cards = new ArrayList();
        CardEntity card4, card5;
        cards.add(new CardEntity("C", 1, "A"));
        cards.add(new CardEntity("D", 1, "A"));
        cards.add(new CardEntity("S", 1, "A"));
        cards.add(card4 = new CardEntity("C", 4, "4"));
        cards.add(card5 = new CardEntity("H", 5, "5"));
        mHand.set_cards(cards);

        RoomEntity mRoom = new RoomEntity();

        boolean shouldHold = b.getAlgorithm().shouldHold(mHand, mRoom, new ArrayList(), false);
        assertFalse(shouldHold); // folds if not

        assertNotEquals(card4, cards.get(3));
        assertNotEquals(card5, cards.get(4));

        cards.set(4, card4); // is full house now;
        cards.set(3, card4);

        shouldHold = b.getAlgorithm().shouldHold(mHand, mRoom, new ArrayList(), false);
        assertTrue(shouldHold);
    }
}
