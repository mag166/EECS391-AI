package edu.cwru.sepia.agent.planner.actions;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.Unit;

public class MoveAction implements StripsAction{
    Unit.UnitView unit;
    Position toPosition;
    Position origin;
    
    public MoveAction(Unit.UnitView unit, Position toPosition){
        this.unit = unit;
        this.toPosition = toPosition;
        this.origin = new Position(unit.getXPosition(), unit.getYPosition());
    }
    
    
    @Override
    public boolean preconditionsMet(GameState state) {
        updateOrigin();
        if(origin.equals(toPosition)){
            return false;
        }
        
        
        return true;
    }
    
    public void updateOrigin(){
        this.origin = new Position(unit.getXPosition(), unit.getYPosition());
    }
    
    /**
     * Applys action to given state
     * @param state
     */
    @Override
    public GameState apply(GameState state) {
        updateOrigin();
        double cost = this.origin.euclideanDistance(toPosition);
        GameState returnState = new GameState(state, this);
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * Other needed updates to GameState
     * @param state
     */
    @Override
    public double getCost(){
        return this.origin.euclideanDistance(toPosition);
    }
    
    
}

