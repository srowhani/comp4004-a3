package acceptance;

import org.junit.*;
import org.junit.runner.JUnitCore;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import server.ServerThread;


import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UITests {
    private static Thread serverThread;
    private static WebDriver driver;
    private static final String URL = "http://localhost:8081/";

    @BeforeClass
    public static void setupClass () {
        driver = new FirefoxDriver();
        try {
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            serverThread = new Thread(new ServerThread());
            serverThread.start();
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void cleanupClass() {
        driver.close();
    }

    @Test
    public void createRoomTest () {
        createRoom(2);
    }

    private void createRoom (int roomSize) {
        loadSite();
        int currentRoomCount = getNumberOfRooms();
        driver.findElement(By.className("host-game")).click();
        driver.findElement(By.className("host-dialog")).findElement(By.tagName("input")).clear();

        driver.findElement(By.className("host-dialog")).findElement(By.tagName("input")).sendKeys(roomSize + "");
        driver.findElement(By.tagName("button")).click();
        int roomCountAfterCreate = getNumberOfRooms();
        assertEquals(currentRoomCount + 1, roomCountAfterCreate);
    }
    @Test
    public void addAIPlayer () {
        createRoom(2);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        WebElement input = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
        input.clear();
        input.sendKeys("type_one");
        int before = logs("has joined room!").size();
        driver.findElement(By.className("md-icon-button")).click();
        int after = logs("has joined room!").size();
        assertEquals(before + 1, after);
    }

    @Test
    public void AITypeOneWinsRoundWithStraightFlush () {
        createRoom(2);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
        i.sendKeys("type_one");

        WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
        input.sendKeys("AH 2S 3C 4D 5H");
        driver.findElement(By.className("md-icon-button")).click();

        String botName = logs("has joined room!").get(0).getText().replace(" has joined room!", "");
        String hostName = logs("has joined room!").get(1).getText().replace(" has joined room!", "");

        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        startGame();
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        driver.findElement(By.className("button_hold")).click();
        assertEquals(driver.findElement(By.cssSelector(".game-winner-0 .user-id")).getText(), botName);
        assertEquals(driver.findElement(By.cssSelector(".game-winner-0 .user-rank")).getText(), "4.05");

        assertEquals(driver.findElement(By.cssSelector(".game-winner-1 .user-id")).getText(), hostName);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-1 .user-rank")).getText()) < Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-0 .user-rank")).getText()));
    }

    @Test
    public void AITypeTwoWinsRoundPlaysAsFirstPlayer () {
        createRoom(2);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
        i.sendKeys("type_two");

        WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
        input.sendKeys("AH 2S 3C 4D 5H");
        driver.findElement(By.className("md-icon-button")).click();

        String botName = logs("has joined room!").get(0).getText().replace(" has joined room!", "");
        String hostName = logs("has joined room!").get(1).getText().replace(" has joined room!", "");

        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        startGame();

        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        driver.findElement(By.className("button_hold")).click();

        assertEquals(driver.findElement(By.cssSelector(".game-winner-0 .user-id")).getText(), botName);
        assertEquals(driver.findElement(By.cssSelector(".game-winner-0 .user-rank")).getText(), "4.05");

        assertEquals(driver.findElement(By.cssSelector(".game-winner-1 .user-id")).getText(), hostName);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-1 .user-rank")).getText()) < Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-0 .user-rank")).getText()));
    }

    @Test
    public void AITypeTwoWinsRoundPlaysAsSecondPlayer() throws InterruptedException {
        createRoom(3);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
        i.sendKeys("type_one");

        WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
        input.sendKeys("AH 2H 3H 4D 5H");
        driver.findElement(By.className("md-icon-button")).click();

        WebElement i2 = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
        i2.clear();
        i2.sendKeys("type_two");

        WebElement input2 = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
        input2.clear();
        input2.sendKeys("AS AD AC AH 5D");

        driver.findElement(By.className("md-icon-button")).click();
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        startGame();

        String botNameTypeTwo = logs("has joined room!").get(0).getText().replace(" has joined room!", "");
        String botNameTypeOne = logs("has joined room!").get(1).getText().replace(" has joined room!", "");
        String hostName = logs("has joined room!").get(2).getText().replace(" has joined room!", "");

        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);


        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        driver.findElement(By.className("button_hold")).click();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        assertEquals(driver.findElement(By.cssSelector(".game-winner-0 .user-id")).getText(), botNameTypeTwo);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-0 .user-rank")).getText()) > 7);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-1 .user-id")).getText(), botNameTypeOne);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-1 .user-rank")).getText()) > 4);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-2 .user-id")).getText(), hostName);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-2 .user-rank")).getText()) < Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-0 .user-rank")).getText()));
    }

    @Test
    public void fourPlayerStratOneScenarioTwo () {
        createRoom(4);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        // First player joins with straight, should hold
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_one");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("AH AS KH KS 5H");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }
        // Second player joins
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_one");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("5S 4C 5D 4H 9C");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }

        // Third player joins
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_one");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("10D 10C 10S 2D 3H");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }

        {
            WebElement i = driver.findElement(By.className("my_starting_cards")).findElement(By.tagName("input"));;
            i.clear();
            i.sendKeys("2H 4C 8S 9D 5C");
            driver.findElement(By.className("set_start_cards")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }
        /**
         * Queue up some cards
         */
        {
            driver.findElement(By.className("x-toggle-btn")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

            WebElement removeCardInput = driver.findElement(By.className("remove_cards_from_deck")).findElement(By.tagName("input"));
            removeCardInput.clear();
            removeCardInput.sendKeys("QS QH 10D KD");

            driver.findElement(By.className("fire_remove_cards_from_deck")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        }
        startGame();
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        {
            driver.findElement(By.className("button_hold")).click();
        }

        String hostName = logs("has joined room!").get(3).getText().replace(" has joined room!", "");
        String twoPairToFullHouse = logs("has joined room!").get(2).getText().replace(" has joined room!", "");
        String twoPairToNothing = logs("has joined room!").get(1).getText().replace(" has joined room!", "");
        String threeOfKindToFullHouse = logs("has joined room!").get(0).getText().replace(" has joined room!", "");

        assertEquals(driver.findElement(By.cssSelector(".game-winner-0 .user-id")).getText(), twoPairToFullHouse);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-0 .user-rank")).getText()) > 6);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-1 .user-id")).getText(), threeOfKindToFullHouse);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-1 .user-rank")).getText()) > 6);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-2 .user-id")).getText(), twoPairToNothing);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-2 .user-rank")).getText()) > 2);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-3 .user-id")).getText(), hostName);
    }
    @Test
    public void fourPlayerStratOneScenarioOne () {
        createRoom(4);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        // First player joins with straight, should hold
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_one");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("AH 2H 3H 4H 5H");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }
        // Second player joins
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_one");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("AD AS AC 4H 5D");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

            WebElement removeCardInput = driver.findElement(By.className("remove_cards_from_deck")).findElement(By.tagName("input"));
            removeCardInput.clear();
            removeCardInput.sendKeys("AH AC AD AS 4H 4S 4D 4C 5C 5S 5D 5H");

            driver.findElement(By.className("fire_remove_cards_from_deck")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }

        // Third player joins
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_one");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("KS KD 7C 9S 10H");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

            WebElement removeCardInput = driver.findElement(By.className("remove_cards_from_deck")).findElement(By.tagName("input"));
            removeCardInput.clear();
            removeCardInput.sendKeys("KH KC KD KS");

            driver.findElement(By.className("fire_remove_cards_from_deck")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }

        {
            WebElement i = driver.findElement(By.className("my_starting_cards")).findElement(By.tagName("input"));;
            i.clear();
            i.sendKeys("2H 4C 8S 9D 5C");
            driver.findElement(By.className("set_start_cards")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }

        startGame();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        driver.findElement(By.className("button_hold")).click();

        String hostName = logs("has joined room!").get(3).getText().replace(" has joined room!", "");
        String straightBot = logs("has joined room!").get(2).getText().replace(" has joined room!", "");
        String threeOfKindBot = logs("has joined room!").get(1).getText().replace(" has joined room!", "");
        String pairBot = logs("has joined room!").get(0).getText().replace(" has joined room!", "");

        assertEquals(driver.findElement(By.cssSelector(".game-winner-0 .user-id")).getText(), straightBot);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-0 .user-rank")).getText()) > 4);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-1 .user-id")).getText(), threeOfKindBot);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-1 .user-rank")).getText()) > 3);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-2 .user-id")).getText(), pairBot);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-2 .user-rank")).getText()) > 1);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-3 .user-id")).getText(), hostName);
    }

    @Test
    public void fourPlayerStratTwoScenarioOne () {
        createRoom(4);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        /**
         * First person playing should follow scenario one. Meaning he will swap out anything
         * not a pair of three of a kind trying to improve.
         *
         * Starts with a pair
         * He will get nothing.
         */
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_two");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("AH AS 3C 8D 9S");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }
        /**
         * player 2: has a pair: reuses Strategy 1, exchanges 3 others,
         * gets full house
         * (so the 3 visible cards ARE of the same kind)
         */
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_two");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("QH QS 2D 5S 8C");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }

        /**
         * player 3: has two pairs: exchanges 1 card,
         * gets a full house that beats the
         * one of player 2
         */
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_two");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("AH AS KD KS 8C");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }
        /**
         * My cards
         */
        {
            WebElement i = driver.findElement(By.className("my_starting_cards")).findElement(By.tagName("input"));;
            i.clear();
            i.sendKeys("2H 4C 8S 9D 5C");
            driver.findElement(By.className("set_start_cards")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }
        /**
         * Queue up some cards
         */
        {
            driver.findElement(By.className("x-toggle-btn")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

            WebElement queueCardInput = driver.findElement(By.className("remove_cards_from_deck")).findElement(By.tagName("input"));
            queueCardInput.clear();

            // FIFO
            queueCardInput.sendKeys("AC JD JS JH 10S 9D 4C");

            driver.findElement(By.className("fire_remove_cards_from_deck")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }

        startGame();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        driver.findElement(By.className("button_hold")).click();

        String hostName = logs("has joined room!").get(3).getText().replace(" has joined room!", "");
        String playerOneGetsNothing = logs("has joined room!").get(2).getText().replace(" has joined room!", "");
        String getsOkayFullHouse = logs("has joined room!").get(1).getText().replace(" has joined room!", "");
        String getsGoodFullHouse = logs("has joined room!").get(0).getText().replace(" has joined room!", "");

        assertEquals(driver.findElement(By.cssSelector(".game-winner-0 .user-id")).getText(), getsGoodFullHouse);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-1 .user-id")).getText(), getsOkayFullHouse);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-2 .user-id")).getText(), playerOneGetsNothing);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-3 .user-id")).getText(), hostName);
    }

    @Test
    public void fourPlayerStratTwoScenarioTwo () {
        createRoom(4);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        /**
         * player 1: has nothing: reuses Strategy 1, exchanges 5 cards, gets full
         house
         */
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_two");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("2S 5S 8C 9D 7H");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }
        /**
         * player 2: has nothing: exchanges 5 cards, gets a royal flush
         (so the 3 visible cards ARE NOT of the same kind)
         */
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_two");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("3S 4S 9C 2D 7S");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }

        /**
         player 3: has one pair: exchanges 3 cards, gets nothing
         */
        {
            WebElement i = driver.findElement(By.className("ai_type")).findElement(By.tagName("input"));
            i.clear();
            i.sendKeys("type_two");

            WebElement input = driver.findElement(By.className("start_cards")).findElement(By.tagName("input"));
            input.clear();
            input.sendKeys("6S 6C 5D QD JS");
            driver.findElement(By.className("md-icon-button")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }

        /**
         * My cards
         */
        {
            WebElement i = driver.findElement(By.className("my_starting_cards")).findElement(By.tagName("input"));;
            i.clear();
            i.sendKeys("2H 4C 8S 9D 5C");
            driver.findElement(By.className("set_start_cards")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }

        /**
         * Queue up some cards
         */
        {
            driver.findElement(By.className("x-toggle-btn")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

            WebElement queueCardInput = driver.findElement(By.className("remove_cards_from_deck")).findElement(By.tagName("input"));
            queueCardInput.clear();

            // FIFO
            queueCardInput.sendKeys("9D KD AD 10H JH QH KH AH 9C 9S 8D 8C 8S");

            driver.findElement(By.className("fire_remove_cards_from_deck")).click();
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }

        startGame();
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        driver.findElement(By.className("button_hold")).click();
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        String nothing = logs("has joined room!").get(3).getText().replace(" has joined room!", "");
        String fullHouse = logs("has joined room!").get(2).getText().replace(" has joined room!", "");
        String royalFlush = logs("has joined room!").get(1).getText().replace(" has joined room!", "");
        String onePair = logs("has joined room!").get(0).getText().replace(" has joined room!", "");

        assertEquals(driver.findElement(By.cssSelector(".game-winner-0 .user-id")).getText(), royalFlush);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-1 .user-id")).getText(), fullHouse);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-2 .user-id")).getText(), onePair);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-3 .user-id")).getText(), nothing);
    }

    private void startGame() {
        int numStarted = logs("Attempting game start").size();
        driver.findElement(By.className("start_game")).click();
        int numAfter = logs("Attempting game start").size();
        assertEquals(numStarted + 1, numAfter);
    }

    private void loadSite () {
        driver.navigate().to(URL);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
    }

    private int getNumberOfRooms () {
        return driver.findElement(By.className("room-list")).findElements(By.cssSelector("md-list-item")).size();
    }

    private List<WebElement> logs (String text) {
        return driver.findElement(By.className("logs"))
            .findElements(By.cssSelector("md-list-item"))
            .stream().filter(item -> item.getText().contains(text))
            .collect(Collectors.toList());
    }
}
