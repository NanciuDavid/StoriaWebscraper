import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AccesApartment {
    public static boolean clickWithRetry(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        //trying to access the apartment
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
                wait.until(ExpectedConditions.elementToBeClickable(element)).click();
                return true;
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                System.out.println("Retrying due to exception: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
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
