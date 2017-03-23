package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {
    
    // The parent GameState of this GameState instance
    private GameState parent;
    
    // The Strips Action taken to reach this state from the parent
    private StripsAction action;
    
    private State.StateView state;
    private double cost;
    private int playernum;
    private int requiredGold;
    private int requiredWood;
    private int woodLeft;
    private int goldLeft;
    private int numPeasants;
    private ArrayList<Integer> peasants;
    private ArrayList<Integer> townhall;
    private ArrayList<Integer> forests;
    private ArrayList<Integer> mines;
    
    private boolean buildPeasants;
    
    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        this.state = state;
        this.playernum = playernum;
        this.requiredGold = requiredGold;
        this.requiredWood = requiredWood;
        this.buildPeasants = buildPeasants;
        
        this.numPeasants = 0;
        this.cost = 0;
        for(int ID:state.getUnitIds(playernum)){
            UnitView unit = state.getUnit(ID);
            String type = unit.getTemplateView().getName().toLowerCase();
            
            if(type.equals("townhall")){
                townhall.add(ID);
            }
            
            if(type.equals("forest")){
                forests.add(ID);
            }
            
            if(type.equals("peasant")){
                peasants.add(ID);
            }
            
            if(type.equals("goldmine")){
                mines.add(ID);
            }
        }
        
    }
    
    public GameState(GameState parentState, StripsAction action){
        this(parentState.state, parentState.getPlayerNum(), parentState.getRequiredGold(), parentState.getRequiredWood(), parentState.buildPeasants);
        numPeasants = parentState.peasants.size();
        this.cost = parentState.cost;
    }

    
    /**
     * Sets the parent of this GameState
     */
    public void setParent(GameState parent) {
        this.parent = parent;
    }
    
    public GameState getParent() {
        return parent;
    }
    
    public void setStripsActions(StripsAction action) {
        this.action = action;
    }
    
    public StripsAction getStripsAction() {
        return action;
    }
    
    public State.StateView getStateView() {
        return state;
    }
    
    public int getPlayerNum() {
        return playernum;
    }
    
    public int getRequiredGold() {
        return requiredGold;
    }
    
    public int getRequiredWood() {
        return requiredWood;
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        int woodTotal = state.getResourceAmount(0, ResourceType.WOOD);
        int goldTotal = state.getResourceAmount(0, ResourceType.GOLD);
        return (requiredWood <= woodTotal) && (requiredGold <= goldTotal);
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        // TODO: Implement me!
        List<GameState> children = new ArrayList<GameState>();
        return null;
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     * @author Previn Kumar
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
        int woodTotal = state.getResourceAmount(0, ResourceType.WOOD);
        int goldTotal = state.getResourceAmount(0, ResourceType.GOLD);
        
        return (requiredWood + requiredGold) - (woodTotal + goldTotal);
    }
    
    /**
     * @return distance to nearest target forest, mine, or townhall
     */
    public int heuristicDistanceToLocation() {
        Unit.UnitView peasant = state.getUnit(peasants.get(0));
        Position peasantPos = new Position(peasant.getXPosition(), peasant.getYPosition());
        int goldTotal = state.getResourceAmount(0, ResourceType.GOLD);
        // find distance to mine or forest if empty handed or townhall to deposit
        if (peasant.getCargoAmount() == 0) {
            if (goldTotal < getRequiredGold()) {
                return distanceToNearestUnitInList(peasantPos, mines);
            }
            else {
                return distanceToNearestUnitInList(peasantPos, forests);
            }
        }
        else {
            return distanceToNearestUnitInList(peasantPos, townhall);
        }
    }
    
    private int distanceToNearestUnitInList(Position peasantPos, ArrayList<Integer> target_list) {
        double min_dist = Double.POSITIVE_INFINITY;
        for (int targetId : target_list) {
            Unit.UnitView target = state.getUnit(targetId);
            Position targetPos = new Position(target.getXPosition(), target.getYPosition());
            min_dist = min_dist > peasantPos.euclideanDistance(targetPos) ? peasantPos.euclideanDistance(targetPos) : min_dist;
        }
        return (int)min_dist;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        //Cost is maintained from parent to children and update during "apply" in stripsAction
        return this.cost;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        if(this.getCost() < o.getCost()){
            return -1;
        }
        
        else if(this.getCost() > o.getCost()){
            return 1;
        }
        
        else{
            return 1;
        }
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        
        return this.hashCode() == o.hashCode();
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        // TODO: Implement me!
        return 0;
    }
}
