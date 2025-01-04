import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class FetchApartments {

    public static List<WebElement> fetchApartments(WebDriver driver) {
        List<WebElement> list = new ArrayList<>();

        try {
            // Wait until the organic listings are present
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            list = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".css-1i43dhb > div:nth-child(3) > ul:nth-child(2) li")));
            return list;

        } catch (Exception e) {
            System.err.println("Error fetching apartments: " + e.getMessage());
        }

        return list;
    }
}