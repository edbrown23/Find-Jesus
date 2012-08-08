import com.perceptron.findjesus.Util;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * User: ebrown
 * Date: 8/8/12
 * Time: 4:42 PM
 */
public class test {
    public static void main(String[] args){
        WebDriver browser = new ChromeDriver();
        Util.navigateToWikipedia(browser);
        Util.goToRandomArticle(browser);
        browser.quit();
    }
}
