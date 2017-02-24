package edu.cwru.sepia.agent.minimax;


import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.State.StateBuilder;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.UnitTemplate;
import edu.cwru.sepia.environment.model.state.UnitTemplate.UnitTemplateView;
import edu.cwru.sepia.util.Direction;
import edu.cwru.sepia.util.DistanceMetrics;

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
        this.turn = state.getTurnNumber();
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
     *
     *
     * Calculate the utility based on total footmanHP, archerHP, number of footmen/archers,
     *  number of trees blocking the footmen and average distance between footmen and nearest archer
     *
     * @return The weighted linear combination of the features
     * @author Minhal Gardezi
     */
    public double getUtility() {
        //Calculate information on the state to create utility
        double footmanHP = 0;
        double archerHP = 0;
        //int treeUtil = 0;
        int numFootman = footmanIds.size();
        int numArcher = archerIds.size();
        double averageDistance = 0;
        
        for(int i: footmanIds){
            footmanHP += state.getUnit(i).getHP()/state.getUnit(i).getTemplateView().getBaseHealth();
            averageDistance += shortestDistance(i);
            
        }
        
        for(int i: archerIds){
            archerHP += state.getUnit(i).getHP()/state.getUnit(i).getTemplateView().getBaseHealth();
        }
        
        
        //Calculate utility based on the calculated values multiplied by their associated weight
        return (footmanHP*2) + (archerHP*-2) + (numFootman*10) + (numArcher*-10) + (averageDistance*-3);
    }
    
    //Function that calculates the distance between a footman and the closest archer.
    public double shortestDistance(int footmanID){
        UnitView footman = state.getUnit(footmanID);
        double shortestDistance = Double.POSITIVE_INFINITY;
        for(int a:archerIds){
            UnitView archer = state.getUnit(a);
            double distance = DistanceMetrics.euclideanDistance(footman.getXPosition(), footman.getYPosition(), archer.getXPosition(), archer.getYPosition());
            if(distance < shortestDistance){
                shortestDistance = distance;
            }
        }
        return shortestDistance;
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
     * Creates a State with the given specifications
     * @param unit_loc the location of the units that may have moved [u1 x, u1 y] or [u1 x, u1 y, u2 x, u2 y]
     * @param playerTurn  the playerNum of the player whose turn it is
     * @author Previn Kumar
     */
    private GameState createState(int[] unit_locations, int playerTurn) {
        StateBuilder sBuilder = new StateBuilder();
        sBuilder.setSize(xExtent, yExtent);
        sBuilder.setTurn(++turn);
        // Adds all resource nodes to the new state
        for (ResourceView resource : state.getAllResourceNodes()) {
            sBuilder.addResource(buildResourceNode(resource));
        }
        int location_index = 0;
        //if player 0's turn add footmen to the new unit_locations else add the archers to the new locations
        if (playerTurn == 0) {
            for (Integer footmanId : footmanIds) {
                UnitView footman = state.getUnit(footmanId);
                UnitTemplateView footmanTemplate = footman.getTemplateView();
                Unit newFootman = new Unit(buildUnitTemplate(footmanTemplate, footmanId), footmanId);
                newFootman.setHP(footman.getHP());
                newFootman.setCargo(footman.getCargoType(), footman.getCargoAmount());
                newFootman.setxPosition(unit_locations[location_index]);
                newFootman.setyPosition(unit_locations[location_index + 1]);
                location_index = location_index + 2;
                sBuilder.addUnit(newFootman, newFootman.getxPosition(), newFootman.getyPosition());
            }
            for (Integer archerId : archerIds) {
                UnitView archer = state.getUnit(archerId);
                UnitTemplateView archerTemplate = archer.getTemplateView();
                Unit newArcher = new Unit(buildUnitTemplate(archerTemplate, archerId), archerId);
                newArcher.setHP(archer.getHP());
                newArcher.setCargo(archer.getCargoType(), archer.getCargoAmount());
                newArcher.setxPosition(archer.getXPosition());
                newArcher.setyPosition(archer.getYPosition());
                sBuilder.addUnit(newArcher, newArcher.getxPosition(), newArcher.getyPosition());
            }
        }
        else {
            for (Integer archerId : archerIds) {
                UnitView archer = state.getUnit(archerId);
                UnitTemplateView archerTemplate = archer.getTemplateView();
                Unit newArcher = new Unit(buildUnitTemplate(archerTemplate, archerId), archerId);
                newArcher.setHP(archer.getHP());
                newArcher.setCargo(archer.getCargoType(), archer.getCargoAmount());
                newArcher.setxPosition(unit_locations[location_index]);
                newArcher.setyPosition(unit_locations[location_index + 1]);
                sBuilder.addUnit(newArcher, newArcher.getxPosition(), newArcher.getyPosition());
                location_index = location_index + 2;
            }
            for (Integer footmanId : footmanIds) {
                UnitView footman = state.getUnit(footmanId);
                UnitTemplateView footmanTemplate = footman.getTemplateView();
                Unit newFootman = new Unit(buildUnitTemplate(footmanTemplate, footmanId), footmanId);
                newFootman.setHP(footman.getHP());
                newFootman.setCargo(footman.getCargoType(), footman.getCargoAmount());
                newFootman.setxPosition(footman.getXPosition());
                newFootman.setyPosition(footman.getYPosition());
                sBuilder.addUnit(newFootman, newFootman.getxPosition(), newFootman.getyPosition());
            }
        }
        return new GameState(sBuilder.build().getView(playerTurn));
    }
    
    /**
     * Builds a ResourceNode from a ResourceView
     */
    private ResourceNode buildResourceNode(ResourceView view) {
        return new ResourceNode(view.getType(), view.getXPosition(), view.getYPosition(), view.getAmountRemaining(), view.getID());
    }
    
    /**
     * Create a UnitTemplate from a UnitTemplateView
     */
    private UnitTemplate buildUnitTemplate(UnitTemplateView view, int id) {
        UnitTemplate newTemplate = new UnitTemplate(id);
        newTemplate.setArmor(view.getArmor());
        newTemplate.setBaseHealth(view.getBaseHealth());
        newTemplate.setBasicAttack(view.getBasicAttack());
        newTemplate.setDurationAttack(view.getDurationAttack());
        newTemplate.setCanMove(true);
        newTemplate.setDurationMove(view.getDurationMove());
        newTemplate.setFoodProvided(view.getFoodProvided());
        newTemplate.setFoodCost(view.getFoodCost());
        newTemplate.setName(view.getName());
        newTemplate.setPiercingAttack(view.getPiercingAttack());
        newTemplate.setPlayer(view.getPlayer());
        newTemplate.setRange(view.getRange());
        newTemplate.setSightRange(view.getSightRange());
        newTemplate.setCanAcceptGold(view.canAcceptGold());
        newTemplate.setCanAcceptWood(view.canAcceptWood());
        newTemplate.setCanBuild(view.canBuild());
        newTemplate.setCanGather(view.canGather());
        newTemplate.setCharacter(view.getCharacter());
        newTemplate.setTimeCost(view.getTimeCost());
        return newTemplate;
    }

    /**
     * Returns true if the archers are dead
     */
    public boolean areArchersDead() {
        return archerIds.size() == 0;
    }
}