package unit.algo;

import org.junit.BeforeClass;
import org.junit.Test;
import server.model.entity.*;
import server.model.entity.algo.StrategyOne;
import server.model.entity.algo.StrategyTwo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class StrategyTwoTests {
    private static BotEntity b;

    @BeforeClass
    public static void setup () {
        b = new BotEntity();
        b.setAlgorithm(new StrategyTwo());
    }

    @Test
    public void usesFirstStratIfFirstPlayer () {
        HandEntity mHand = new HandEntity();

        BotEntity b1 = new BotEntity();
        b1.setAlgorithm(new StrategyOne());

        List<CardEntity> cards = new ArrayList();
        cards.add(new CardEntity("C", 1, "A"));
        cards.add(new CardEntity("D", 2, "2"));
        cards.add(new CardEntity("C", 3, "3"));
        cards.add(new CardEntity("C", 4, "4"));
        cards.add(new CardEntity("H", 5, "5"));
        mHand.set_cards(cards);

        RoomEntity mRoom = new RoomEntity();

        boolean shouldHold = b.getAlgorithm().shouldHold(mHand, mRoom, new ArrayList(), true);
        boolean shouldHold2 = b1.getAlgorithm().shouldHold(mHand, mRoom, new ArrayList(), true);

        assertEquals(shouldHold, shouldHold2);


    }

    @Test
    public void doubleElseCondition () {
        HandEntity mHand = new HandEntity();

        BotEntity b1 = new BotEntity();
        b1.setAlgorithm(new StrategyOne());

        RoomEntity mRoom = new RoomEntity();

        List<CardEntity> cards = new ArrayList();
        cards.add(new CardEntity("C", 1, "A"));
        cards.add(new CardEntity("D", 2, "2"));
        cards.add(new CardEntity("C", 3, "3"));
        cards.add(new CardEntity("C", 4, "4"));
        cards.add(new CardEntity("H", 5, "5"));
        mHand.set_cards(cards);


        boolean shouldHold = b.getAlgorithm().shouldHold(mHand, mRoom, new ArrayList(), false);
        assertTrue(shouldHold);
    }

    @Test
    public void visibleCardModifierSituation () {
        HandEntity mHand = new HandEntity();

        BotEntity b1 = new BotEntity();
        b1.setAlgorithm(new StrategyOne());

        RoomEntity mRoom = new RoomEntity();
        PlayerEntity p1 = new BotEntity();

        List<CardEntity> cards2 = new ArrayList();
        cards2.add(new CardEntity("C", 1, "A"));
        cards2.add(new CardEntity("D", 2, "2"));
        cards2.add(new CardEntity("C", 3, "3"));
        cards2.add(new CardEntity("C", 4, "4"));
        cards2.add(new CardEntity("H", 5, "5"));
        cards2.forEach(c -> c.setPubliclyVisible(true));
        p1.getHand().set_cards(cards2);

        List<HandEntity> otherHands = new ArrayList();

        otherHands.add(p1.getHand());

        List<CardEntity> cards = new ArrayList();

        cards.add(new CardEntity("C", 1, "A"));
        cards.add(new CardEntity("D", 2, "2"));
        cards.add(new CardEntity("C", 3, "3"));
        cards.add(new CardEntity("D", 4, "4"));
        cards.add(new CardEntity("H", 5, "5"));

        List<CardEntity> cardClone = cards.stream().collect(Collectors.toList());

        mHand.set_cards(cards);

        boolean shouldHold = b.getAlgorithm().shouldHold(mHand, mRoom, otherHands, false);
        assertTrue(shouldHold);
        // changes all cards
        for (int i = 0; i < cards.size(); i++) {
            assertNotEquals(cardClone.get(i), cards.get(i));
        }
    }
}
