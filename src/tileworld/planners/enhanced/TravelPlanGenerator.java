/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.planners.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import sim.util.Int2D;
import tileworld.agent.TWAgentPercept;
import tileworld.agent.TWAgentWorkingMemory;
import tileworld.agent.enhanced.CellSelector;
import tileworld.agent.enhanced.DerivedConstants;
import tileworld.agent.enhanced.ProbabilityCalculator;
import tileworld.environment.TWHole;
import tileworld.environment.TWTile;
import tileworld.util.CombinatoricsUtil;

/**
 *
 * @author Xu Meng
 */
public class TravelPlanGenerator {
    private TWAgentWorkingMemory memory;
    private CellSelector selector;
    private TravelPathGenerator pather;
    private boolean debug;
    
    public TravelPlanGenerator(TWAgentWorkingMemory memory, boolean debug) {
        this.memory = memory;
        this.selector = new CellSelector(memory);
        this.pather = new TravelPathGenerator(memory);
        this.debug = debug;
    }
    
    public TreeSet<TravelPlan> plan(Class type, Int2D pos, int step, double timestamp, TravelPath existingPath, int level){
if(debug){
    printTabs(level);
    System.out.println("Searching for "+type+" - at "+pos.toCoordinates()+", within "+step+" steps, at timestamp "+timestamp);                      
}
        TreeSet<TravelPlan> result = new TreeSet();
        List<Int2D> cellsInRange = selector.cellsWithinStep(pos.x, pos.y, step);
        
        List<Class> sequence = new ArrayList();
        sequence.add(type);

        for(Int2D cell : cellsInRange){  
            TWAgentPercept percept = memory.getPercept(cell.x, cell.y);
            
            if(!onPath(cell.x, cell.y, existingPath)){
                
                TravelPath path = null;
                double probOfHavingMin = 0;
                double probOfReaching = 0;
                
                if(percept==null || timestamp + memory.distance(pos.x, pos.y, cell.x, cell.y) - percept.getT()>=percept.getTTL()){
                    path = pather.findMaxExploredPath(pos.x, pos.y, cell.x, cell.y, timestamp);
                    if(path!=null){
                        probOfHavingMin = ProbabilityCalculator.probabilityOfHavingMin(path, 1, type);
                        probOfReaching = ProbabilityCalculator.probabilityOfReaching(path, step);
                        if(probOfHavingMin * probOfReaching>=DerivedConstants.THREHOLD){
                            result.add(new TravelPlan(cell, path, probOfHavingMin, probOfReaching, step, sequence));
                        }
                    }
                }            
                else if(type.isInstance(percept.getO()) && timestamp + memory.distance(pos.x, pos.y, cell.x, cell.y) - percept.getT()<percept.getTTL()){
                    path = pather.findMinUnknownPath(pos.x, pos.y, cell.x, cell.y, timestamp);
                    if(path!=null){
                        probOfHavingMin = 1.0;
                        probOfReaching = ProbabilityCalculator.probabilityOfReaching(path, step);                
                        if(probOfHavingMin * probOfReaching>=DerivedConstants.THREHOLD){
                            result.add(new TravelPlan(cell, path, probOfHavingMin, probOfReaching, step, sequence));
                        }
                    }
                }
if(debug){
    printTabs(level+1);
    if(path==null){
        System.out.println("Investigating cell: "+cell.toCoordinates()+", prob of having: "+probOfHavingMin+", probOfReaching: "+probOfReaching + ", Path: null"); 
    } else{
        System.out.println("Investigating cell: "+cell.toCoordinates()+", prob of having: "+probOfHavingMin+", probOfReaching: "+probOfReaching + ", Path: "+path.getPath()+", Unknowns: "+path.getNumUnknown()+", Explored: "+path.getNumExplored());    
    }
}
            }
        }      
if(debug){        
    if(result.isEmpty()){
        printTabs(level);
        System.out.println("None of the cell has "+type);
    }        
    for(TravelPlan plan : result){
        printTabs(level);
        System.out.println("Plan - destination at "+plan.getDestination().toCoordinates()+", probability of having="+plan.getProbabilityOfHaving()+", probability of reaching="+plan.getProbabilityOfReaching()+", cost="+plan.getCost()+", sequence: "+plan.getSequence());
    }     
}
        return result;
    }
    
    public TreeSet<TravelPlan> plan(int numCarriedTile, Int2D pos, int step, double timestamp, TravelPath existingPath, int level){
if(debug){
    printTabs(level);
    System.out.println("Planning - at "+pos.toCoordinates()+", within "+step+" steps, at timestamp "+timestamp);     
}
        TreeSet<TravelPlan> result = new TreeSet();
        if(step!=0){
            if(numCarriedTile==0){
                for(int a=1;a<=step;a++){
if(debug){
    printTabs(level+1);
    System.out.println("Granting first movement "+a+" steps");                         
}                    
                    TreeSet<TravelPlan> firstHalfPlans = plan(TWTile.class, pos, a, timestamp, existingPath, level+2);
                    for(TravelPlan firstHalfPlan : firstHalfPlans){
                        TreeSet<TravelPlan> secondHalfPlans = plan(numCarriedTile+1, firstHalfPlan.getDestination(), step-a, timestamp+a, existingPath.join(firstHalfPlan.getPath()), level+2);
                        if(secondHalfPlans.isEmpty()){
                            result.add(firstHalfPlan);
                        } else{
                            for(TravelPlan secondHalfPlan : secondHalfPlans){
                                result.add(firstHalfPlan.join(secondHalfPlan));
                            }
                        }
                    }
                }
            } else if(numCarriedTile>=1 && numCarriedTile<=2){
                for(int a=1;a<=step;a++){
if(debug){
    printTabs(level+1);
    System.out.println("Granting first movement "+a+" steps");                         
}                        
                    TreeSet<TravelPlan> firstHalfPlans = plan(TWTile.class, pos, a, timestamp, existingPath, level+2);
                    for(TravelPlan firstHalfPlan : firstHalfPlans){
                        TreeSet<TravelPlan> secondHalfPlans = plan(numCarriedTile+1, firstHalfPlan.getDestination(), step-a, timestamp+a, existingPath.join(firstHalfPlan.getPath()), level+2);
                        if(secondHalfPlans.isEmpty()){
                            result.add(firstHalfPlan);
                        } else{
                            for(TravelPlan secondHalfPlan : secondHalfPlans){
                                result.add(firstHalfPlan.join(secondHalfPlan));
                            }
                        }
                    }
                }
                for(int a=1;a<=step;a++){
                    TreeSet<TravelPlan> firstHalfPlans = plan(TWHole.class, pos, a, timestamp, existingPath, level+2);
if(debug){
    printTabs(level+1);
    System.out.println("Granting first movement "+a+" steps");                         
}    
                    for(TravelPlan firstHalfPlan : firstHalfPlans){
                        TreeSet<TravelPlan> secondHalfPlans = plan(numCarriedTile-1, firstHalfPlan.getDestination(), step-a, timestamp+a, existingPath.join(firstHalfPlan.getPath()), level+2);
                        if(secondHalfPlans.isEmpty()){
                            result.add(firstHalfPlan);
                        } else{                        
                            for(TravelPlan secondHalfPlan : secondHalfPlans){
                                result.add(firstHalfPlan.join(secondHalfPlan));
                            }
                        }
                    }
                }                
            } else if(numCarriedTile==3){
                for(int a=1;a<=step;a++){
if(debug){
    printTabs(level+1);
    System.out.println("Granting first movement "+a+" steps");                         
}    
                    TreeSet<TravelPlan> firstHalfPlans = plan(TWHole.class, pos, a, timestamp, existingPath, level+2);
                    for(TravelPlan firstHalfPlan : firstHalfPlans){                      
                        TreeSet<TravelPlan> secondHalfPlans = plan(numCarriedTile-1, firstHalfPlan.getDestination(), step-a, timestamp+a, existingPath.join(firstHalfPlan.getPath()), level+2);
                        if(secondHalfPlans.isEmpty()){
                            result.add(firstHalfPlan);
                        } else{                        
                            for(TravelPlan secondHalfPlan : secondHalfPlans){
                                result.add(firstHalfPlan.join(secondHalfPlan));
                            }
                        }
                    }
                }
            } 
        }  
if(debug){
    if(result.isEmpty()){
        printTabs(level);
        System.out.println("No plan found");
    }           
    for(TravelPlan plan : result){
        printTabs(level);
        System.out.println("Plan - destination at "+plan.getDestination().toCoordinates()+", probability of having="+plan.getProbabilityOfHaving()+", probability of reaching="+plan.getProbabilityOfReaching()+", cost="+plan.getCost()+", sequence: "+plan.getSequence());
        printTabs(level);
        System.out.println("Path - " +plan.getPath().getPath());
    }        
}        
        return result;
    }    
    private boolean onPath(int x, int y, TravelPath path){
        for(Int2D point : path.getPath()){
            if(x==point.x && y==point.y){
                return true;
            }
        }
        return false;
    }
    

private void printTabs(int n){
    for(int i=0;i<n;i++){
        System.out.print("\t");
    }
}         
    
}