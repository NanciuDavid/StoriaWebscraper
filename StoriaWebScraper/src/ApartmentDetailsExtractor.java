import java.time.Duration;
import java.util.HashMap;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.List;

public class ApartmentDetailsExtractor {
    
    public static HashMap<String, String> extractApartmentDetails(WebDriver driver) {
        HashMap<String, String> details = new HashMap<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));


        details.put("url", driver.getCurrentUrl());

        // Extract description
        try {
            WebElement descriptionElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//h4[contains(@class,'css-momjvf')]"))
            );
            details.put("Description", descriptionElement.getText().trim());
        } catch (Exception e) {
            System.err.println("Could not extract description: " + e.getMessage());
            details.put("Description", null);
        }   

        // Extract price
        try {
            WebElement priceElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//strong[@aria-label='Preț']"))
            );
            details.put("Price", priceElement.getText().trim());
        } catch (Exception e) {
            System.err.println("Could not extract price: " + e.getMessage());
            details.put("Price", null);
        }

        // Extract address
        try {
            WebElement addressElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@class,'css-1jjm9oe')]"))
            );
            details.put("Address", addressElement.getText().trim());
        } catch (Exception e) {
            System.err.println("Could not extract address: " + e.getMessage());
            details.put("Address", null);
        }

        // Extract surface area
        try {
            WebElement surfaceWebElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'css-zej2ui')]/div[@class='css-1ftqasz']"))
            );
            details.put("Surface", surfaceWebElement.getText().trim());
        } catch (Exception e) {
            System.err.println("Could not extract surface area: " + e.getMessage());
            details.put("Surface", null);
        }

        // Extract number of rooms
        try {   
            WebElement roomsElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("(//button[contains(@class, 'css-zej2ui')])[2]/div[@class='css-1ftqasz']"))
            );
            details.put("Rooms", roomsElement.getText().trim());
        } catch (Exception e) {
            System.err.println("Could not extract number of rooms: " + e.getMessage());
            details.put("Rooms", null);
        }

        // Extract specifications
        try {
            List<WebElement> specElements = driver.findElements(By.cssSelector(".css-t7cajz.e15n0fyo1"));
        
            for(WebElement specElement : specElements) {
                try {
                    String key = specElement.findElement(By.xpath(".//p[1]")).getText().trim();
                    String value = specElement.findElement(By.xpath(".//p[2]")).getText().trim();

                    if(value.equals("fără informații")) {
                        details.put(key, "fara informatii");
                        continue;
                    }

                    details.put(key, value);
                } catch (Exception e) {
                    System.err.println("Could not extract specifications: " + e.getMessage());
                }
            }
        } catch (Exception e) {
                System.err.println("Could not locate specifications: " + e.getMessage());
        }

        return details;
    }
}
