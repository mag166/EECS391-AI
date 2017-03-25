package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.util.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper Class to Track PeasantState without messing with the Unit objects
 @author Minhal
 */

public class Peasant {
    
    
    private int unitID;
    private int gold = 0;
    private int wood = 0;
    private Position position;
    
    
    
    public Peasant(int unitID, Position position) {
        this.position = position;
    }
    
    public Peasant(int unitID, Position position, int gold, int wood) {
        this.position = position;
        unitID = unitID;
        this.gold = gold;
        this.wood = wood;
    }
    
    public void updateGold(int gold){
        this.gold = gold;
    }
    
    public void updateWood(int wood){
        this.wood = wood;
    }
    
    public void updatePosition(Position position){
        this.position = position;
    }
    public int getUnitID(){
        return this.unitID;
    }
}
