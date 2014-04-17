/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.planners.enhanced;

import java.util.ArrayList;
import java.util.List;
import sim.util.Int2D;

/**
 *
 * @author Xu Meng
 */
public class TravelPath {
    private int distance;
    private List<Int2D> path;
    private int numUnknown;
    private int numExplored;

    public TravelPath(int distance, List<Int2D> path, int numUnknown, int numExplored) {
        this.distance = distance;
        this.path = path;
        this.numUnknown = numUnknown;
        this.numExplored = numExplored;
    }

    public int getDistance() {
        return distance;
    }

    public List<Int2D> getPath() {
        return path;
    }

    public int getNumUnknown() {
        return numUnknown;
    }

    public int getNumExplored() {
        return numExplored;
    }
    
    public TravelPath join(TravelPath anotherPath){
        List<Int2D> newPath = new ArrayList();
        newPath.addAll(path);
        newPath.addAll(anotherPath.path);
        return new TravelPath(this.distance + anotherPath.distance, newPath, this.numUnknown+anotherPath.numUnknown, this.numExplored+anotherPath.numExplored); 
    }
    
    public static TravelPath zeroLengthPath(){
        return new TravelPath(0, new ArrayList(), 0, 0);
    }
}
