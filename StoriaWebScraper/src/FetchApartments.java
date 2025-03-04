import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for fetching apartment listings from the webpage.
 */
public class FetchApartments {

    /**
     * Fetches the list of apartment elements from the page.
     * Uses WebDriverWait to ensure all elements are loaded before returning them.
     * @param driver WebDriver instance
     * @return List of WebElement representing apartments
     */
    
    public static List<WebElement> fetchApartments(WebDriver driver) {
        List<WebElement> list = new ArrayList<>();
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            list = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".css-1i43dhb > div:nth-child(3) > ul:nth-child(2) li")));
        } catch (Exception e) {
            // Silent failure, return empty list if apartments cannot be fetched
        }
        return list;
    }
}
