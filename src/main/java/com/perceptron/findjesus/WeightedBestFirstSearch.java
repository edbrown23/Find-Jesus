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

    private Graph<WeightedLink,DefaultWeightedEdge> edgeGraph;

    private WeightStorage weightStorage;

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
        edgeGraph = new SimpleGraph<WeightedLink, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        WeightedLink currentNode = new WeightedLink(browser.getCurrentUrl(), 1.0f, 0); // The start link has a distance of 0
        WeightedLink goal = new WeightedLink(jesusURL, 100.0f, 0);
        neighbors.add(currentNode);
        edgeGraph.addVertex(currentNode);
        WeightedLink start = currentNode;
        CustomLogger.logMessage("Starting at: " + start.getLink());
        weightStorage.addLink(jesusURL, 100.0f);
        while(neighbors.size() > 0){
            if(visitCount > 100){
                CustomLogger.logMessage("Working too hard, giving up!");
                givingUp = true;
                break;
            }
            //System.out.println(neighbors.size());
            visitCount++;
            maxDistance = currentNode.getDistance();
            currentNode = neighbors.poll();
            browser.get(currentNode.getLink());
            CustomLogger.logMessage("Currently on page " + currentNode.getLink());
            visitedLinks.add(currentNode.getLink());
            if(currentNode.getLink().equals(jesusURL)){
                CustomLogger.logMessage("Found Jesus!");
                goal = currentNode;
                goal.setDistance(maxDistance + 1);
                break;
            }
            ArrayList<String> currentNeighbors = getAllCurrentPageLinks();
            ArrayList<String> currentPageAdditions = new ArrayList<String>();
            for(String n : currentNeighbors){
                boolean grayListed = false;
                for(String l : graylist){
                    if(n.contains(l)){
                        grayListed = true;
                        break;
                    }
                }
                if(!grayListed && !visitedLinks.contains(n) && !currentPageAdditions.contains(n) && Util.levenshteinDistance(n, currentNode.getLink()) > 5){
                    float weight = 1.0f;
                    if(weightStorage.containsLink(n)){
                        weight = weightStorage.getPageWeight(n);
                    }
                    WeightedLink neighbor = new WeightedLink(n, weight, currentNode.getDistance() + 1);
                    if(!neighbors.contains(neighbor)){
                        neighbors.add(neighbor);
                        currentPageAdditions.add(n);
                        edgeGraph.addVertex(neighbor);
                        edgeGraph.addEdge(currentNode, neighbor, new DefaultWeightedEdge());
                    }
                }
            }
            truncateNeighbors(neighbors, 100);
        }
        CustomLogger.logMessage("Visited " + visitCount + " total links");
        // Once we get to here, we've found Jesus, so run the modifications
        //weightModification(edgeGraph, shortestPath(edgeGraph, start, goal));
        if(!givingUp){
            ArrayList<WeightedLink> path = shortestPath(edgeGraph, start, goal);
            simpleWeightMod(edgeGraph, path);
            CustomLogger.logMessage("Shortest path contains " + path.size() + " links!");
            weightStorage.saveStorage(getFilePath());
            weightStorage.loadStorage(getFilePath());
        }else{
            failureMod(edgeGraph, visitedLinks);
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

    private ArrayList<WeightedLink> shortestPath(Graph<WeightedLink, DefaultWeightedEdge> graph, WeightedLink start, WeightedLink goal){
        ArrayList<WeightedLink> path = new ArrayList<WeightedLink>();
        WeightedLink currentNode = goal;
        while(!currentNode.equals(start)){
            path.add(currentNode);
            Set<DefaultWeightedEdge> edges = graph.edgesOf(currentNode);
            for(DefaultWeightedEdge edge : edges){
                if(graph.getEdgeSource(edge).getDistance() == (currentNode.getDistance() - 1)){
                    currentNode = graph.getEdgeSource(edge);
                    break;
                }
            }
        }
        path.add(currentNode);
        return path;
    }

    private void simpleWeightMod(Graph<WeightedLink, DefaultWeightedEdge> graph, ArrayList<WeightedLink> links){
        for(int i = 0; i < links.size(); i++){
            // We want to weight heavier those links that are close to the goal
            float expFactor = (float)Math.pow(Math.E, (float)(-1 * links.size()) / 8);
            float weight = (float)(links.size() - i) * expFactor + weightStorage.getPageWeight(links.get(i).getLink());
            if(Math.abs(weight) > 0.00001){
                weightStorage.addLink(links.get(i).getLink(), weight);
//                Set<DefaultWeightedEdge> neighbors = graph.edgesOf(links.get(i));
//                for(DefaultWeightedEdge edge : neighbors){
//                    WeightedLink n = graph.getEdgeTarget(edge);
//                    float neighborWeight = (weight / 2) + weightStorage.getPageWeight(n.getLink());
//                    if(Math.abs(neighborWeight) > 0.00001){
//                        weightStorage.addLink(n.getLink(), neighborWeight);
//                    }
//                }
            }
        }
    }

    private void failureMod(Graph<WeightedLink, DefaultWeightedEdge> graph, ArrayList<String> links){
        for(String link : links){
            float weight = (weightStorage.getPageWeight(link)) / 2.0f;
            if(Math.abs(weight) > 0.00001){
                weightStorage.addLink(link, weight);
//                Set<DefaultWeightedEdge> neighbors = graph.edgesOf(new WeightedLink(link, 0.0f, 0));
//                for(DefaultWeightedEdge edge : neighbors){
//                    WeightedLink n = graph.getEdgeTarget(edge);
//                    float neighborWeight = weightStorage.getPageWeight(n.getLink()) - (weight / 4);
//                    if(Math.abs(neighborWeight) > 0.00001){
//                        weightStorage.addLink(n.getLink(), neighborWeight);
//                    }
//                }
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
                if(link.contains(baseURL)){
                    links.add(link);
                }
            }
        }
        return links;
    }
}
