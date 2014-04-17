/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.util;

import java.util.ArrayList;
import java.util.List;
import sim.util.Int2D;
import tileworld.Parameters;
import tileworld.environment.NeighbourSpiral;
import tileworld.environment.TWDirection;

/**
 *
 * @author Xu Meng
 */
public class SpiralUtil {

    public static List<Int2D> generateSpiral(int r, int steps, TWDirection direction, boolean clockwise, int x, int y){

        List<Int2D> result = new ArrayList();

        List<Int2D> tempSpiral = new NeighbourSpiral(r, steps, direction, clockwise).spiral();

        for(Int2D point : tempSpiral){
            
            int xPos = point.x+x;
            int yPos = point.y+y;
            
            if(xPos>=Parameters.defaultSensorRange && xPos<Parameters.xDimension-Parameters.defaultSensorRange && yPos>=Parameters.defaultSensorRange && yPos<Parameters.yDimension-Parameters.defaultSensorRange){
                result.add(new Int2D(xPos, yPos)); 
for(Int2D p : result){
    System.out.println(p);
}                
            }
        }
        return result;
    }
}
