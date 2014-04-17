package tileworld.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sim.engine.Schedule;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.IntBag;
import tileworld.environment.NeighbourSpiral;
import tileworld.Parameters;
import tileworld.agent.enhanced.DerivedConstants;
import tileworld.environment.TWDirection;
import tileworld.environment.TWEntity;


import tileworld.environment.TWHole;
import tileworld.environment.TWObject;
import tileworld.environment.TWObstacle;
import tileworld.environment.TWTile;
import tileworld.environment.enhanced.TWEmptyCell;

/**
 * TWAgentMemory
 * 
 * @author michaellees
 * 
 *         Created: Apr 15, 2010 Copyright michaellees 2010
 * 
 *         Description:
 * 
 *         This class represents the memory of the TileWorld agents. It stores
 *         all objects which is has observed for a given period of time. You may
 *         want to develop an entirely new memory system or extend/modify this one.
 * 
 *         The memory is supposed to have a probabilistic decay, whereby an element is
 *         removed from memory with a probability proportional to the length of
 *         time the element has been in memory. The maximum length of time which
 *         the agent can remember is specified as MAX_TIME. Any memories beyond
 *         this are automatically removed.
 */
public class TWAgentWorkingMemory {

    /**
     * Access to Scedule (TWEnvironment) so that we can retrieve the current timestep of the simulation.
     */
    private Schedule schedule;
    private TWAgent me;
    private final static int MAX_TIME = 10;
    private final static float MEM_DECAY = 0.5f;
    
     private ObjectGrid2D memoryGrid;
    
    /*
     * This was originally a queue ordered by the time at which the fact was observed.
     * However, when updating the memory a queue is very slow.
     * Here we trade off memory (in that we maintian a complete image of the map)
     * for speed of update. Updating the memory is a lot more straightfoward.
     */
    private TWAgentPercept[][] objects;
    /**
     * Number of items recorded in memory, currently doesn't decrease as memory
     * is not degraded - nothing is every removed!
     */
    private int memorySize;

    /**
     * Stores (for each TWObject type) the closest object within sensor range,
     * null if no objects are in sensor range
     */
    private HashMap<Class<?>, TWEntity> closestInSensorRange;
    static private List<Int2D> spiral = new NeighbourSpiral(Parameters.defaultSensorRange * 4, 1, TWDirection.E, false).spiral();
//    private List<TWAgent> neighbouringAgents = new ArrayList<TWAgent>();

    private List<TWAgentPercept> messages;
    private TWAgentPercept target;
    
    public TWAgentWorkingMemory(TWAgent moi, Schedule schedule, int x, int y) {

        closestInSensorRange = new HashMap<Class<?>, TWEntity>(4);
        this.me = moi;

        this.objects = new TWAgentPercept[x][y];

        this.schedule = schedule;
        this.memoryGrid = new ObjectGrid2D(me.getEnvironment().getxDimension(), me.getEnvironment().getyDimension());
    }

    /**
     * Called at each time step, updates the memory map of the agent.
     * Note that some objects may disappear or be moved, in which case part of
     * sensed may contain null objects
     *
     * Also note that currently the agent has no sense of moving objects, so
     * an agent may remember the same object at two locations simultaneously.
     * 
     * Other agents in the grid are sensed and passed to this function. But it
     * is currently not used for anything. Do remember that an agent sense itself
     * too.
     *
     * @param sensedObjects bag containing the sensed objects
     * @param objectXCoords bag containing x coordinates of objects
     * @param objectYCoords bag containing y coordinates of object
     * @param sensedAgents bag containing the sensed agents
     * @param agentXCoords bag containing x coordinates of agents
     * @param agentYCoords bag containing y coordinates of agents
    */
    public void updateMemory(Bag sensedObjects, IntBag objectXCoords, IntBag objectYCoords, Bag sensedAgents, IntBag agentXCoords, IntBag agentYCoords, int xPos, int yPos) {
        
        messages = new ArrayList();
        
        assert (sensedObjects.size() == objectXCoords.size() && sensedObjects.size() == objectYCoords.size());

        decayMemory();
        
        closestInSensorRange = new HashMap<Class<?>, TWEntity>(4);

        TWEntity[][] sensedObjectsGrid = new TWEntity[2*Parameters.defaultSensorRange+1][2*Parameters.defaultSensorRange+1];

        for (int i = 0; i < sensedObjects.size(); i++) {
            TWEntity o = (TWEntity) sensedObjects.get(i);
       
            if (!(o instanceof TWObject)) {
                continue;
            }            

            updateClosest(o);
            sensedObjectsGrid[objectXCoords.get(i)-xPos+Parameters.defaultSensorRange][objectYCoords.get(i)-yPos+Parameters.defaultSensorRange] = o;
        }
        for(int x=0;x<sensedObjectsGrid.length;x++){
            for(int y=0;y<sensedObjectsGrid[x].length;y++){
                int worldX = xPos-Parameters.defaultSensorRange+x;
                int worldY = yPos-Parameters.defaultSensorRange+y;
                
                if(inBoundary(worldX, worldY)){
                
                    if(objects[worldX][worldY]==null){

                        if(sensedObjectsGrid[x][y]!=null) {
                            // object sensed
                            memoryGrid.set(worldX, worldY, sensedObjectsGrid[x][y]);
                            int expectedTTL = 0;
                            if(sensedObjectsGrid[x][y] instanceof TWTile){
                                expectedTTL = DerivedConstants.EXPECTED_LIFE_TIME_OF_TILE;
                            } else if(sensedObjectsGrid[x][y] instanceof TWHole){
                                expectedTTL = DerivedConstants.EXPECTED_LIFE_TIME_OF_HOLE;
                            } else if(sensedObjectsGrid[x][y] instanceof TWObstacle){
                                expectedTTL = DerivedConstants.EXPECTED_LIFE_TIME_OF_OBSTACLE;
                            }
                            TWAgentPercept percept = new TWAgentPercept(sensedObjectsGrid[x][y], this.getSimulationTime(), expectedTTL, true);
                            objects[worldX][worldY] = percept;
                            messages.add(percept);
                        } else{
                            // empty cell sensed
                            TWEmptyCell cell = new TWEmptyCell(worldX, worldY);
                            memoryGrid.set(worldX, worldY, cell);
                            TWAgentPercept percept = new TWAgentPercept(cell, this.getSimulationTime(), DerivedConstants.EXPECTED_LIFE_TIME_OF_EMPTY_CELLS, true);
                            objects[worldX][worldY] = percept;
                            messages.add(percept);
                        }
                    } else{
                        // status known in memory
                        if(objects[worldX][worldY].getO() instanceof TWEmptyCell && sensedObjectsGrid[x][y]!=null){
                            // a newly appeared object detected
                            TWAgentPercept percept = new TWAgentPercept(sensedObjectsGrid[x][y], this.getSimulationTime(), DerivedConstants.LIFE_TIME, false);
                            objects[worldX][worldY] = percept;
                            messages.add(percept);
                            memoryGrid.set(worldX, worldY, sensedObjectsGrid[x][y]);
                        } else if( !(objects[worldX][worldY].getO() instanceof TWEmptyCell) && sensedObjectsGrid[x][y]==null){
                            // an object disappeared
                            TWEmptyCell cell = new TWEmptyCell(worldX, worldY);
                            TWAgentPercept percept = new TWAgentPercept(cell, this.getSimulationTime(), DerivedConstants.EXPECTED_LIFE_TIME_OF_EMPTY_CELLS, true);
                            objects[worldX][worldY] = percept;
                            messages.add(percept);
                            memoryGrid.set(worldX, worldY, cell);
                        } else if(objects[worldX][worldY].getO() instanceof TWEmptyCell && sensedObjectsGrid[x][y]==null){
                            // empty cell remains empty
                            TWAgentPercept percept = objects[worldX][worldY];
                            messages.add(percept);
                        } else if(!objects[worldX][worldY].getO().equals(sensedObjectsGrid[x][y])){
                            // a cell is changed!
                            TWAgentPercept percept = new TWAgentPercept(sensedObjectsGrid[x][y], this.getSimulationTime(), DerivedConstants.LIFE_TIME, false);
                            objects[worldX][worldY] = percept;
                            messages.add(percept);
                            memoryGrid.set(worldX, worldY, sensedObjectsGrid[x][y]);
                        } else{
                            // a non=empty cell is still occupied by the same object
                            TWAgentPercept percept = objects[worldX][worldY];
                            messages.add(percept);
                        }
                    }
                }
            }         
        }
    }

    public void updateMemoryForMessage(List<TWAgentPercept> messages){
        for(TWAgentPercept percept : messages){
            memoryGrid.set(percept.getO().getX(), percept.getO().getY(), percept.getO());
            objects[percept.getO().getX()][percept.getO().getY()] = percept;
        }
    }
    
    public void updateMemoryForTarget(TWAgentPercept target){
        if(target!=null){
            memoryGrid.set(target.getO().getX(), target.getO().getY(), null);
            objects[target.getO().getX()][target.getO().getY()] = null;
        }
    }
    /**
     * removes all facts earlier than now - max memory time. 
     * TODO: Other facts are
     * remove probabilistically (exponential decay of memory)
     */
    public void decayMemory() {
        for(int x=0;x<objects.length;x++){
            for(int y=0;y<objects[x].length;y++){
                if(objects[x][y]!=null){
                    if (this.getSimulationTime() - objects[x][y].getT() >= objects[x][y].getTTL()) {
                        memoryGrid.set(x, y, null);
                        objects[x][y] = null;
                    }
                }
            }
        }
    }


    public void removeAgentPercept(int x, int y){
        objects[x][y] = null;
    }


    public void removeObject(TWEntity o){
        removeAgentPercept(o.getX(), o.getY());
    }

    public List<TWAgentPercept> getMessages() {
        return messages;
    }

    public TWAgentPercept getTarget() {
        return target;
    }

    public void setTarget(int x, int y) {
        this.target = objects[x][y];
    }

    public void nullTarget() {
        this.target = null;
    }
    /**
     * @return
     */
    public double getSimulationTime() {
        return schedule.getTime();
    }

    /**
     * Finds a nearby tile we have seen less than threshold timesteps ago
     *
     * @see TWAgentWorkingMemory#getNearbyObject(int, int, double, java.lang.Class)
     */
    public TWTile getNearbyTile(int x, int y, double threshold) {
        return (TWTile) this.getNearbyObject(x, y, threshold, TWTile.class);
    }

    /**
     * Finds a nearby hole we have seen less than threshold timesteps ago
     *
     * @see TWAgentWorkingMemory#getNearbyObject(int, int, double, java.lang.Class)
     */
    public TWHole getNearbyHole(int x, int y, double threshold) {
        return (TWHole) this.getNearbyObject(x, y, threshold, TWHole.class);
    }


    /**
     * Returns the number of items currently in memory
     */ 
    public int getMemorySize() {
        return memorySize;
    }

    public boolean inBoundary(int x, int y){
        return me.getEnvironment().isInBounds(x, y);
    }
    
    public boolean[] inOptimalBoundary(int x, int y){
        boolean[] result = new boolean[2];
        result[0] = x>=Parameters.defaultSensorRange && x<me.getEnvironment().getxDimension() - Parameters.defaultSensorRange;
        result[1] = y>=Parameters.defaultSensorRange && y<me.getEnvironment().getyDimension() - Parameters.defaultSensorRange;
    
        return result;
    }
    
    /**
     * Returns the nearest object that has been remembered recently where recently
     * is defined by a number of timesteps (threshold)
     *
     * If no Object is in memory which has been observed in the last threshold
     * timesteps it returns the most recently observed object. If there are no objects in
     * memory the method returns null. Note that specifying a threshold of one
     * will always return the most recently observed object. Specifying a threshold
     * of MAX_VALUE will always return the nearest remembered tile.
     *
     * Also note that it is likely that nearby objects are also the most recently observed
     *
     *
     * @param x coordinate from which to check for tiles
     * @param y coordinate from which to check for tiles
     * @param threshold how recently we want to have see the object
     * @param type the class of object we're looking for (Must inherit from TWObject, specifically tile or hole)
     * @return
     */
    private TWObject getNearbyObject(int sx, int sy, double threshold, Class<?> type) {

        //If we cannot find an object which we have seen recently then we want
        //the one with maxTimestamp
        double maxTimestamp = 0;
        TWObject o = null;
        double time = 0;
        TWObject ret = null;
        int x, y;
        for (Int2D offset : spiral) {
            x = offset.x + sx;
            y = offset.y + sy;

            if (me.getEnvironment().isInBounds(x, y) && objects[x][y] != null && !(objects[x][y].getO() instanceof TWEmptyCell)) {
                o = (TWObject) objects[x][y].getO();//get mem object
                if (type.isInstance(o)) {//if it's not the type we're looking for do nothing

                    time = objects[x][y].getT();//get time of memory

                    if (this.getSimulationTime() - time <= threshold) {
                        //if we found one satisfying time, then return
                        return o;
                    } else if (time > maxTimestamp) {
                        //otherwise record the timestamp and the item in case
                        //it's the most recent one we see
                        ret = o;
                        maxTimestamp = time;
                    }
                }
            }
        }

        //this will either be null or the object of Class type which we have
        //seen most recently but longer ago than now-threshold.
        return ret;
    }

    /**
     * Used for invalidating the plan, returns the object of a particular type
     * (Tile or Hole) which is closest to the agent and within it's sensor range
     *
     * @param type
     * @return
     */
    public TWEntity getClosestObjectInSensorRange(Class<?> type) {
        return closestInSensorRange.get(type);
    }

    private void updateClosest(TWEntity o) {
        assert (o != null);
        if (closestInSensorRange.get(o.getClass()) == null || me.closerTo(o, closestInSensorRange.get(o.getClass()))) {
            closestInSensorRange.put(o.getClass(), o);
        }
    }

    /**
     * Is the cell blocked according to our memory?
     * 
     * @param tx x position of cell
     * @param ty y position of cell
     * @return true if the cell is blocked in our memory
     */
    public boolean isCellBlocked(int tx, int ty, double timestamp) {

        //no memory at all, so assume not blocked
        if (objects[tx][ty] == null) {
            return false;
        }

        if(!(objects[tx][ty].getO() instanceof TWObstacle)){
            return false;
        }
        
        return (timestamp - objects[tx][ty].getT() < objects[tx][ty].getTTL());
    }

    public ObjectGrid2D getMemoryGrid() {
        return this.memoryGrid;
    }
        
    public boolean isObstacle(int tx, int ty){
        return objects[tx][ty]!=null && objects[tx][ty].getO() instanceof TWObstacle;
    }
    
    public boolean isHole(int tx, int ty){
        return objects[tx][ty]!=null && objects[tx][ty].getO() instanceof TWHole;
    }
    
    public boolean isTile(int tx, int ty){
        return objects[tx][ty]!=null && objects[tx][ty].getO() instanceof TWTile;
    }
    
    public boolean isCellUnknown(int tx, int ty, double timestamp) {
        return objects[tx][ty] == null || timestamp - objects[tx][ty].getT() >= objects[tx][ty].getTTL();
    }
    
    public TWAgentPercept getPercept(int x, int y){
        return objects[x][y];
    }
    
    public int countNewlyExplored(int curX, int curY, int prevX, int prevY, double timestamp){
        
        int newCells = 0;
        
        if(curX>prevX && curY==prevY){
            for(int y=0;y<2*Parameters.defaultSensorRange+1;y++){
                if(inBoundary(curX+Parameters.defaultSensorRange, curY-Parameters.defaultSensorRange+y)){
                    if(objects[curX+Parameters.defaultSensorRange][curY-Parameters.defaultSensorRange+y] == null
                            || timestamp - objects[curX+Parameters.defaultSensorRange][curY-Parameters.defaultSensorRange+y].getT() >=objects[curX+Parameters.defaultSensorRange][curY-Parameters.defaultSensorRange+y].getTTL()){
                        newCells++;
                    }
                }
            }
        } else if(curX<prevX && curY==prevY){
            for(int y=0;y<2*Parameters.defaultSensorRange+1;y++){
                if(inBoundary(curX-Parameters.defaultSensorRange, curY-Parameters.defaultSensorRange+y)){
                    if(objects[curX-Parameters.defaultSensorRange][curY-Parameters.defaultSensorRange+y] == null
                            || timestamp - objects[curX-Parameters.defaultSensorRange][curY-Parameters.defaultSensorRange+y].getT()>=objects[curX-Parameters.defaultSensorRange][curY-Parameters.defaultSensorRange+y].getTTL()){
                        newCells++;
                    }
                }
            }
        } else if(curX==prevX && curY>prevY){
            for(int x=0;x<2*Parameters.defaultSensorRange+1;x++){
                if(inBoundary(curX-Parameters.defaultSensorRange+x, curY+Parameters.defaultSensorRange)){
                    if(objects[curX-Parameters.defaultSensorRange+x][curY+Parameters.defaultSensorRange] == null
                            || timestamp - objects[curX-Parameters.defaultSensorRange+x][curY+Parameters.defaultSensorRange].getT()>=objects[curX-Parameters.defaultSensorRange+x][curY+Parameters.defaultSensorRange].getTTL()){
                        newCells++;
                    }
                }
            }
        } else if(curX==prevX && curY<prevY){
            for(int x=0;x<2*Parameters.defaultSensorRange+1;x++){
                if(inBoundary(curX-Parameters.defaultSensorRange+x, curY-Parameters.defaultSensorRange)){
                    if(objects[curX-Parameters.defaultSensorRange+x][curY-Parameters.defaultSensorRange] == null
                            || timestamp - objects[curX-Parameters.defaultSensorRange+x][curY-Parameters.defaultSensorRange].getT()>=objects[curX-Parameters.defaultSensorRange+x][curY-Parameters.defaultSensorRange].getTTL()){
                        newCells++;
                    }
                }
            }
        }
                
        return newCells;
    }    
    public int countEnhancedExplored(int curX, int curY, int prevX, int prevY){
        
        int newCells = 0;
        
        if(curX>prevX && curY==prevY){
            for(int y=0;y<2*Parameters.defaultSensorRange+1;y++){
                if(inBoundary(curX+Parameters.defaultSensorRange, curY-Parameters.defaultSensorRange+y)){
                    if(objects[curX+Parameters.defaultSensorRange][curY-Parameters.defaultSensorRange+y] == null || objects[curX+Parameters.defaultSensorRange][curY-Parameters.defaultSensorRange+y].isExpected()){
                        newCells++;
                    }
                }
            }
        } else if(curX<prevX && curY==prevY){
            for(int y=0;y<2*Parameters.defaultSensorRange+1;y++){
                if(inBoundary(curX-Parameters.defaultSensorRange, curY-Parameters.defaultSensorRange+y)){
                    if(objects[curX-Parameters.defaultSensorRange][curY-Parameters.defaultSensorRange+y] == null || objects[curX-Parameters.defaultSensorRange][curY-Parameters.defaultSensorRange+y].isExpected()){
                        newCells++;
                    }
                }
            }
        } else if(curX==prevX && curY>prevY){
            for(int x=0;x<2*Parameters.defaultSensorRange+1;x++){
                if(inBoundary(curX-Parameters.defaultSensorRange+x, curY+Parameters.defaultSensorRange)){
                    if(objects[curX-Parameters.defaultSensorRange+x][curY+Parameters.defaultSensorRange] == null || objects[curX-Parameters.defaultSensorRange+x][curY+Parameters.defaultSensorRange].isExpected()){
                        newCells++;
                    }
                }
            }
        } else if(curX==prevX && curY<prevY){
            for(int x=0;x<2*Parameters.defaultSensorRange+1;x++){
                if(inBoundary(curX-Parameters.defaultSensorRange+x, curY-Parameters.defaultSensorRange)){
                    if(objects[curX-Parameters.defaultSensorRange+x][curY-Parameters.defaultSensorRange] == null || objects[curX-Parameters.defaultSensorRange+x][curY-Parameters.defaultSensorRange].isExpected()){
                        newCells++;
                    }
                }
            }
        }
        
        return newCells;
    }      
    
    public Double2D getMemoryConcentrationCenter(int xPos, int yPos, int limit){
        int counter = 0;
        double xSum = 0;
        double ySum = 0;
        
        for(int x=0;x<objects.length;x++){
            for(int y=0;y<objects[x].length;y++){
                if(distance(x, y, xPos, yPos)<=limit){
                    if(objects[x][y]!=null){
                        xSum+=x;
                        ySum+=y;
                        counter++;
                    }
                }
            }
        }
        
        if(counter==0){
            return new Double2D(xPos, yPos);
        } else{
            return new Double2D(xSum/counter, ySum/counter);
        }
    }
    
    public int distance(int x, int y, int nx, int ny){
        return Math.abs(x-nx)+Math.abs(y-ny);
    }
    
    public double gDistance(int x, int y, double nx, double ny){
        return Math.sqrt((x-nx)*(x-nx)+(y-ny)*(y-ny));
    }    
}
