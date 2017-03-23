package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;

public class MoveAction implements StripsAction{
    Unit.UnitView unit;
    Position toPosition;
    
    public MoveAction(Unit.UnitView unit, Position toPosition){
        this.unit = unit;
        this.toPosition = toPosition
    }
    
    
    @Override
    public boolean preconditionsMet(GameState state) {
        Position unitPos = new Position(unit.getXPosition(), unit.getYPosition());
        if(unitPos.equals(toPosition)){
            return false;
        }
        
        return true;
    }
    

}

