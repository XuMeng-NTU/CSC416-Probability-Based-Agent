/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent.enhanced;

import tileworld.Parameters;
import tileworld.environment.TWHole;
import tileworld.environment.TWObstacle;
import tileworld.environment.TWTile;
import tileworld.planners.enhanced.TravelPath;
import tileworld.util.CombinatoricsUtil;

/**
 *
 * @author Xu Meng
 */
public class ProbabilityCalculator {
    
    public static int inverseProbabilityHavingLessThan(Class type, int n, double threhold){

        int k=0;
        double sum = 0;

        while(sum<threhold && k<n){
            sum = sum+probabilityExact(type, k, n);
            k++;
        }
        
        return k-1;
    }
    
    public static double probabilityExact(Class type, int k, int n){
        double creationMean = retrieveParameter(type);
        double density = creationMean * Parameters.lifeTime / (Parameters.xDimension * Parameters.yDimension - Parameters.lifeTime*(Parameters.tileMean+Parameters.holeMean+Parameters.obstacleMean-creationMean));
        
        return CombinatoricsUtil.C(n, k) * Math.pow(density, k) * Math.pow(1-density, n-k);
    }
    
    public static double probabilityAtLeast(Class type, int k, int n){
        double result = 1;
        for(int i=0;i<k;i++){
            result = result - probabilityExact(type, i, n);
        }
        
        return result;
    }
    
    public static double probabilityOfReaching(TravelPath path, int step){
        double result = 0;
        for(int i=0;i<=path.getNumUnknown();i++){
            if(path.getDistance()+2*i<=step){
                result+=probabilityExact(TWObstacle.class, i, path.getNumUnknown());
            } else{
                break;
            }
        }
        return result;
    }
    
    public static double probabilityOfHavingMin(TravelPath path, int minNum, Class type){
        return probabilityAtLeast(type, minNum, path.getNumExplored());
    }
    
    private static double retrieveParameter(Class type){
        if(type.equals(TWTile.class)){
            return Parameters.tileMean;
        } else if(type.equals(TWHole.class)){
            return Parameters.holeMean;
        } else if(type.equals(TWObstacle.class)){
            return Parameters.obstacleMean;
        }
        return 0;
    }
}
