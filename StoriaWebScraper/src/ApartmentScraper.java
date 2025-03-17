import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import de.siegmar.fastcsv.writer.CsvWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * ApartmentScraper is a multi-threaded web scraper for extracting apartment listings from Storia.ro.
 * The extracted data is saved into a CSV file.
 */
public class ApartmentScraper extends Thread {

    private static final String BASE_URL = "https://www.storia.ro/ro/rezultate/vanzare/apartament/toata-romania?viewType=listing&page=";
    private static final List<HashMap<String, String>> processedApartments = new ArrayList<>();
    private static final int TOTAL_PAGES = 2200;
    private static final int THREAD_COUNT = 1;
    private final int startPage;
    private final int endPage;
    private static FileWriter file;

    static {
        try {
            file = new FileWriter("apartments.csv");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing file writer", e);
        }
    }

    private static final CsvWriter csv = CsvWriter.builder().build(file);

    public ApartmentScraper(int startPage, int endPage) {
        this.startPage = startPage;
        this.endPage = endPage;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        List<ApartmentScraper> threads = new ArrayList<>();
        int pagesPerThread = TOTAL_PAGES / THREAD_COUNT;

        // Define and write CSV headers
        String[] headers = {
            "Header", "Price", "Price Per Square Meter", "Surface", "Rooms",
            "Address", "Latitude", "Longitude", "URL", "Extraction Date",
            "Etaj", "Chirie", "Informații suplimentare", "Tip vânzător",
            "Liber de la", "Tip proprietate", "Forma de proprietate",
            "Stare", "Încălzire"
        };
        csv.writeRecord(headers);

        // Start scraper threads
        for (int i = 0; i < THREAD_COUNT; i++) {
            int startPage = i * pagesPerThread + 1;
            int endPage = (i == THREAD_COUNT - 1) ? TOTAL_PAGES : (startPage + pagesPerThread - 1);
            ApartmentScraper thread = new ApartmentScraper(startPage, endPage);
            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to complete
        for (ApartmentScraper thread : threads) {
            thread.join();
        }
    }

    @Override
    public void run() {
        try {
            getDataScraped(startPage, endPage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Adds extracted apartment details to the processed list.
     * @param apartmentDetails HashMap containing apartment information
     */
    
    public static synchronized void addProcessedApartment(HashMap<String, String> apartmentDetails) {
        processedApartments.add(apartmentDetails);
    }

    /**
     * Scrapes apartment data from the website.
     * @param startPage Starting page number
     * @param endPage Ending page number
     */

    public void getDataScraped(int startPage, int endPage) throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        HashSet<String> visitedApartments = new HashSet<>();
        try {
            for (int page = startPage; page <= endPage; page++) {
                driver.get(BASE_URL + page);
                AcceptCookies.acceptCookies(driver);
                List<WebElement> apartments = FetchApartments.fetchApartments(driver);
                if (apartments.isEmpty()) continue;

                for (int index = 0; index < apartments.size(); index++) {
                    try {
                        apartments = FetchApartments.fetchApartments(driver);
                        if (index >= apartments.size()) continue;

                        WebElement apartment = apartments.get(index);
                        String apartmentUrl = apartment.findElement(By.tagName("a")).getDomAttribute("href");
                        if (visitedApartments.contains(apartmentUrl)) continue;

                        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, document.body.scrollHeight);");
                        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                        wait.until(ExpectedConditions.elementToBeClickable(apartment)).click();

                        // Extract apartment details
                        HashMap<String, String> apartmentDetails = ApartmentDetailsExtractor.extractApartmentDetails(driver);
                        Apartment apartmentObj = new Apartment(apartmentDetails);

                        addProcessedApartment(apartmentDetails);
                        visitedApartments.add(apartmentUrl);

                        // Write extracted data to CSV file
                        csv.writeRecord(new String[]{
                            apartmentDetails.getOrDefault("Header", "N/A"),
                            apartmentDetails.getOrDefault("Price", "N/A"),
                            apartmentDetails.getOrDefault("Price Per Square Meter", "N/A"),
                            apartmentDetails.getOrDefault("Surface", "N/A"),
                            apartmentDetails.getOrDefault("Rooms", "N/A"),
                            apartmentDetails.getOrDefault("Address", "N/A"),
                            apartmentDetails.getOrDefault("Latitude", "N/A"),
                            apartmentDetails.getOrDefault("Longitude", "N/A"),
                            apartmentDetails.getOrDefault("URL", "N/A"),
                            apartmentDetails.getOrDefault("Extraction Date", "N/A"),
                            apartmentDetails.getOrDefault("Etaj:", "N/A"),
                            apartmentDetails.getOrDefault("Chirie:", "N/A"),
                            apartmentDetails.getOrDefault("Informații suplimentare:", "N/A"),
                            apartmentDetails.getOrDefault("Tip vânzător:", "N/A"),
                            apartmentDetails.getOrDefault("Liber de la:", "N/A"),
                            apartmentDetails.getOrDefault("Tip proprietate:", "N/A"),
                            apartmentDetails.getOrDefault("Forma de proprietate:", "N/A"),
                            apartmentDetails.getOrDefault("Stare:", "N/A"),
                            apartmentDetails.getOrDefault("Încălzire:", "N/A")
                        });
                        
        

                        System.out.println(apartmentObj);

                        driver.navigate().back();
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        System.err.println("[ERROR] Issue while processing apartment: " + e.getMessage());
                        driver.navigate().back();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Scraper encountered an issue: " + e.getMessage());
        } finally {
            Thread.sleep(6000);
            driver.quit();
        }
    }
}
