package com.perceptron.findjesus;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.jgrapht.Graph;

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

    private Graph<WeightedLink,DefaultWeightedEdge> edgeGraph;

    private WeightStorage weightStorage;

    public WeightedBestFirstSearch(WebDriver browser){
        weightStorage = new WeightStorage();
        this.browser = browser;
        blacklist = new ArrayList<String>();
        graylist = new ArrayList<String>();
        edgeGraph = new SimpleGraph<WeightedLink, DefaultWeightedEdge>(DefaultWeightedEdge.class);
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

    }

    public void runSearch(){
        int visitCount = 0;
        ArrayList<String> visitedLinks = new ArrayList<String>();
        PriorityQueue<WeightedLink> neighbors = new PriorityQueue<WeightedLink>();
        WeightedLink currentNode = new WeightedLink(browser.getCurrentUrl(), 1.0f, 0); // The start link has a distance of 0
        neighbors.add(currentNode);
        edgeGraph.addVertex(currentNode);
        WeightedLink start = currentNode;
        WeightedLink goal = new WeightedLink(jesusURL, 100.0f, 0);
        while(neighbors.size() > 0){
            visitCount++;
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
                    float weight = 1.0f;
                    if(weightStorage.containsLink(n)){
                        weight = weightStorage.getPageWeight(n);
                    }
                    WeightedLink neighbor = new WeightedLink(n, weight, currentNode.getDistance() + 1);
                    neighbors.add(neighbor);
                    edgeGraph.addVertex(neighbor);
                    edgeGraph.addEdge(currentNode, neighbor, new DefaultWeightedEdge());
                }
            }
        }
        System.out.println("Visited " + visitCount + " total links");
        // Once we get to here, we've found Jesus, so run the modifications
        weightModification(edgeGraph, shortestPath(edgeGraph, start, goal));
    }

    private List<DefaultWeightedEdge> shortestPath(Graph<WeightedLink, DefaultWeightedEdge> graph, WeightedLink start, WeightedLink goal){
        // wrapper method for now, a specialized shortest path method will probably have to me made here
        return DijkstraShortestPath.findPathBetween(graph, start, goal);
    }

    private void weightModification(Graph<WeightedLink, DefaultWeightedEdge> graph, List<DefaultWeightedEdge> path){
        for(DefaultWeightedEdge edge : path){
            WeightedLink p = graph.getEdgeSource(edge);
            WeightedLink t = graph.getEdgeTarget(edge);
            weightStorage.addLink(p.getLink(), 2.0f);
            weightStorage.addLink(t.getLink(), 2.0f);
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
