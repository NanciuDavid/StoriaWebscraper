import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import de.siegmar.fastcsv.writer.CsvWriter;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

public class ApartmentScraper extends Thread {

    private static final String BASE_URL = "https://www.storia.ro/ro/rezultate/vanzare/apartament/toata-romania?viewType=listing&page=";
    private static final List<Apartment> processedApartments = new ArrayList<>();
    private static final int TOTAL_PAGES = 2200;
    private static final int THREAD_COUNT = 1;
    private final int startPage;
    private final int endPage;
    private static FileWriter file;
    private static CsvWriter csv;

    static {
        try {
            file = new FileWriter("apartments.csv");
            csv = CsvWriter.builder().build(file);
            writeCSVHeaders();
        } catch (IOException e) {
            throw new RuntimeException("Error initializing file writer", e);
        }
    }

    public ApartmentScraper(int startPage, int endPage) {
        this.startPage = startPage;
        this.endPage = endPage;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        List<ApartmentScraper> threads = new ArrayList<>();
        int pagesPerThread = TOTAL_PAGES / THREAD_COUNT;

        for (int i = 0; i < THREAD_COUNT; i++) {
            int startPage = i * pagesPerThread + 1;
            int endPage = (i == THREAD_COUNT - 1) ? TOTAL_PAGES : (startPage + pagesPerThread - 1);
            ApartmentScraper thread = new ApartmentScraper(startPage, endPage);
            threads.add(thread);
            thread.start();
        }

        for (ApartmentScraper thread : threads) {
            thread.join();
        }

        file.close(); // Close file at the end
    }

    @Override
    public void run() {
        try {
            getDataScraped(startPage, endPage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("[ERROR] CSV Writing Issue: " + e.getMessage());
        }
    }

    private static void writeCSVHeaders() throws IOException {
        List<String> headers = new ArrayList<>(Arrays.asList(
                "Header", "Price", "Price Per Square Meter", "Surface", "Rooms",
                "Address", "Latitude", "Longitude", "URL", "Extraction Date",
                "Floor", "Rent", "Additional Information", "Seller Type",
                "Free From", "Type of Property", "Form of Property", "Status",
                "Heating Type", "Accessibility Score"
        ));
        
        // Add headers for known facility flags
        headers.add("Flag_hasSchoolNearby");
        headers.add("Flag_hasParkNearby");
        headers.add("Flag_hasPublicTransportNearby");
        headers.add("Flag_hasSupermarketNearby");
        headers.add("Flag_hasHospitalNearby");
        headers.add("Flag_hasRestaurantNearby");
        headers.add("Flag_hasGymNearby");
        headers.add("Flag_hasShoppingMallNearby");
        
        // Add headers for known facility distances
        headers.add("Distance_schoolDistance");
        headers.add("Distance_parkDistance");
        headers.add("Distance_publicTransportDistance");
        headers.add("Distance_supermarketDistance");
        headers.add("Distance_hospitalDistance");
        headers.add("Distance_restaurantDistance");
        headers.add("Distance_gymDistance");
        headers.add("Distance_shoppingMallDistance");
        
        csv.writeRecord(headers.toArray(new String[0]));
    }

    public void getDataScraped(int startPage, int endPage) throws InterruptedException, IOException {
        WebDriver driver = new ChromeDriver();
        HashSet<String> visitedApartments = new HashSet<>();
        try {
            for (int page = startPage; page <= endPage; page++) {
                System.out.println("Processing page " + page);
                driver.get(BASE_URL + page);
                AcceptCookies.acceptCookies(driver);

                boolean allApartmentsProcessed = false;
                int retryCount = 0;

                while (!allApartmentsProcessed && retryCount < 3) {
                    try {
                        // Fetch apartments on the current page
                        List<WebElement> apartments = FetchApartments.fetchApartments(driver);
                        if (apartments.isEmpty()) {
                            System.out.println("No apartments found on page " + page);
                            allApartmentsProcessed = true;
                            continue;
                        }

                        // Track how many apartments we've processed on this page
                        int processedCount = 0;
                        int totalOnPage = apartments.size();

                        for (int index = 0; index < apartments.size(); index++) {
                            try {
                                // Refresh the apartment list to avoid stale elements
                                apartments = FetchApartments.fetchApartments(driver);
                                if (index >= apartments.size())
                                    continue;

                                WebElement apartment = apartments.get(index);
                                String apartmentUrl = apartment.findElement(By.tagName("a")).getDomAttribute("href");

                                // Skip if already visited
                                if (visitedApartments.contains(apartmentUrl)) {
                                    processedCount++;
                                    continue;
                                }

                                // Scroll to make the element visible and clickable
                                ((JavascriptExecutor) driver).executeScript(
                                        "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                                        apartment);
                                Thread.sleep(1000); // Give time for scrolling to complete

                                // Click with explicit wait
                                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                                wait.until(ExpectedConditions.elementToBeClickable(apartment)).click();

                                // Process apartment details
                                HashMap<String, String> apartmentDetails = ApartmentDetailsExtractor
                                        .extractApartmentDetails(driver);
                                Apartment apartmentObj = new Apartment(apartmentDetails);
                                ApartmentFacilitySearcher.enrichApartmentWithFacilityData(apartmentObj);

                                // Save data
                                processedApartments.add(apartmentObj);
                                visitedApartments.add(apartmentUrl);
                                writeApartmentToCSV(apartmentObj);

                                System.out
                                        .println("Processed apartment " + (processedCount + 1) + " of " + totalOnPage +
                                                " on page " + page);

                                // Return to listing page
                                driver.navigate().back();

                                // Wait for page to reload with explicit condition
                                wait.until(ExpectedConditions.presenceOfElementLocated(
                                        By.cssSelector(".css-1i43dhb > div:nth-child(3) > ul:nth-child(2) li")));

                                processedCount++;

                            } catch (StaleElementReferenceException e) {
                                System.out.println("Stale element, refreshing page...");
                                driver.navigate().refresh();
                                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                                wait.until(ExpectedConditions.presenceOfElementLocated(
                                        By.cssSelector(".css-1i43dhb > div:nth-child(3) > ul:nth-child(2) li")));
                                // Don't increment processedCount - we'll retry this apartment
                                index--; // Try this index again
                            } catch (Exception e) {
                                System.err.println("[ERROR] Issue while processing apartment: " + e.getMessage());
                                e.printStackTrace();

                                // Check if we're still on details page and navigate back if needed
                                if (!driver.getCurrentUrl().contains(BASE_URL)) {
                                    driver.navigate().back();
                                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                                    wait.until(ExpectedConditions.presenceOfElementLocated(
                                            By.cssSelector(".css-1i43dhb > div:nth-child(3) > ul:nth-child(2) li")));
                                }

                                processedCount++; // Count this as processed even though it failed
                            }
                        }

                        // Check if we've processed all apartments on this page
                        if (processedCount >= totalOnPage) {
                            allApartmentsProcessed = true;
                            System.out.println("Completed page " + page + ": processed " + processedCount +
                                    " out of " + totalOnPage + " apartments");
                        } else {
                            System.out.println("Only processed " + processedCount + " out of " + totalOnPage +
                                    " apartments on page " + page + ". Retrying...");
                            retryCount++;
                            // Refresh the page before retrying
                            driver.navigate().refresh();
                            Thread.sleep(3000);
                        }

                    } catch (Exception e) {
                        System.err.println("[ERROR] Issue with page " + page + ": " + e.getMessage());
                        e.printStackTrace();
                        retryCount++;
                        // Try to get back to the listings page
                        driver.get(BASE_URL + page);
                        Thread.sleep(3000);
                    }
                }

                if (!allApartmentsProcessed) {
                    System.out.println("Warning: Could not process all apartments on page " + page +
                            " after " + retryCount + " retries");
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Scraper encountered a major issue: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Thread.sleep(3000);
            driver.quit();
        }
    }

    private static void writeApartmentToCSV(Apartment apartment) throws IOException {
        Map<String, String> allFields = apartment.getAllFields();
        List<String> values = new ArrayList<>();
    
        // Add basic fields
        values.add(allFields.getOrDefault("Header", "N/A"));
        values.add(allFields.getOrDefault("Price", "0"));
        values.add(allFields.getOrDefault("Price Per Square Meter", "0"));
        values.add(allFields.getOrDefault("Surface", "0"));
        values.add(allFields.getOrDefault("Rooms", "0"));
        values.add(allFields.getOrDefault("Address", "N/A"));
        values.add(allFields.getOrDefault("Latitude", "0"));
        values.add(allFields.getOrDefault("Longitude", "0"));
        values.add(allFields.getOrDefault("URL", "N/A"));
        values.add(allFields.getOrDefault("Extraction Date", "N/A"));
        values.add(allFields.getOrDefault("Floor", "N/A"));
        values.add(allFields.getOrDefault("Rent", "0"));
        values.add(allFields.getOrDefault("Additional Information", "N/A"));
        values.add(allFields.getOrDefault("Seller Type", "N/A"));
        values.add(allFields.getOrDefault("Free From", "N/A"));
        values.add(allFields.getOrDefault("Type Of Property", "N/A"));
        values.add(allFields.getOrDefault("Form Of Property", "N/A"));
        values.add(allFields.getOrDefault("Status", "N/A"));
        values.add(allFields.getOrDefault("Heating Type", "N/A"));
        values.add(allFields.getOrDefault("Accessibility Score", "0"));
    
        // Add facility flags
        values.add(allFields.getOrDefault("Flag_hasSchoolNearby", "false"));
        values.add(allFields.getOrDefault("Flag_hasParkNearby", "false"));
        values.add(allFields.getOrDefault("Flag_hasPublicTransportNearby", "false"));
        values.add(allFields.getOrDefault("Flag_hasSupermarketNearby", "false"));
        values.add(allFields.getOrDefault("Flag_hasHospitalNearby", "false"));
        values.add(allFields.getOrDefault("Flag_hasRestaurantNearby", "false"));
        values.add(allFields.getOrDefault("Flag_hasGymNearby", "false"));
        values.add(allFields.getOrDefault("Flag_hasShoppingMallNearby", "false"));
        
        // Add facility distances
        values.add(allFields.getOrDefault("Distance_schoolDistance", "-1"));
        values.add(allFields.getOrDefault("Distance_parkDistance", "-1"));
        values.add(allFields.getOrDefault("Distance_publicTransportDistance", "-1"));
        values.add(allFields.getOrDefault("Distance_supermarketDistance", "-1"));
        values.add(allFields.getOrDefault("Distance_hospitalDistance", "-1"));
        values.add(allFields.getOrDefault("Distance_restaurantDistance", "-1"));
        values.add(allFields.getOrDefault("Distance_gymDistance", "-1"));
        values.add(allFields.getOrDefault("Distance_shoppingMallDistance", "-1"));
    
        csv.writeRecord(values.toArray(new String[0]));
    }
}
