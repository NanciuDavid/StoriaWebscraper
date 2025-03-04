import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Utility class for handling click actions with retries.
 * This helps avoid issues with stale elements and intercepted clicks.
 */
public class AccesApartment {
    
    /**
     * Attempts to click an element multiple times with retries if necessary.
     * This method handles stale elements and intercepted clicks by retrying the click action.
     * @param driver WebDriver instance
     * @param locator By locator of the element to click
     * @return true if click is successful, false otherwise
     */
    public static boolean clickWithRetry(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
                wait.until(ExpectedConditions.elementToBeClickable(element)).click();
                return true;
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                // Retry silently if element is stale or intercepted
            } catch (Exception e) {
                // Log unexpected errors if necessary
            }
            
            // Wait before retrying
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }
}
