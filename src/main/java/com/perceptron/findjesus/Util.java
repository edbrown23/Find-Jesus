package com.perceptron.findjesus;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * This software falls under the MIT license, as follows:
 * Copyright (C) 2012
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * Created By: Eric Brown
 * Date: 8/8/12
 */
public class Util {
    public static void navigateToWikipedia(WebDriver browser){
        browser.get("http://en.wikipedia.org/wiki/Main_Page");
    }

    public static void goToRandomArticle(WebDriver browser){
        String link = browser.findElement(By.linkText("Random article")).getAttribute("href");
        browser.get(link);
    }

    public static int levenshteinDistance(String s, String t){
        int m = s.length();
        int n = t.length();
        int d[][] = new int[m + 1][n + 1];
        for(int i = 0; i <= m; i++){
            for(int j = 0; j <= n; j++){
                d[i][j] = 0;
            }
        }
        for(int i = 1; i <= m; i++){
            d[i][0] = i;
        }

        for(int j = 1; j <= n; j++){
            d[0][j] = j;
        }

        for(int j = 1; j <= n; j++){
            for(int i = 1; i <= m; i++){
                if(s.charAt(i - 1) == t.charAt(j - 1)){
                    d[i][j] = d[i - 1][j - 1];
                }else{
                    d[i][j] = getMin(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + 1);
                }
            }
        }

        return d[m][n];
    }

    private static int getMin(int a, int b, int c){
        int min = a;
        if(b < min){
            min = b;
        }
        if(c < min){
            min = c;
        }
        return min;
    }
}
