package com.perceptron.findjesus;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * User: ebrown
 * Date: 8/8/12
 * Time: 4:39 PM
 */
public class Util {
    public static void navigateToWikipedia(WebDriver browser){
        browser.get("http://en.wikipedia.org/wiki/Main_Page");
    }

    public static void goToRandomArticle(WebDriver browser){
        String link = browser.findElement(By.linkText("Random article")).getAttribute("href");
        browser.get(link);
    }
}
