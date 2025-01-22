import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import io.github.cdimascio.dotenv.Dotenv;

public class ApartmentDetailsExtractor {

    public static HashMap<String, String> extractApartmentDetails(WebDriver driver) {
        HashMap<String, String> details = new HashMap<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebDriverWait waitForSurvey = new WebDriverWait(driver, Duration.ofSeconds(2));
        Dotenv dotenv = Dotenv.configure().load();
        String apiKey = dotenv.get("GOOGLE_API");
        GeoApiContext geoContext = new GeoApiContext.Builder()
        .apiKey(apiKey)
        .build();
    
        // Close survey popup if present

        try {
            WebElement surveyContainer = waitForSurvey.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".smcx-modal-close")));
            if(surveyContainer.isDisplayed()) {
            WebElement surveyCloseButton = waitForSurvey.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[5]/div[1]/div[2]")));
            surveyCloseButton.click();
                System.out.println("Survey popup closed.");
            }   
        } catch (Exception e) {
            System.out.println("[INFO] No survey popup present: " + e.getMessage());
        }

         // Add the current URL
        details.put("url", driver.getCurrentUrl());

        // Add the current extraction time
        details.put("Extraction Date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Extract header announcement
        try {
            WebElement headerElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[@class='css-wqvm7k ef3kcx01']"))
            );
            details.put("Header", headerElement.getText().trim());
        } catch (Exception e) {
            System.err.println("[ERROR] Could not extract header: " + e.getMessage());
            details.put("Header", "N/A");
        }

        // Extract price
        try {
            WebElement priceElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".css-1o51x5a"))
            );
            details.put("Price", priceElement.getText().trim().replaceAll("[^0-9.,]", ""));
        } catch (Exception e) {
            System.err.println("[ERROR] Could not extract price: " + e.getMessage());
            details.put("Price", "N/A");
        }

        // Extract price per square meter
        try {
            WebElement pricePerSqMElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".css-z3xj2a"))
            );
            details.put("Price Per Square Meter", pricePerSqMElement.getText().trim().replaceAll("[^0-9.,]", ""));
        } catch (Exception e) {
            System.err.println("[ERROR] Could not extract price per square meter: " + e.getMessage());
            details.put("Price Per Square Meter", "N/A");
        }

        // Extract address
        // Extract address
        try {
            WebElement addressElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".css-1jjm9oe")));
            String address = addressElement.getText().trim();
            details.put("Address", address);

            // Fetch latitude and longitude
            LatLng coordinates = getCoordinates(geoContext, address);
            if (coordinates != null) {
                details.put("Latitude", String.valueOf(coordinates.lat));
                details.put("Longitude", String.valueOf(coordinates.lng));
            } else {
                details.put("Latitude", "N/A");
                details.put("Longitude", "N/A");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Could not extract address or coordinates: " + e.getMessage());
            details.put("Address", "N/A");
            details.put("Latitude", "N/A");
            details.put("Longitude", "N/A");
        }

        // Extract surface area
        try {
            WebElement surfaceElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("#__next > div.css-n9t2oe.e1b97qw40 > main > div.css-11zs7dp.edhb3g30 > div.css-1w41ge1.edhb3g31 > div.css-1xbf5wd.e15n0fyo0 > div.css-58w8b7.eezlw8k0 > button:nth-child(1)"))
            );
            details.put("Surface", surfaceElement.getText().trim().replaceAll("[^0-9.,]", ""));
        } catch (Exception e) {
            System.err.println("[ERROR] Could not extract surface area: " + e.getMessage());
            details.put("Surface", "N/A");
        }

        // Extract number of rooms
        try {
            WebElement roomsElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("#__next > div.css-n9t2oe.e1b97qw40 > main > div.css-11zs7dp.edhb3g30 > div.css-1w41ge1.edhb3g31 > div.css-1xbf5wd.e15n0fyo0 > div.css-58w8b7.eezlw8k0 > button:nth-child(2)"))
            );
            details.put("Rooms", roomsElement.getText().trim().replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            System.err.println("[ERROR] Could not extract number of rooms: " + e.getMessage());
            details.put("Rooms", "N/A");
        }

        // Extract additional information
        try {
            List<WebElement> additionalInformationElements = driver.findElements(By.cssSelector(".css-t7cajz.e15n0fyo1"));

            for (WebElement features : additionalInformationElements) {
                try {
                    String key = features.findElement(By.xpath(".//p[1]")).getText().trim();
                    // System.out.println(key);
                    String value = features.findElement(By.xpath(".//p[2]")).getText().trim();

                    if (value.equals("fără informații")) {
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

        try {
            WebElement buildingAndMaterialContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.css-v8q1bf:nth-child(1) > header:nth-child(1)")));
            buildingAndMaterialContainer.click();
            // Extract year of construction
            try {
                WebElement constructionYear = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".css-gfsn8h > div:nth-child(1) > div:nth-child(1) > p:nth-child(2)")));
                //System.out.println("Anul construcției: " + constructionYear.getText().trim());
                details.put("Anul construcției:", constructionYear.getText().trim());
            } catch (Exception e) {
                System.err.println("[ERROR] Could not extract year of construction: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Could not find building details container");
        }

        // for (String key : details.keySet()) {
        //     System.out.println(key + ": " + details.get(key));
        // }

        return formatDetails(details);

    }

        private static LatLng getCoordinates(GeoApiContext geoContext, String address) {
        try {
            GeocodingResult[] results = GeocodingApi.geocode(geoContext, address).await();
            if (results != null && results.length > 0) {
                return results[0].geometry.location;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Geocoding failed: " + e.getMessage());
        }
        return null;
    }



    private static HashMap<String, String> formatDetails(HashMap<String, String> details) {
        HashMap<String, String> formattedDetails = new LinkedHashMap<>();
        
        // Add predefined keys to ensure consistency
        formattedDetails.put("Header", details.getOrDefault("Header", "N/A"));
        formattedDetails.put("Price", details.getOrDefault("Price", "N/A"));
        formattedDetails.put("Price Per Square Meter", details.getOrDefault("Price Per Square Meter", "N/A"));
        formattedDetails.put("Surface", details.getOrDefault("Surface", "N/A"));
        formattedDetails.put("Rooms", details.getOrDefault("Rooms", "N/A"));
        formattedDetails.put("Address", details.getOrDefault("Address", "N/A"));
        formattedDetails.put("Latitude", details.getOrDefault("Latitude", "N/A"));
        formattedDetails.put("Longitude", details.getOrDefault("Longitude", "N/A"));
        formattedDetails.put("Year of Construction", details.getOrDefault("Anul construcției:", "N/A"));
        formattedDetails.put("URL", details.getOrDefault("url", "N/A"));
        formattedDetails.put("Extraction Date", details.getOrDefault("Extraction Date", "N/A"));
    
        // Add additional information dynamically
        for (String key : details.keySet()) {
            if (!formattedDetails.containsKey(key)) {
                formattedDetails.put(key, details.get(key)); // Add only keys not already in formattedDetails
            }
        }
    
        return formattedDetails;
    }

}

