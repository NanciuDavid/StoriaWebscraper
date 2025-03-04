import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Utility class to handle cookie consent pop-ups during web scraping.
 */
public class AcceptCookies {

    /**
     * Accepts cookies if the cookie consent button is present on the page.
     * Uses WebDriverWait to handle dynamic content and click the consent button.
     * @param driver WebDriver instance
     */
    
    public static void acceptCookies(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.id("onetrust-accept-btn-handler"))).click();
        } catch (Exception e) {
            // Silent failure if the cookie button is not found
        }
    }
}
