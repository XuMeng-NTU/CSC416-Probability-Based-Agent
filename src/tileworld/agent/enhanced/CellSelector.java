/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent.enhanced;

import java.util.ArrayList;
import java.util.List;
import sim.util.Int2D;
import tileworld.agent.TWAgentWorkingMemory;

/**
 *
 * @author Xu Meng
 */
public class CellSelector {
    
    public TWAgentWorkingMemory memory;

    public CellSelector(TWAgentWorkingMemory memory) {
        this.memory = memory;
    }
    
    public List<Int2D> cellsWithinStep(int x, int y, int step){
        List<Int2D> result = new ArrayList();
        
        for(int i=-step;i<=step;i++){
            int dx = i;
            int dy1 = Math.abs(dx)-step;
            int dy2 = step - Math.abs(dx);
            
            if(memory.inBoundary(x+dx, y+dy1)){
                result.add(new Int2D(x+dx, y+dy1));
            }
            
            if(dy1!=dy2 && memory.inBoundary(x+dx, y+dy2)){
                result.add(new Int2D(x+dx, y+dy2));
            }
            
        }
        return result;
    }
}
