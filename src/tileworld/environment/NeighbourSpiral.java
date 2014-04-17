/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.environment;

import java.util.ArrayList;
import java.util.List;
import sim.util.Int2D;

/**
 * NeighbourSpiral
 *
 * @author michaellees Created: Apr 26, 2010
 *
 * Copyright Michael lees 2010
 *
 * Description:
 *
 * Used in the agent memory to work out the nearest object from the current
 * location. Essentially spirals around a central square using the TWDirection.
 *
 */
public class NeighbourSpiral {

    Int2D point;
    TWDirection direction;
    int steps;
    private List<Int2D> list = new ArrayList<Int2D>();
    int maxRadius;
    boolean clockwise;

    public NeighbourSpiral(int maxRadius, int steps, TWDirection direction, boolean clockwise) {
        this.maxRadius = maxRadius;
        this.steps = steps;
        this.direction = direction;
        this.clockwise = clockwise;
        list = spiral();
    }

    public List<Int2D> spiral() {
        point = TWDirection.ORIGIN;
        int steps = 1;
        while (steps <= maxRadius * 2 / this.steps) {
            advance(steps);
            advance(steps);
            steps++;
        }
        list.add(point);
        return list;
    }

    private void advance(int n) {
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < steps; j++) {
                list.add(point);
                point = direction.advance(point);
            }
        }
        if(!clockwise){
            direction = direction.next();
        } else{
            direction = direction.next().next().next();
        }
    }
}
