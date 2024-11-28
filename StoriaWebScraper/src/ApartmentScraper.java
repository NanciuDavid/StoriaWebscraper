import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private static final int noPages = 2200; // Total number of pages to scrape
    private static final int noThreads = 10; // Number of threads
    private final int startPage; // Starting page for this thread
    private final int endPage; // Ending page for this thread

    public ApartmentScraper(int startPage, int endPage) {
        this.startPage = startPage;
        this.endPage = endPage;
    }

    public static void main(String[] args) throws InterruptedException {
        List<ApartmentScraper> threads = new ArrayList<>();
        int pagesPerThread = noPages / noThreads;

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
                List<WebElement> apartments = FetchApartments.fetchApartments(driver);

                if (apartments.isEmpty()) {
                    System.out.println("No apartments fetched on page " + page);
                    continue;
                }

                for (int index = 0; index < apartments.size(); index++) {
                    WebElement apartment = apartments.get(index);
                    boolean clicked = AccesApartment.clickWithRetry(driver, By.cssSelector(".css-1i43dhb > div:nth-child(3) > ul:nth-child(2) li:nth-child(" + (index + 1) + ")"));

                    if (clicked) {
                        HashMap<String, String> apartmentDetails = ApartmentDetailsExtractor.extractApartmentDetails(driver);
                        addProcessedApartment(apartmentDetails);
                        System.out.println(apartmentDetails.get("Price"));

                        driver.navigate().back();
                        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".css-1i43dhb > div:nth-child(2) > ul:nth-child(2) li")));
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
        }
    }
}
