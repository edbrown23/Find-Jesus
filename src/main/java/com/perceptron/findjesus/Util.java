package com.perceptron.findjesus;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * *****************************************************************************
 * This program is the property of Innovative Software Engineering
 * It is confidential and proprietary to Innovative Software Engineering
 * and may not be copied, used or disclosed without the advance
 * written permission of Innovative Software Engineering.
 * *****************************************************************************
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
