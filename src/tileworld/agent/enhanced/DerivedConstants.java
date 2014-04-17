/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent.enhanced;

import tileworld.Parameters;

/**
 *
 * @author Xu Meng
 */
public class DerivedConstants {
    
    // Config 1
//    public final static double THREHOLD = 0.3;
//    public final static double TILE_VALUE = 0.5;
//    public final static double HOLE_VALUE = 1-TILE_VALUE;
//    public final static int OPTIMAL_STEPS = 7;
//    public static double FUEL_SAFETY_MARGIN = 1.5;
//    
//    private final static double TILE_LIFE_TIME_CONSERVATIVENESS = 0.4;
//    private final static double HOLE_LIFE_TIME_CONSERVATIVENESS = 0.4;
//    private final static double OBSTACLE_LIFE_TIME_CONSERVATIVENESS = 0.7;
//    private final static double CONSERVATIVENESS = 0.995;
    
    // Config 3
//    public final static double THREHOLD = 0.3;
//    public final static double TILE_VALUE = 0.9;
//    public final static double HOLE_VALUE = 1-TILE_VALUE;
//    public final static int OPTIMAL_STEPS = 9;
//    public static double FUEL_SAFETY_MARGIN = 0.1;
//    
//    private final static double TILE_LIFE_TIME_CONSERVATIVENESS = 0.4;
//    private final static double HOLE_LIFE_TIME_CONSERVATIVENESS = 0.4;
//    private final static double OBSTACLE_LIFE_TIME_CONSERVATIVENESS = 0.7;
//    private final static double CONSERVATIVENESS = 0.9925;
    
    //Config 2
    
    public final static double THREHOLD = 0.4;
    public final static double TILE_VALUE = 0.5;
    public final static double HOLE_VALUE = 1-TILE_VALUE;
    public final static int OPTIMAL_STEPS = 7;
    public static double FUEL_SAFETY_MARGIN = 1.5;
    
    private final static double TILE_LIFE_TIME_CONSERVATIVENESS = 0.4;
    private final static double HOLE_LIFE_TIME_CONSERVATIVENESS = 0.4;
    private final static double OBSTACLE_LIFE_TIME_CONSERVATIVENESS = 0.7;
    private final static double CONSERVATIVENESS = 0.925;
    
    public final static int LIFE_TIME = Parameters.lifeTime;
    public final static int EXPECTED_LIFE_TIME_OF_TILE = (int) Math.floor((Parameters.lifeTime * TILE_LIFE_TIME_CONSERVATIVENESS));
    public final static int EXPECTED_LIFE_TIME_OF_HOLE = (int) Math.floor((Parameters.lifeTime * HOLE_LIFE_TIME_CONSERVATIVENESS));
    public final static int EXPECTED_LIFE_TIME_OF_OBSTACLE = (int) Math.floor((Parameters.lifeTime * OBSTACLE_LIFE_TIME_CONSERVATIVENESS));
    public final static int EXPECTED_LIFE_TIME_OF_EMPTY_CELLS = calculateExpectedLifeTimeOfEmptyCells();
    public final static int EXPECTED_NUM_OF_EMPTY_CELLS_WITHOUT_OBJECT = calculateNumOfEmptyCellsWithoutObject();
    public static final int AVERAGE_STEP_TO_TILE = (int)(-1 + Math.sqrt(2*Math.log(1-CONSERVATIVENESS)/Math.log(1-Parameters.tileMean*Parameters.lifeTime/(Parameters.xDimension*Parameters.yDimension))))/2;
    public static final int AVERAGE_STEP_TO_HOLE = (int)(-1 + Math.sqrt(2*Math.log(1-CONSERVATIVENESS)/Math.log(1-Parameters.holeMean*Parameters.lifeTime/(Parameters.xDimension*Parameters.yDimension))))/2;
    public static final int AVERAGE_STEP_TO_OBSTACLE = (int)(-1 + Math.sqrt(2*Math.log(1-CONSERVATIVENESS)/Math.log(1-Parameters.obstacleMean*Parameters.lifeTime/(Parameters.xDimension*Parameters.yDimension))))/2;  
    
    private static int calculateExpectedLifeTimeOfEmptyCells(){
        double densityOfTile = Parameters.tileMean/(Parameters.xDimension * Parameters.yDimension - Parameters.lifeTime*(Parameters.tileMean+Parameters.holeMean+Parameters.obstacleMean));
        double densityOfHole = Parameters.holeMean/(Parameters.xDimension * Parameters.yDimension - Parameters.lifeTime*(Parameters.tileMean+Parameters.holeMean+Parameters.obstacleMean));
        double densityOfObstacle = Parameters.obstacleMean/(Parameters.xDimension * Parameters.yDimension - Parameters.lifeTime*(Parameters.tileMean+Parameters.holeMean+Parameters.obstacleMean));
        return (int) Math.floor(Math.log(CONSERVATIVENESS) / Math.log((1-densityOfTile) * (1-densityOfHole) * (1-densityOfObstacle)));
    }
    
    private static int calculateNumOfEmptyCellsWithoutObject(){
        double densityOfTile = Parameters.lifeTime * Parameters.tileMean/(Parameters.xDimension * Parameters.yDimension);
        double densityOfHole = Parameters.lifeTime * Parameters.holeMean/(Parameters.xDimension * Parameters.yDimension);
        double densityOfObstacle = Parameters.lifeTime * Parameters.obstacleMean/(Parameters.xDimension * Parameters.yDimension);
        return (int) Math.floor(Math.log(CONSERVATIVENESS) / Math.log((1-densityOfTile) * (1-densityOfHole) * (1-densityOfObstacle)));
    }
    
    public static void printConstants(){
        System.out.println("THREHOLD="+THREHOLD);
        System.out.println("TILE_VALUE="+TILE_VALUE);
        System.out.println("HOLE_VALUE="+HOLE_VALUE);
        System.out.println("OPTIMAL_STEPS="+OPTIMAL_STEPS);
        System.out.println("TILE_LIFE_TIME_CONSERVATIVENESS="+TILE_LIFE_TIME_CONSERVATIVENESS);
        System.out.println("HOLE_LIFE_TIME_CONSERVATIVENESS="+HOLE_LIFE_TIME_CONSERVATIVENESS);
        System.out.println("OBSTACLE_LIFE_TIME_CONSERVATIVENESS="+OBSTACLE_LIFE_TIME_CONSERVATIVENESS);
        System.out.println("CONSERVATIVENESS="+CONSERVATIVENESS);
        System.out.println("FUEL_SAFETY_MARGIN="+FUEL_SAFETY_MARGIN);
    }
    
}
