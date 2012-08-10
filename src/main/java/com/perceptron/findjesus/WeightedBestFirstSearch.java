package com.perceptron.findjesus;

import org.cyberneko.html.HTMLScanner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import sun.management.GarbageCollectionNotifInfoCompositeData;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

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
public class WeightedBestFirstSearch {
    /** The goal of the search */
    private String jesusURL = "http://en.wikipedia.org/wiki/Jesus";
    private WebDriver browser;
    /** A blacklist of links which should be ignored in the search */
    private ArrayList<String> blacklist;
    /** A graylist of partial links which should probably be ignored in the search */
    private ArrayList<String> graylist;

    public WeightedBestFirstSearch(WebDriver browser){
        this.browser = browser;
        blacklist = new ArrayList<String>();
        graylist = new ArrayList<String>();
        setupGrayList();
    }

    private void setupGrayList(){
        graylist.add("User_talk"); // these links won't go anywhere, so kill them
        graylist.add("File:"); // these links are usually pictures and specifics, and they probably don't go anywhere
        graylist.add("Talk:"); // these links are comment-esque sections, and should be ignored
        graylist.add("User:"); // Similar to above, usually dead ends
        graylist.add("Special:");
        graylist.add("Wikipedia:");
        graylist.add("Template:");
    }

    public void runSearch(){
        ArrayList<String> visitedLinks = new ArrayList<String>();
        PriorityQueue<WeightedLink> neighbors = new PriorityQueue<WeightedLink>();
        WeightedLink currentNode = new WeightedLink(browser.getCurrentUrl(), 1.0f);
        neighbors.add(currentNode);
        while(neighbors.size() > 0){
            currentNode = neighbors.poll();
            browser.get(currentNode.getLink());
            System.out.println("Currently on page " + currentNode.getLink());
            visitedLinks.add(currentNode.getLink());
            if(currentNode.getLink().equals(jesusURL)){
                System.out.println("Found Jesus!");
                break;
            }
            ArrayList<String> currentNeighbors = getAllCurrentPageLinks();
            for(String n : currentNeighbors){
                boolean grayListed = false;
                for(String l : graylist){
                    if(n.contains(l)){
                        grayListed = true;
                        break;
                    }
                }
                if(!grayListed && !visitedLinks.contains(n) && Util.levenshteinDistance(n, currentNode.getLink()) > 5){
                    neighbors.add(new WeightedLink(n, 1.0f));
                }
            }
        }
    }

    private ArrayList<String> getAllCurrentPageLinks(){
        ArrayList<String> links = new ArrayList<String>();
        // I'm only interested in links within the content
        WebElement content = browser.findElement(By.id("mw-content-text"));
        // I'm assuming for now that all links will be in <a> tags
        List<WebElement> tags = content.findElements(By.tagName("a")); // This will probably take forever
        for(WebElement current : tags){
            String link = current.getAttribute("href");
            if(link != null){
                // We only accept links that keep us on wikipedia
                if(link.contains("http://en.wikipedia.org/wiki")){
                    links.add(link);
                }
            }
        }
        return links;
    }
}
