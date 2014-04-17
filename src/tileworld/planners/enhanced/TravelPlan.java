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
public class TravelPlan implements Comparable<TravelPlan>{
    private Int2D destination;
    private TravelPath path;
    private double probabilityOfHaving;
    private double probabilityOfReaching;
    private int cost;
    private List<Class> sequence;

    public TravelPlan(Int2D destination, TravelPath path, double probabilityOfHaving, double probabilityOfReaching, int cost, List<Class> sequence) {
        this.destination = destination;
        this.path = path;
        this.probabilityOfHaving = probabilityOfHaving;
        this.probabilityOfReaching = probabilityOfReaching;
        this.cost = cost;
        this.sequence = sequence;
    }

    public Int2D getDestination() {
        return destination;
    }

    public TravelPath getPath() {
        return path;
    }

    public double getProbabilityOfHaving() {
        return probabilityOfHaving;
    }

    public double getProbabilityOfReaching() {
        return probabilityOfReaching;
    }
    
    public int getCost() {
        return cost;
    }

    public List<Class> getSequence() {
        return sequence;
    }
    
    public int compareTo(TravelPlan t) {
        if(this.probabilityOfHaving * this.probabilityOfReaching > t.probabilityOfHaving * t.probabilityOfReaching){
            return 1;
        } else if(this.probabilityOfHaving * this.probabilityOfReaching < t.probabilityOfHaving * t.probabilityOfReaching){
            return -1;
        } else{
            return 0;
        }
    }

    public TravelPlan join(TravelPlan plan){
        List<Class> combinedSequence = new ArrayList();
        combinedSequence.addAll(this.sequence);
        combinedSequence.addAll(plan.sequence);
        return new TravelPlan(plan.destination, this.path.join(plan.path), this.probabilityOfHaving * plan.probabilityOfHaving, this.probabilityOfReaching * plan.probabilityOfReaching, this.cost+plan.cost, combinedSequence);
    }
    
    public static TravelPlan zeroLengthPlan(int x, int y){
        return new TravelPlan(new Int2D(x, y), TravelPath.zeroLengthPath(), 1, 1, 0, new ArrayList());
    }
}
