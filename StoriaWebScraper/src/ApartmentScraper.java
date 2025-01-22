import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

import de.siegmar.fastcsv.writer.CsvWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ApartmentScraper extends Thread {

    private static final String BASE_URL = "https://www.storia.ro/ro/rezultate/vanzare/apartament/toata-romania?viewType=listing&page=";
    private static final List<HashMap<String, String>> processedApartments = new ArrayList<>();
    private static final int noPages = 2200;
    private static final int noThreads = 1;
    private final int startPage;
    private final int endPage;
    private static FileWriter file;
    static {
        try {
            file = new FileWriter("apartments.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final CsvWriter csv = CsvWriter.builder().build(file);

    public ApartmentScraper(int startPage, int endPage) throws IOException, InterruptedException {
        this.startPage = startPage;
        this.endPage = endPage;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        List<ApartmentScraper> threads = new ArrayList<>();
        int pagesPerThread = noPages / noThreads;

        // Create a CSV file that is initially populated with the headers for each column

        String[] headers = {
            "Header", 
            "Price", 
            "Price Per Square Meter", 
            "Surface", 
            "Rooms", 
            "Address", 
            "Latitude",
            "Longitude",
            "Heating Type", 
            "Floor", 
            "Condition", 
            "Property Type", 
            "Additional Information", 
            "Year of Construction", 
            "URL", 
            "Extraction Date"
        };

        csv.writeRecord(headers);

        for (int i = 0; i < noThreads; i++) {
            int startPage = i * pagesPerThread + 1;
            int endPage = (i == noThreads - 1) ? noPages : (startPage + pagesPerThread - 1);

            ApartmentScraper thread = new ApartmentScraper(startPage, endPage);
            threads.add(thread);
            thread.start();
        }

        for (ApartmentScraper thread : threads) {
            thread.join();
        }

        System.out.println("Processed Apartments: " + processedApartments.size());
    }

    @Override
    public void run() {
        try {
            getDataScraped(startPage, endPage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void addProcessedApartment(HashMap<String, String> apartmentDetails) {
        processedApartments.add(apartmentDetails);
    }

    public void getDataScraped(int startPage, int endPage) throws InterruptedException {
        WebDriver driver = new FirefoxDriver();
        try {
            driver.get(BASE_URL);
            AcceptCookies.acceptCookies(driver);
    
            for (int page = startPage; page <= endPage; page++) {
                driver.get(BASE_URL + page);
                System.out.println("Scraping page: " + page);
    
                List<WebElement> apartments = FetchApartments.fetchApartments(driver);
    
                if (apartments.isEmpty()) {
                    System.out.println("No apartments fetched on page " + page);
                    continue;
                }
    
                for (int index = 0; index < apartments.size(); index++) {
                    boolean clicked = AccesApartment.clickWithRetry(driver,
                            By.cssSelector(".css-1i43dhb > div:nth-child(3) > ul:nth-child(2) li:nth-child(" + (index + 1) + ")"));
    
                    if (clicked) {
                        // Extract apartment details
                        HashMap<String, String> apartmentDetails = ApartmentDetailsExtractor.extractApartmentDetails(driver);
                        addProcessedApartment(apartmentDetails);
    
                        // Display all apartment details
                        System.out.println("Apartment Details:");
                        apartmentDetails.forEach((key, value) -> System.out.println(key + ": " + value));
                        System.out.println("---------------------------------------------------");
    
                        // Prepare data for CSV writing
                        String[] values = {
                                apartmentDetails.getOrDefault("Header", "N/A"),
                                apartmentDetails.getOrDefault("Address", "N/A"),
                                apartmentDetails.getOrDefault("Latitude", "N/A"),
                                apartmentDetails.getOrDefault("Longitude", "N/A"),
                                apartmentDetails.getOrDefault("Price", "N/A"),
                                apartmentDetails.getOrDefault("Price Per Square Meter", "N/A"),
                                apartmentDetails.getOrDefault("Surface", "N/A"),
                                apartmentDetails.getOrDefault("Rooms", "N/A"),
                                apartmentDetails.getOrDefault("Heating Type", "N/A"),
                                apartmentDetails.getOrDefault("Floor", "N/A"),
                                apartmentDetails.getOrDefault("Condition", "N/A"),
                                apartmentDetails.getOrDefault("Property Type", "N/A"),
                                apartmentDetails.getOrDefault("Additional Information", "N/A"),
                                apartmentDetails.getOrDefault("Year of Construction", "N/A"),
                                apartmentDetails.getOrDefault("URL", "N/A"),
                                apartmentDetails.getOrDefault("Extraction Date", "N/A")
                        };
    
                        // Write to CSV
                        csv.writeRecord(values);
    
                        // Navigate back to the apartments list
                        driver.navigate().back();
    
                        // Wait for the apartments list to reload
                        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".css-1i43dhb > div:nth-child(2) > ul:nth-child(2) li")));
    
                        // Refresh the apartment list after navigating back
                        apartments = FetchApartments.fetchApartments(driver);
                    } else {
                        System.out.println("Failed to click on an apartment link.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during scraping: " + e.getMessage());
        } finally {
            Thread.sleep(6000);
            driver.quit();
            System.out.println("Final Processed Apartments Count: " + processedApartments.size());
            System.out.println("All Processed Apartments:");
            processedApartments.forEach(apartment -> {
                apartment.forEach((key, value) -> System.out.println(key + ": " + value));
                System.out.println("---------------------------------------------------");
            });
        }
    }
    
}
