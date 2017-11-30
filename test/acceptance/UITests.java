package acceptance;

import org.junit.*;
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

public class UITests {
    private static Thread serverThread;
    private static WebDriver driver;
    private static final String URL = "http://localhost:8081/";

    @BeforeClass
    public static void setupClass () {
        driver = new FirefoxDriver();
        try {
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() throws InterruptedException {
        serverThread = new Thread(new ServerThread());
        serverThread.start();
        sleep(2000);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @After
    public void cleanup() {
        serverThread.interrupt();
        serverThread = null;
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
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        assertEquals(driver.findElement(By.cssSelector(".game-winner-0 .user-id")).getText(), botNameTypeTwo);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-0 .user-rank")).getText()) > 7);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-1 .user-id")).getText(), botNameTypeOne);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-1 .user-rank")).getText()) > 4);

        assertEquals(driver.findElement(By.cssSelector(".game-winner-2 .user-id")).getText(), hostName);
        assertTrue(Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-2 .user-rank")).getText()) < Double.parseDouble(driver.findElement(By.cssSelector(".game-winner-0 .user-rank")).getText()));
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
