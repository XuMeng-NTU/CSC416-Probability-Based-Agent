/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.util;

import java.util.List;
import tileworld.agent.TWAgentPercept;
import tileworld.environment.TWEntity;
import tileworld.environment.TWHole;
import tileworld.environment.TWObstacle;
import tileworld.environment.TWTile;

/**
 *
 * @author Xu Meng
 */
public class PrintUtil {
    public static void printSensedObjectGrid(TWEntity[][] sensedObjectsGrid){
        for(int y=0;y<sensedObjectsGrid[0].length;y++){
            for(int x=0;x<sensedObjectsGrid.length;x++){
                if (sensedObjectsGrid[x][y] == null) {
                    System.out.print("n ");
                } else if (sensedObjectsGrid[x][y] instanceof TWHole) {
                    System.out.print("h ");
                } else if (sensedObjectsGrid[x][y] instanceof TWTile) {
                    System.out.print("t ");
                } else if (sensedObjectsGrid[x][y] instanceof TWObstacle) {
                    System.out.print("o ");
                }
            }
            System.out.println("");
        }
        System.out.println("");
    }
    public static void printCommunicatedMessage(List<TWAgentPercept> messages){
        for(TWAgentPercept percept : messages){
            System.out.print(percept.getO()+" ");
            System.out.println(percept.getO().getX()+","+percept.getO().getY());
        }
    }
}
