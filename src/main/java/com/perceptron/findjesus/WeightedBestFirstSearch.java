package com.perceptron.findjesus;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.jgrapht.Graph;

import java.util.*;

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
    private String baseURL = "http://en.wikipedia.org/wiki";
    private WebDriver browser;
    /** A blacklist of links which should be ignored in the search */
    private ArrayList<String> blacklist;
    /** A graylist of partial links which should probably be ignored in the search */
    private ArrayList<String> graylist;

    private WeightStorage weightStorage;

    private TreeNode startNode;

    public WeightedBestFirstSearch(WebDriver browser){
        weightStorage = new WeightStorage();
        weightStorage.loadStorage(getFilePath());
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
        graylist.add("Portal:");
        graylist.add("Template_talk:");
        graylist.add("Category:");
        graylist.add("#");
        graylist.add("Help:");
    }

    public void runSearch(){
        int visitCount = 0;
        boolean givingUp = false;
        int maxDistance = 0;

        ArrayList<String> visitedLinks = new ArrayList<String>();
        PriorityQueue<WeightedLink> neighbors = new PriorityQueue<WeightedLink>();
        ArrayList<String> currentPageAdditions = new ArrayList<String>();
        ArrayList<String> currentNeighbors = new ArrayList<String>();

        WeightedLink currentNode = new WeightedLink(browser.getCurrentUrl(), 1.0f, 0); // The start link has a distance of 0
        WeightedLink lastNode;
        startNode = new TreeNode(null, 0, browser.getCurrentUrl());
        currentNode.setTreeNode(startNode);

        WeightedLink goal = new WeightedLink(jesusURL, 100.0f, 0);
        neighbors.add(currentNode);
        WeightedLink start = currentNode;
        CustomLogger.logMessage("Starting at: " + start.getLink());

        if(!weightStorage.containsLink(jesusURL)){
            weightStorage.addLink(jesusURL, 100.0f);
        }

        while(neighbors.size() > 0){
            if(visitCount > 100){
                CustomLogger.logMessage("Working too hard, giving up!");
                givingUp = true;
                break;
            }
            visitCount++;
            maxDistance = currentNode.getDistance();
            lastNode = currentNode;
            currentNode = neighbors.poll();
            currentNode.setTreeNode(new TreeNode(lastNode.getNode(), lastNode.getDistance() + 1, currentNode.getLink()));
            browser.get(currentNode.getLink());
            CustomLogger.logMessage("Currently on page " + currentNode.getLink());
            visitedLinks.add(currentNode.getLink());
            if(currentNode.getLink().equals(jesusURL)){
                CustomLogger.logMessage("Found Jesus!");
                goal = currentNode;
                goal.setDistance(maxDistance + 1);
                break;
            }
            currentNeighbors.clear();
            currentPageAdditions.clear();
            getAllCurrentPageLinks(currentNeighbors);
            for(String n : currentNeighbors){
                boolean grayListed = false;
                for(String l : graylist){
                    if(n.contains(l)){
                        grayListed = true;
                        break;
                    }
                }
                if(!grayListed  && !visitedLinks.contains(n) && !currentPageAdditions.contains(n) && Util.levenshteinDistance(n, currentNode.getLink()) > 5){
                    float weight = 1.0f;
                    if(weightStorage.containsLink(n)){
                        weight = weightStorage.getPageWeight(n);
                    }
                    WeightedLink neighbor = new WeightedLink(n, weight, currentNode.getDistance() + 1);
                    if(!neighbors.contains(neighbor)){
                        neighbors.add(neighbor);
                        currentPageAdditions.add(n);
                        currentNode.getNode().addChild(new TreeNode(currentNode.getNode(), currentNode.getNode().getDistanceFromStart() + 1, neighbor.getLink()));
                    }
                }
            }
            truncateNeighbors(neighbors, 100);
        }
        CustomLogger.logMessage("Visited " + visitCount + " total links");
        // Once we get to here, we've found Jesus, so run the modifications
        if(!givingUp){
            ArrayList<String> path = shortestPath(start.getNode(), goal.getNode());
            simpleWeightMod(path);
            CustomLogger.logMessage("Shortest path contains " + path.size() + " links!");
            weightStorage.saveStorage(getFilePath());
            weightStorage.loadStorage(getFilePath());
        }else{
            failureMod(visitedLinks);
        }
    }

    private void truncateNeighbors(PriorityQueue<WeightedLink> neighbors, int size){
        if(neighbors.size() > size){
            WeightedLink links[] = neighbors.toArray(new WeightedLink[1]);
            Arrays.sort(links);
            for(int i = (size - 1); i < links.length; i++){
                neighbors.remove(links[i]);
            }
        }
    }

    private String getFilePath(){
        String portions[] = jesusURL.split("/");
        return portions[portions.length - 1] + ".csv";
    }

    private ArrayList<String> shortestPath(TreeNode start, TreeNode goal){
        ArrayList<String> path = new ArrayList<String>();
        TreeNode currentNode = goal;
        // TODO implement equals to get rid of this
        while(!currentNode.getName().equals(start.getName())){
            path.add(currentNode.getName());
            currentNode = currentNode.getParent();
        }
        path.add(currentNode.getName());
        return path;
    }

    private void simpleWeightMod(ArrayList<String> links){
        for(int i = 0; i < links.size(); i++){
            // We want to weight heavier those links that are close to the goal
            float expFactor = (float)Math.pow(Math.E, (float)(-1 * links.size()) / 8);
            float weight = (float)(links.size() - i) * expFactor + weightStorage.getPageWeight(links.get(i));
            if(Math.abs(weight) > 0.00001){
                weightStorage.addLink(links.get(i), weight);
            }
        }
    }

    private void failureMod(ArrayList<String> links){
        for(String link : links){
            float weight = (weightStorage.getPageWeight(link)) / 2.0f;
            if(Math.abs(weight) > 0.00001){
                weightStorage.addLink(link, weight);
            }
        }
    }

    private void getAllCurrentPageLinks(ArrayList<String> links){
        // I'm only interested in links within the content
        WebElement content = browser.findElement(By.id("mw-content-text"));
        // I'm assuming for now that all links will be in <a> tags
        List<WebElement> tags = content.findElements(By.tagName("a"));
        for(WebElement current : tags){
            String link = current.getAttribute("href");
            if(link != null){
                // We only accept links that keep us on wikipedia
                if(link.contains(baseURL)){
                    links.add(link);
                }
            }
        }
        tags.clear();
    }
}
