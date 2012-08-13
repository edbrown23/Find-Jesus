package com.perceptron.findjesus;

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
public class WeightedLink implements Comparable {
    private String link;
    private float weight;
    private int distance;

    public WeightedLink(String link, float weight, int distance){
        this.link = link;
        this.weight = weight;
        this.distance = distance;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof WeightedLink)){
            throw new IllegalArgumentException("Can only compare to other WeightedLinks!");
        }
        WeightedLink w = (WeightedLink)o;
        return (w.link.equals(link) && Math.abs(weight - w.weight) < 0.000001f);
    }

    @Override
    public int compareTo(Object o) {
        if(!(o instanceof WeightedLink)){
            throw new IllegalArgumentException("Can only compare to other WeightedLinks!");
        }
        WeightedLink w = (WeightedLink)o;
        if(weight < w.weight){
            return -1;
        }else if(weight > w.weight){
            return 1;
        }else{
            return 0;
        }
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
