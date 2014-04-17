/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.planners.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import sim.util.Int2D;
import tileworld.agent.TWAgentWorkingMemory;
import tileworld.util.PowerSetUtil;

/**
 *
 * @author Xu Meng
 */
public class TravelPathGenerator {
    private TWAgentWorkingMemory memory;

    public TravelPathGenerator(TWAgentWorkingMemory memory) {
        this.memory = memory;
    }
    
    public TravelPath findMinUnknownPath(int x, int y, int objX, int objY, double timestamp){
        int xDist = Math.abs(objX - x);
        int yDist = Math.abs(objY - y);
        
        int xInc;
        if(objX == x){
            xInc = 0;
        } else{
            xInc = (objX - x) / xDist;
        }
       
        int yInc;
        if(objY == y){
           yInc = 0; 
        } else{
            yInc = (objY - y) / yDist;
        }

        List<Integer> indexes = new ArrayList();
        
        for(int i=0;i<xDist+yDist;i++){
            indexes.add(i);
        }
        
        int minNumUnknowns = Integer.MAX_VALUE;
        int maxNumExplored = Integer.MIN_VALUE;
        List<Int2D> bestPath = null;
        
        List<Set<Integer>> subsets = PowerSetUtil.getSubsets(indexes, xDist);      
        for(Set<Integer> set : subsets){
            int lastX = x;
            int lastY = y;
            List<Int2D> path = new ArrayList();
            int numUnknowns = 0;
            int numExplored = 0;
            for(int i=0;i<xDist + yDist;i++){
                if(set.contains(i)){
                    path.add(new Int2D(lastX+xInc, lastY));
                    lastX = lastX + xInc;
                } else{
                    path.add(new Int2D(lastX, lastY+yInc));
                    lastY = lastY + yInc;
                }
            }
            
            boolean blocked = false;
            
            int prevX = x;
            int prevY = y;
            for(int i=0;i<path.size();i++){
                Int2D point = path.get(i);
                if(memory.isCellBlocked(point.x, point.y, timestamp + i)){
                    blocked = true;
                    break;
                }
                if(memory.isCellUnknown(point.x, point.y, timestamp + i)){
                    numUnknowns ++;
                }       
                
                numExplored+=memory.countNewlyExplored(point.x, point.y, prevX, prevY, timestamp + i);
                
                prevX = point.x;
                prevY = point.y;
            }
            if(blocked){
                continue;
            } else{
                if(numExplored>maxNumExplored){
                    maxNumExplored = numExplored;
                    minNumUnknowns = numUnknowns;
                    bestPath = path;
                } else if(numExplored==maxNumExplored){
                    if(numUnknowns < minNumUnknowns){
                        minNumUnknowns = numUnknowns;
                        bestPath = path;
                    }
                }
            }
        }
        if(bestPath==null){
            return null;
        } else{
            return new TravelPath(xDist + yDist, bestPath, minNumUnknowns, maxNumExplored);
        }
    }
    
    public TravelPath findMaxExploredPath(int x, int y, int objX, int objY, double timestamp){
        int xDist = Math.abs(objX - x);
        int yDist = Math.abs(objY - y);
        
        int xInc;
        if(objX == x){
            xInc = 0;
        } else{
            xInc = (objX - x) / xDist;
        }
       
        int yInc;
        if(objY == y){
           yInc = 0; 
        } else{
            yInc = (objY - y) / yDist;
        }

        List<Integer> indexes = new ArrayList();
        
        for(int i=0;i<xDist+yDist;i++){
            indexes.add(i);
        }
        
        int minNumUnknowns = Integer.MAX_VALUE;
        int maxNumExplored = Integer.MIN_VALUE;
        List<Int2D> bestPath = null;
        
        List<Set<Integer>> subsets = PowerSetUtil.getSubsets(indexes, xDist);      
        for(Set<Integer> set : subsets){
            int lastX = x;
            int lastY = y;
            List<Int2D> path = new ArrayList();
            int numUnknowns = 0;
            int numExplored = 0;
            for(int i=0;i<xDist + yDist;i++){
                if(set.contains(i)){
                    path.add(new Int2D(lastX+xInc, lastY));
                    lastX = lastX + xInc;
                } else{
                    path.add(new Int2D(lastX, lastY+yInc));
                    lastY = lastY + yInc;
                }
            }
            
            boolean blocked = false;
            
            int prevX = x;
            int prevY = y;
            for(int i=0;i<path.size();i++){
                Int2D point = path.get(i);
                if(memory.isCellBlocked(point.x, point.y, timestamp + i)){
                    blocked = true;
                    break;
                }
                if(memory.isCellUnknown(point.x, point.y, timestamp + i)){
                    numUnknowns ++;
                }       
                
                numExplored+=memory.countNewlyExplored(point.x, point.y, prevX, prevY, timestamp + i);
                
                prevX = point.x;
                prevY = point.y;
            }
            if(blocked){
                continue;
            } else{
                if(numUnknowns<minNumUnknowns){
                    minNumUnknowns = numUnknowns;
                    maxNumExplored = numExplored;
                    bestPath = path;
                } else if(numUnknowns==minNumUnknowns){
                    if(numExplored > maxNumExplored){
                        maxNumExplored = numExplored;
                        bestPath = path;
                    }
                }
            }
        }
        if(bestPath==null){
            return null;
        } else{
            return new TravelPath(xDist + yDist, bestPath, minNumUnknowns, maxNumExplored);
        }
    }

}
