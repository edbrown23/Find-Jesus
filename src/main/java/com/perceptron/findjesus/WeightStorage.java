package com.perceptron.findjesus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

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
        if(pageWeights.get(link) == null){
            return 0;
        }else{
            return pageWeights.get(link);
        }
    }

    public void addLink(String link, float value){
        pageWeights.put(link, value);
    }

    public void saveStorage(String fileLoc){
        try {
            PrintWriter output = new PrintWriter(new File(fileLoc));
            for(String url : pageWeights.keySet()){
                String weight = Float.toString(pageWeights.get(url));
                output.println(url + "\t" + weight);
            }
            output.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not find the file specified with name = " + fileLoc);
        }
    }

    public void loadStorage(String fileLoc){
        try {
            Scanner input = new Scanner(new File(fileLoc));
            while(input.hasNext()){
                String items[] = input.nextLine().split("\t");
                pageWeights.put(items[0], Float.parseFloat(items[1]));
            }
            input.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not find the file specified with name = " + fileLoc);
        }
    }
}
