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

    /**
     * Extracts detailed information about an apartment from the current page.
     * Utilizes various helper methods to gather data such as price, address, and additional features.
     * @param driver WebDriver instance
     * @return HashMap containing apartment details
     */

    public static HashMap<String, String> extractApartmentDetails(WebDriver driver) {
        HashMap<String, String> details = new HashMap<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebDriverWait waitForSurvey = new WebDriverWait(driver, Duration.ofSeconds(2));
        GeoApiContext geoContext = initializeGeoApiContext();

        closeSurveyPopup(driver, waitForSurvey);
        extractBasicDetails(driver, details);
        extractHeader(driver, wait, details);
        extractPrice(driver, wait, details);
        extractPricePerSquareMeter(driver, wait, details);
        extractAddressAndCoordinates(driver, wait, geoContext, details);
        extractSurface(driver, wait, details);
        extractRooms(driver, wait, details);
        extractAdditionalInformation(driver, details);

        return formatDetails(details);
    }

    /**
     * Initializes the GeoApiContext for geocoding operations.
     * Loads the API key from environment variables.
     * @return GeoApiContext instance
     */

    private static GeoApiContext initializeGeoApiContext() {
        Dotenv dotenv = Dotenv.configure().load();
        String apiKey = dotenv.get("GOOGLE_API");
        return new GeoApiContext.Builder().apiKey(apiKey).build();
    }

    /**
     * Closes any survey pop-ups that may appear on the page.
     * Uses WebDriverWait to handle dynamic content.
     * @param driver WebDriver instance
     * @param waitForSurvey WebDriverWait instance for short waits
     */
    
    private static void closeSurveyPopup(WebDriver driver, WebDriverWait waitForSurvey) {
        try {
            WebElement surveyContainer = waitForSurvey.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".smcx-modal-close")));
            WebElement surveyFilter = waitForSurvey.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".laq-layout--welcome")));
            if (surveyContainer.isDisplayed() || surveyFilter.isDisplayed()) {
                WebElement surveyCloseButton = waitForSurvey.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[5]/div[1]/div[2]")));
                surveyCloseButton.click();
                WebElement surveyFilterButton = waitForSurvey.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"laq-next-eXkFTQJyzs9L\"]")));
                surveyFilterButton.click();
            }
        } catch (Exception e) {
          //  System.out.println("[INFO] No survey popup present: " + e.getMessage());
        }
    }

    /**
     * Extracts basic details such as URL and extraction date.
     * Adds these details to the provided HashMap.
     * @param driver WebDriver instance
     * @param details HashMap to store extracted details
     */

    private static void extractBasicDetails(WebDriver driver, HashMap<String, String> details) {
        details.put("url", driver.getCurrentUrl());
       // System.out.println("[INFO] Extracted URL: " + details.get("url"));

        details.put("Extraction Date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
       // System.out.println("[INFO] Extracted Extraction Date: " + details.get("Extraction Date"));
    }

    /**
     * Extracts the header information of the apartment listing.
     * Handles exceptions and adds the header to the details HashMap.
     * @param driver WebDriver instance
     * @param wait WebDriverWait instance for waiting on elements
     * @param details HashMap to store extracted details
     */

    private static void extractHeader(WebDriver driver, WebDriverWait wait, HashMap<String, String> details) {
        try {
            WebElement headerElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".css-4utb9r.ednseph1"))
            );
            details.put("Header", headerElement.getText().trim());
           // System.out.println("[INFO] Extracted Header: " + details.get("Header"));
        } catch (Exception e) {
            System.err.println("[ERROR] Could not extract header: " + e.getMessage());
            details.put("Header", "N/A");
        }
    }

    /**
     * Extracts the price of the apartment.
     * Handles exceptions and adds the price to the details HashMap.
     * @param driver WebDriver instance
     * @param wait WebDriverWait instance for waiting on elements
     * @param details HashMap to store extracted details
     */

    private static void extractPrice(WebDriver driver, WebDriverWait wait, HashMap<String, String> details) {
        try {
            WebElement priceElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//strong[@aria-label='Preț']"))
            );
            details.put("Price", priceElement.getText().trim().replaceAll("[^0-9.,]", ""));
           // System.out.println("[INFO] Extracted Price: " + details.get("Price"));
        } catch (Exception e) {
            closeSurveyPopup(driver, wait);
            System.err.println("[ERROR] Could not extract price: " + e.getMessage());
            details.put("Price", "N/A");
        }
    }

    /**
     * Extracts the price per square meter of the apartment.
     * Handles exceptions and adds the price per square meter to the details HashMap.
     * @param driver WebDriver instance
     * @param wait WebDriverWait instance for waiting on elements
     * @param details HashMap to store extracted details
     */

    private static void extractPricePerSquareMeter(WebDriver driver, WebDriverWait wait, HashMap<String, String> details) {
        try {
            WebElement pricePerSqMElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@aria-label='Prețul pe metru pătrat']"))
            );
            details.put("Price Per Square Meter", pricePerSqMElement.getText().trim().replaceAll("[^0-9.,]", ""));
           // System.out.println("[INFO] Extracted Price Per Square Meter: " + details.get("Price Per Square Meter"));
        } catch (Exception e) {
            closeSurveyPopup(driver, wait);
            System.err.println("[ERROR] Could not extract price per square meter: " + e.getMessage());
            details.put("Price Per Square Meter", "N/A");
        }
    }

    /**
     * Extracts the address and geocodes it to obtain latitude and longitude.
     * Handles exceptions and adds the address and coordinates to the details HashMap.
     * @param driver WebDriver instance
     * @param wait WebDriverWait instance for waiting on elements
     * @param geoContext GeoApiContext for geocoding
     * @param details HashMap to store extracted details
     */

    private static void extractAddressAndCoordinates(WebDriver driver, WebDriverWait wait, GeoApiContext geoContext, HashMap<String, String> details) {
        try {
            WebElement addressElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@class='css-1jjm9oe e42rcgs1']")));
            String address = addressElement.getText().trim();
            details.put("Address", address);
            // System.out.println("[INFO] Extracted Address: " + details.get("Address"));

            LatLng coordinates = getCoordinates(geoContext, address);
            if (coordinates != null) {
                details.put("Latitude", String.valueOf(coordinates.lat));
                details.put("Longitude", String.valueOf(coordinates.lng));
                // System.out.println("[INFO] Extracted Latitude: " + details.get("Latitude"));
                // System.out.println("[INFO] Extracted Longitude: " + details.get("Longitude"));
            } else {
                details.put("Latitude", "N/A");
                details.put("Longitude", "N/A");
                // System.out.println("[INFO] Latitude and Longitude not found");
            }
        } catch (Exception e) {
            closeSurveyPopup(driver, wait);
            System.err.println("[ERROR] Could not extract address or coordinates: " + e.getMessage());
            details.put("Address", "N/A");
            details.put("Latitude", "N/A");
            details.put("Longitude", "N/A");
        }
    }

    /**
     * Extracts the surface area of the apartment.
     * Handles exceptions and adds the surface area to the details HashMap.
     * @param driver WebDriver instance
     * @param wait WebDriverWait instance for waiting on elements
     * @param details HashMap to store extracted details
     */

    private static void extractSurface(WebDriver driver, WebDriverWait wait, HashMap<String, String> details) {
        try {
            WebElement surfaceElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("button.eezlw8k1:nth-child(1) > div:nth-child(2)"))
            );
            details.put("Surface", surfaceElement.getText().trim().replaceAll("[^0-9.,]", ""));
            // System.out.println("[INFO] Extracted Surface: " + details.get("Surface"));
        } catch (Exception e) {
            closeSurveyPopup(driver, wait);
            System.err.println("[ERROR] Could not extract surface area: " + e.getMessage());
            details.put("Surface", "N/A");
        }
    }

    /**
     * Extracts the number of rooms in the apartment.
     * Handles exceptions and adds the number of rooms to the details HashMap.
     * @param driver WebDriver instance
     * @param wait WebDriverWait instance for waiting on elements
     * @param details HashMap to store extracted details
     */
    private static void extractRooms(WebDriver driver, WebDriverWait wait, HashMap<String, String> details) {
        try {
            WebElement roomsElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("button.eezlw8k1:nth-child(2) > div:nth-child(2)"))
            );
            details.put("Rooms", roomsElement.getText().trim().replaceAll("[^0-9]", ""));
            // System.out.println("[INFO] Extracted Rooms: " + details.get("Rooms"));
        } catch (Exception e) {
            closeSurveyPopup(driver, wait);
            System.err.println("[ERROR] Could not extract number of rooms: " + e.getMessage());
            details.put("Rooms", "N/A");
        }
    }

    /**
     * Extracts additional information about the apartment.
     * Handles exceptions and adds additional information to the details HashMap.
     * @param driver WebDriver instance
     * @param details HashMap to store extracted details
     */

    private static void extractAdditionalInformation(WebDriver driver, HashMap<String, String> details) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    
            // Locate only the divs with the class "css-1xw0jqp eows69w1"
            List<WebElement> additionalInformationElements = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".css-1xw0jqp.eows69w1"))
            );
    
            // System.out.println("[INFO] Extracted Additional Information: " + additionalInformationElements.size());
    
            for (WebElement element : additionalInformationElements) {
                try {
                    // Extract the key (label) and value from the current element
                    String key = element.findElement(By.cssSelector("p:nth-child(1)")).getText().trim();
                    String value = element.findElement(By.cssSelector("p:nth-child(2)")).getText().trim();
    
                    // Handle the case where the value is "fără informații"
                    if (value.equals("fără informații")) {
                        details.put(key, "fara informatii");
                    } else {
                        details.put(key, value);
                    }
    
                    // Print the extracted key-value pair for debugging
                    // System.out.println(key + ": " + value);
                } catch (Exception e) {
                    closeSurveyPopup(driver, wait);
                    System.err.println("Could not extract specifications: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Could not locate specifications: " + e.getMessage());
        }
    }

    /**
     * Geocodes the given address to obtain latitude and longitude.
     * Uses the GeoApiContext for geocoding operations.
     * @param geoContext GeoApiContext for geocoding
     * @param address Address to geocode
     * @return LatLng object containing latitude and longitude, or null if geocoding fails
     */

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

    /**
     * Formats the extracted details into a consistent structure.
     * Ensures all expected keys are present in the returned HashMap.
     * @param details HashMap containing raw extracted details
     * @return HashMap with formatted details
     */
    
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
        formattedDetails.put("URL", details.getOrDefault("url", "N/A"));
        formattedDetails.put("Extraction Date", details.getOrDefault("Extraction Date", "N/A"));
    
        // Add additional information dynamically
        formattedDetails.put("Etaj", details.getOrDefault("Etaj", "N/A"));
        formattedDetails.put("Chirie", details.getOrDefault("Chirie", "fara informatii"));
        formattedDetails.put("Informații suplimentare", details.getOrDefault("Informații suplimentare", "N/A"));
        formattedDetails.put("Tip vânzător", details.getOrDefault("Tip vânzător", "N/A"));
        formattedDetails.put("Liber de la", details.getOrDefault("Liber de la", "fara informatii"));
        formattedDetails.put("Tip proprietate", details.getOrDefault("Tip proprietate", "N/A"));
        formattedDetails.put("Forma de proprietate", details.getOrDefault("Forma de proprietate", "fara informatii"));
        formattedDetails.put("Stare", details.getOrDefault("Stare", "fara informatii"));
        formattedDetails.put("Încălzire", details.getOrDefault("Încălzire", "N/A"));

        // Add any other additional information dynamically
        for (String key : details.keySet()) {
            if (!formattedDetails.containsKey(key)) {
                formattedDetails.put(key, details.get(key)); // Add only keys not already in formattedDetails
            }
        }
    
        return formattedDetails;
    }
}
