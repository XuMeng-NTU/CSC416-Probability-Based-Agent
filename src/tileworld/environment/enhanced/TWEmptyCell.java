/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.environment.enhanced;

import java.awt.Color;
import sim.portrayal.Portrayal;
import sim.portrayal.simple.RectanglePortrayal2D;
import tileworld.environment.TWDirection;
import tileworld.environment.TWEntity;
import tileworld.exceptions.CellBlockedException;
import tileworld.exceptions.InsufficientFuelException;

/**
 *
 * @author Xu Meng
 */
public class TWEmptyCell extends TWEntity{

    public TWEmptyCell(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    public static Portrayal getMemoryPortrayal() {
        return new RectanglePortrayal2D(new Color(255, 255, 255), false);

    }

    @Override
    protected void move(TWDirection d) throws InsufficientFuelException, CellBlockedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
