package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.util.*;

/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {
    
    private State.StateView state;
    private double savedUtility;
    private int myPlayerNum = 0;
    private int enemyPlayerNum = 1;
    // Lists of units
    List<Integer> myUnitIds;
    List<Integer> enemyUnitIds;
    List<Integer> resourceIDs;
    List<Integer> archerIds = new ArrayList<Integer>();
    List<Integer> footmanIds = new ArrayList<Integer>();
    private int xExtent;
    private int yExtent;
    private int turn;
    
    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     *
     * You may find the following state methods useful:
     *
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIDs(): returns all of the obstacles in the map
     * state.getResourceNode(Integer resourceID): Return a ResourceView for the given ID
     *
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     *
     * For a given unit you will need to find the attack damage, range and max HP
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit deals
     * unitView.getTemplateView().getBaseHealth(): The maximum amount of health of this unit
     *
     * @param state Current state of the episode
     */
    public GameState(State.StateView state) {
        this.state = state;
        myUnitIds = state.getUnitIds(myPlayerNum);
        enemyUnitIds = state.getUnitIds(enemyPlayerNum);
        resourceIDs = state.getAllResourceIds();
        populateIdLists(myUnitIds);
        populateIdLists(enemyUnitIds);
        this.xExtent = state.getXExtent();
        this.yExtent = state.getYExtent();
        this.turn = state.getTurn;
    }
    
    /**
     * Populates UnitId lists
     * @author Previn Kumar
     */
    private void populateIdLists(List<Integer> unitIds) {
        // Classifies each unit ID as a Footman, or Archer
        for(Integer unitID : unitIds) {
            Unit.UnitView unit = state.getUnit(unitID);
            String unitTypeName = unit.getTemplateView().getName();
            if (unitTypeName.equals("Footman")) {
                footmanIds.add(unitID);
            }
            else if (unitTypeName.equals("Archer")) {
                archerIds.add(unitID);
            }
            else {
                System.err.println("Unexpected Unit type: " + unitTypeName);
            }
        }
    }

    /**
     * You will implement this function.
     *
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     *
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     *
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     * @author Minhal Gardezi
     */
    public double getUtility() {
        for(int i: footmanIds){
            
        }
        return 0.0;
    }
    
    /**
     * Returns the saved utility of the GameState
     */
    public double getSavedUtility() {
        return savedUtility;
    }
    
    /**
     * Sets the saved utility
     */
    public void setSavedUtility(double util) {
        savedUtility = util;
    }

    /**
     * You will implement this function.
     *
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     *
     * You may find it useful to iterate over all the different directions in SEPIA.
     *
     * for(Direction direction : Directions.values())
     *
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     *
     * @return All possible actions and their associated resulting game state
     * @author Minhal Gardezi
     */
    public List<GameStateChild> getChildren() {
        for (Direction direction : Direction.values()) {
            
        }
        
        return null;
    }

    /**
     * Returns true if the archers are dead
     */
    public boolean areArchersDead() {
        return archerIds.size() == 0;
    }
}
