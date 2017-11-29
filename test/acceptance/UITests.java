package acceptance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.junit.Assert;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class UITests {
    public static void main(String[] args) throws MalformedURLException {
        WebDriver driver = new FirefoxDriver();
        driver.navigate().to("http://localhost:8081/");
        driver.findElement(By.className("host-game")).click();
        driver.findElement(By.className("host-dialog")).findElement(By.tagName("input")).sendKeys("2");

    }
}
