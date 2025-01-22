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


    //Close the survey if present : <div class="smcx-widget smcx-modal smcx-modal-survey smcx-widget-light smcx-hide-branding smcx-opaque smcx-show"><div class="smcx-modal-header"><div smcx-modal-headline="" class="smcx-modal-title">Chestionar imobiliare</div><div smcx-modal-close="" class="smcx-modal-close"></div></div><div class="smcx-modal-content"><div class="smcx-iframe-container"><iframe width="100%" height="100%" frameborder="0" allowtransparency="true" src="https://www.surveymonkey.com/r/SHWPYYG?embedded=1"></iframe></div></div><div class="smcx-widget-footer smcx-modal-footer"><a class="smcx-branding" href="https://www.surveymonkey.com/?ut_source=powered_by&amp;ut_source2=new_website_collector" target="_blank"><span class="smcx-powered-by">powered by</span></a></div></div>
    //press /html/body/div[5]/div[1]/div[2]


}


