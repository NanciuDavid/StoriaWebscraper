import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AcceptCookies {


    //Accepting the cookies pop-up that appears every time the site is accessed so that every to avoid the pop-up obstructing the apartment
    //the pop-up is found by its ID  "onetrust-accept-btn-handler" :
    public static void acceptCookies(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.id("onetrust-accept-btn-handler"))).click();
            //if the pop - up was succesfully clicked, the following message will be printed :
            System.out.println("Cookies have been accepted");
        } catch (Exception e) {
            //otherwise
            System.err.println("Cookies acceptance button not found: " + e.getMessage());
        }
    }
}


