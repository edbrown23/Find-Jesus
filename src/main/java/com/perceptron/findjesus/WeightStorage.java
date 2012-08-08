package com.perceptron.findjesus;

import java.util.HashMap;

/**
 * *****************************************************************************
 * This program is the property of Innovative Software Engineering
 * It is confidential and proprietary to Innovative Software Engineering
 * and may not be copied, used or disclosed without the advance
 * written permission of Innovative Software Engineering.
 * *****************************************************************************
 * User: ebrown
 * Date: 8/8/12
 * Time: 4:55 PM
 */
public class WeightStorage {
    private HashMap<String, Float> pageWeights;

    public WeightStorage(){
        pageWeights = new HashMap<String, Float>();
    }

    public WeightStorage(HashMap<String, Float> oldWeights){
        pageWeights = new HashMap<String, Float>(oldWeights);
    }

    public boolean containsLink(String link){
        return pageWeights.containsKey(link);
    }

    public float getPageWeight(String link){
        return pageWeights.get(link);
    }

    public void addLink(String link, float value){
        pageWeights.put(link, value);
    }
}
