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
    private List<Integer> myUnitIds;
    private List<Integer> enemyUnitIds;
    private List<Integer> resourceIDs;
    private List<Integer> archerIds = new ArrayList<Integer>();
    private List<Integer> footmanIds = new ArrayList<Integer>();
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
     *W
     *
     * Calculate the utility based on total footmanHP, archerHP, number of footmen/archers,
     *  number of trees blocking the footmen and average distance between footmen and nearest archer
     * Each Utility has its own weight (ex. being adjacent to archers should make u attack)
     * @return The weighted linear combination of the features
     * @author Minhal Gardezi
     */
    public double getUtility() {
        //Calculate information on the state to create utility
        double HP_Weight = 15;
        double ArcherHP_Weight = -25;
        double numFootman_Weight = 10;
        double numArcher_Weight = -10;
        double averageDistance_Weight = -2;
        double numAttacking_Weight = 20;
        
        
        
        int numAttacking  = 0;
        double footmanHP = 0;
        double archerHP = 0;
        int numFootman = footmanIds.size();
        int numArcher = archerIds.size();
        double averageDistance = 0;
        
        for(int i: footmanIds){
            footmanHP += state.getUnit(i).getHP()/state.getUnit(i).getTemplateView().getBaseHealth();
            double shortestDistance = shortestDistance(i);
            averageDistance += shortestDistance;
            
        }
        
        for(int i: archerIds){
            archerHP += state.getUnit(i).getHP()/state.getUnit(i).getTemplateView().getBaseHealth();
        }
        
        
        //Calculate utility based on the calculated values multiplied by their associated weight
        averageDistance /= footmanIds.size();
        footmanHP /= footmanIds.size();
        archerHP /= archerIds.size();
        
        return (footmanHP*HP_Weight) + (archerHP*ArcherHP_Weight) + (numFootman*numFootman_Weight) + (numArcher*numArcher_Weight) + (averageDistance*averageDistance_Weight);
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
    public List<GameStateChild> getChildren(int playerIdTurn) {
    	if (playerIdTurn == 0) {
    		return generatePossibleChildren(footmanIds, playerIdTurn);
    	}
    	else {
    		return generatePossibleChildren(archerIds, playerIdTurn);
    	}
    }
    
    /**
     * 
     * @param unitIds Units to generate possible moves for
     * @param playerTurn current playeNumber
     * @return List of child states
     */
    public List<GameStateChild> generatePossibleChildren(List<Integer> unitIds, int playerTurn) {
    	List<GameStateChild> child_states = new LinkedList<GameStateChild>();
    	int[] unit_locations = new int[4];
    	// Generate all combination of directional child states and one combination of direction and an attack move
        for (Direction direction : Direction.values()) {
        	int newX = state.getUnit(unitIds.get(0)).getXPosition() + direction.xComponent();
        	int newY = state.getUnit(unitIds.get(0)).getYPosition() + direction.yComponent();
        	if (state.inBounds(newX, newY) && !state.isUnitAt(newX, newY) && !state.isResourceAt(newX, newY)) {
	        	// Stores the new location of the unit
	        	unit_locations[0] = newX;
	        	unit_locations[1] = newY;
	        	Map<Integer, Action> unit_actions = new HashMap<Integer, Action>();
	        	// Adds the unit action to the action map
	        	unit_actions.put(unitIds.get(0), Action.createPrimitiveMove(unitIds.get(0), direction));
	        	// if there are two units get a combination of directional moves or unit1 moves and unit2 attacks
	        	if (unitIds.size() == 2) {
	        		// Create GameStateChild for every second direction in combination with the first
	        		for (Direction direction2 : Direction.values()) {
	        			int newX2 = state.getUnit(unitIds.get(0)).getXPosition() + direction.xComponent();
	                	int newY2 = state.getUnit(unitIds.get(0)).getYPosition() + direction.yComponent();
	                	if (state.inBounds(newX2, newY2) && !state.isUnitAt(newX2, newY2) && !state.isResourceAt(newX2, newY2)) {
		        			// Stores the new location of the second unit
		        			unit_locations[2] = state.getUnit(unitIds.get(1)).getXPosition() + direction2.xComponent();
		                	unit_locations[3] = state.getUnit(unitIds.get(1)).getYPosition() + direction2.yComponent();
		                	Map<Integer, Action> sec_unit_actions = new HashMap<Integer, Action>();
		                	// Adds the unit action to the second action map, so the first can be reused in other iterations
		                	sec_unit_actions.put(unitIds.get(0), Action.createPrimitiveMove(unitIds.get(1), direction2));
		                	sec_unit_actions.putAll(unit_actions);
		                	child_states.add(new GameStateChild(sec_unit_actions, createMoveState(unit_locations, playerTurn)));
	                	}
	        		}
	        		// Create a GameState where first unit moves and second attacks
	        		if (playerTurn == enemyPlayerNum || isArcherAdjacent(unitIds.get(1))) {
	        			//create a combination of move and attack for the footman
	        			if (playerTurn == myPlayerNum) {
	        				List<Integer> adjacentArchers = adjacentArcherIds(unitIds.get(1));
	        				for (Integer adjacentArcher : adjacentArchers) {
	        					Action attack = Action.createPrimitiveAttack(unitIds.get(1), adjacentArcher);
	        					Map<Integer, Action> sec_unit_actions = new HashMap<Integer, Action>();
	        					sec_unit_actions.put(unitIds.get(1), attack);
	        					sec_unit_actions.putAll(unit_actions);
	        					child_states.add(new GameStateChild(sec_unit_actions, createMoveAttackState(unit_locations[0], unit_locations[1], unitIds.get(0), unitIds.get(1), adjacentArcher, playerTurn)));
	        				}
	        			}
	        			// Create combination of move and attack for the archers
	        			else {
	        				for (Integer footman : footmanIds) {
	        					Action attack = Action.createPrimitiveAttack(unitIds.get(1), footman);
	        					Map<Integer, Action> sec_unit_actions = new HashMap<Integer, Action>();
	        					sec_unit_actions.put(unitIds.get(1), attack);
	        					sec_unit_actions.putAll(unit_actions);
	        					child_states.add(new GameStateChild(sec_unit_actions, createMoveAttackState(unit_locations[0], unit_locations[1], unitIds.get(0), unitIds.get(1), footman, playerTurn)));
	        				}
	        			}
	        		}
	        	}
        	}
        }
        // Create states for unit 1 to attack while unit 2 moves or attacks
        if (playerTurn == enemyPlayerNum || isArcherAdjacent(unitIds.get(0))) {
        	List<Integer> targets = new LinkedList<Integer>();
			//create a combination of move and attack for the footman
			if (playerTurn == myPlayerNum) {
				targets = adjacentArcherIds(unitIds.get(0));
			}
			else {
				targets = footmanIds;
			}
			for (Integer target : targets) {
				Action attack = Action.createPrimitiveAttack(unitIds.get(0), target);
				Map<Integer, Action> unit_actions = new HashMap<Integer, Action>();
				unit_actions.put(unitIds.get(0), attack);
				//create simultaneous move or attack for unit 2 if alive
				if (unitIds.size() == 2) {
					// Unit 2 moves
					for (Direction direction : Direction.values()) {
						int newX = state.getUnit(unitIds.get(1)).getXPosition() + direction.xComponent();
			        	int newY = state.getUnit(unitIds.get(1)).getYPosition() + direction.yComponent();
			        	//If directional move is valid
			        	if (state.inBounds(newX, newY) && !state.isUnitAt(newX, newY) && !state.isResourceAt(newX, newY)) {
			        		Map<Integer, Action> sec_unit_actions = new HashMap<Integer, Action>();
		                	// Adds the unit action to the second action map, so the first can be reused in other iterations
		                	sec_unit_actions.put(unitIds.get(1), Action.createPrimitiveMove(unitIds.get(1), direction));
		                	sec_unit_actions.putAll(unit_actions);
		                	child_states.add(new GameStateChild(sec_unit_actions, createMoveAttackState(newX, newY, unitIds.get(1), unitIds.get(0), target, playerTurn)));
			        	}
					}
					// Unite 2 attacks
					if (playerTurn == enemyPlayerNum || isArcherAdjacent(unitIds.get(1))) {
	        			//create a combination of attack and attack for the footman
	        			if (playerTurn == myPlayerNum) {
	        				List<Integer> adjacentArchers = adjacentArcherIds(unitIds.get(1));
	        				for (Integer adjacentArcher : adjacentArchers) {
	        					Action attack2 = Action.createPrimitiveAttack(unitIds.get(1), adjacentArcher);
	        					Map<Integer, Action> sec_unit_actions = new HashMap<Integer, Action>();
	        					sec_unit_actions.put(unitIds.get(1), attack2);
	        					sec_unit_actions.putAll(unit_actions);
	        					child_states.add(new GameStateChild(sec_unit_actions, createDoubleAttackState(unitIds, target, adjacentArcher, playerTurn)));
	        				}
	        			}
	        			// Create combination of attack and attack for the archers
	        			else {
	        				for (Integer footman : footmanIds) {
	        					Action attack2 = Action.createPrimitiveAttack(unitIds.get(1), footman);
	        					Map<Integer, Action> sec_unit_actions = new HashMap<Integer, Action>();
	        					sec_unit_actions.put(unitIds.get(1), attack2);
	        					sec_unit_actions.putAll(unit_actions);
	        					child_states.add(new GameStateChild(sec_unit_actions, createDoubleAttackState(unitIds, target, footman, playerTurn)));
	        				}
	        			}
	        		}
				}
			}
		}
        return child_states;
    }
    
    /**
     * Creates a State with only unit moves
     * @param unit_loc the location of the units that may have moved [u1 x, u1 y] or [u1 x, u1 y, u2 x, u2 y]
     * @param playerTurn  the playerNum of the player whose turn it is
     * @author Previn Kumar
     */
    private GameState createMoveState(int[] unit_locations, int playerTurn) {
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
     * Creates a State with a unit move and attack
     */
    private GameState createMoveAttackState(int newX, int newY, int moveUnit, int attackUnit, int target, int playerTurn) {
        StateBuilder sBuilder = new StateBuilder();
        sBuilder.setSize(xExtent, yExtent);
        sBuilder.setTurn(++turn);
        // Adds all resource nodes to the new state
        for (ResourceView resource : state.getAllResourceNodes()) {
            sBuilder.addResource(buildResourceNode(resource));
        }
        //if player 0's turn add footmen to the new unit_locations else add the archers to the new locations
        if (playerTurn == 0) {
            //Move footman
            UnitView footman = state.getUnit(moveUnit);
            UnitTemplateView footmanTemplate = footman.getTemplateView();
            Unit newFootman = new Unit(buildUnitTemplate(footmanTemplate, moveUnit), moveUnit);
            newFootman.setHP(footman.getHP());
            newFootman.setCargo(footman.getCargoType(), footman.getCargoAmount());
            newFootman.setxPosition(newX);
            newFootman.setyPosition(newY);
            sBuilder.addUnit(newFootman, newFootman.getxPosition(), newFootman.getyPosition());
            
            //Place attack footman in same location
            UnitView footman2 = state.getUnit(attackUnit);
            UnitTemplateView footmanTemplate2 = footman2.getTemplateView();
            Unit newFootman2 = new Unit(buildUnitTemplate(footmanTemplate2, attackUnit), attackUnit);
            newFootman2.setHP(footman2.getHP());
            newFootman2.setCargo(footman2.getCargoType(), footman2.getCargoAmount());
            newFootman2.setxPosition(footman2.getXPosition());
            newFootman2.setyPosition(footman2.getYPosition());
            sBuilder.addUnit(newFootman2, newFootman2.getxPosition(), newFootman2.getyPosition());
            
            //Pace archers with lowered health in same position
            for (Integer archerId : archerIds) {
                UnitView archer = state.getUnit(archerId);
                UnitTemplateView archerTemplate = archer.getTemplateView();
                Unit newArcher = new Unit(buildUnitTemplate(archerTemplate, archerId), archerId);
                if (archerId == target) {
                	newArcher.setHP(archer.getHP() - 10);
                }
                else {
                	newArcher.setHP(archer.getHP());
                }
                if (newArcher.getCurrentHealth() > 0) {
	                newArcher.setCargo(archer.getCargoType(), archer.getCargoAmount());
	                newArcher.setxPosition(archer.getXPosition());
	                newArcher.setyPosition(archer.getYPosition());
	                sBuilder.addUnit(newArcher, newArcher.getxPosition(), newArcher.getyPosition());
                }
            }
        }
        else {
        	//Move archer
            UnitView archer = state.getUnit(moveUnit);
            UnitTemplateView archerTemplate = archer.getTemplateView();
            Unit newArcher = new Unit(buildUnitTemplate(archerTemplate, moveUnit), moveUnit);
            newArcher.setHP(archer.getHP());
            newArcher.setCargo(archer.getCargoType(), archer.getCargoAmount());
            newArcher.setxPosition(newX);
            newArcher.setyPosition(newY);
            sBuilder.addUnit(newArcher, newArcher.getxPosition(), newArcher.getyPosition());
            
            //Place attack archer in same location
            UnitView archer2 = state.getUnit(attackUnit);
            UnitTemplateView archerTemplate2 = archer2.getTemplateView();
            Unit newArcher2 = new Unit(buildUnitTemplate(archerTemplate2, attackUnit), attackUnit);
            newArcher2.setHP(archer2.getHP());
            newArcher2.setCargo(archer2.getCargoType(), archer2.getCargoAmount());
            newArcher2.setxPosition(archer2.getXPosition());
            newArcher2.setyPosition(archer2.getYPosition());
            sBuilder.addUnit(newArcher2, newArcher2.getxPosition(), newArcher2.getyPosition());
            
            //Place footman with lowered health in the same position
            for (Integer footmanId : footmanIds) {
                UnitView footman = state.getUnit(footmanId);
                UnitTemplateView footmanTemplate = footman.getTemplateView();
                Unit newFootman = new Unit(buildUnitTemplate(footmanTemplate, footmanId), footmanId);
                if (footmanId == target) {
                	newFootman.setHP(footman.getHP() - 10);
                }
                else {
                	newFootman.setHP(footman.getHP());
                }
                if (newFootman.getCurrentHealth() > 0) {
	                newFootman.setCargo(footman.getCargoType(), footman.getCargoAmount());
	                newFootman.setxPosition(footman.getXPosition());
	                newFootman.setyPosition(footman.getYPosition());
	                sBuilder.addUnit(newFootman, newFootman.getxPosition(), newFootman.getyPosition());
                }
            }
        }
        return new GameState(sBuilder.build().getView(playerTurn));
    }
    
    /**
     * Creates a State with a unit move and attack
     */
    private GameState createDoubleAttackState(List<Integer> unitIds, int target1, int target2, int playerTurn) {
        StateBuilder sBuilder = new StateBuilder();
        sBuilder.setSize(xExtent, yExtent);
        sBuilder.setTurn(++turn);
        // Adds all resource nodes to the new state
        for (ResourceView resource : state.getAllResourceNodes()) {
            sBuilder.addResource(buildResourceNode(resource));
        }
        //Place attacking units in the same location with same health
        for (Integer unitId : unitIds) {
            UnitView unit = state.getUnit(unitId);
            UnitTemplateView unitTemplate = unit.getTemplateView();
            Unit newUnit = new Unit(buildUnitTemplate(unitTemplate, unitId), unitId);
        	newUnit.setHP(unit.getHP());
            newUnit.setCargo(unit.getCargoType(), unit.getCargoAmount());
            newUnit.setxPosition(unit.getXPosition());
            newUnit.setyPosition(unit.getYPosition());
            sBuilder.addUnit(newUnit, newUnit.getxPosition(), newUnit.getyPosition());
        }
        List<Integer> targetIds = new LinkedList<Integer>();
        if (target1 != target2) {
        	targetIds.add(target1);
        	targetIds.add(target2);
        }
        else {
        	targetIds.add(target1);
        }
        //Place targets in same location with reduced health
        for (Integer targetId : targetIds) {
        	UnitView target = state.getUnit(targetId);
            UnitTemplateView unitTemplate = target.getTemplateView();
            Unit newUnit = new Unit(buildUnitTemplate(unitTemplate, targetId), targetId);
        	newUnit.setHP(target.getHP() - 10);
        	if (newUnit.getCurrentHealth() > 0) {
	            newUnit.setCargo(target.getCargoType(), target.getCargoAmount());
	            newUnit.setxPosition(target.getXPosition());
	            newUnit.setyPosition(target.getYPosition());
	            sBuilder.addUnit(newUnit, newUnit.getxPosition(), newUnit.getyPosition());
        	}
        }
        //Add untargetted archer if there is one
        if (playerTurn == myPlayerNum && targetIds.size() != archerIds.size()) {
        	int unitId = targetIds.get(0) == archerIds.get(0) ? archerIds.get(1) : archerIds.get(0);
        	UnitView unit = state.getUnit(unitId);
            UnitTemplateView unitTemplate = unit.getTemplateView();
            Unit newUnit = new Unit(buildUnitTemplate(unitTemplate, unitId), unitId);
        	newUnit.setHP(unit.getHP());
            newUnit.setCargo(unit.getCargoType(), unit.getCargoAmount());
            newUnit.setxPosition(unit.getXPosition());
            newUnit.setyPosition(unit.getYPosition());
            sBuilder.addUnit(newUnit, newUnit.getxPosition(), newUnit.getyPosition());
        }
        //Add untargetted footman if there is one
        else if (playerTurn == enemyPlayerNum && targetIds.size() != footmanIds.size()) {
        	int unitId = targetIds.get(0) == footmanIds.get(0) ? footmanIds.get(1) : footmanIds.get(0);
        	UnitView unit = state.getUnit(unitId);
            UnitTemplateView unitTemplate = unit.getTemplateView();
            Unit newUnit = new Unit(buildUnitTemplate(unitTemplate, unitId), unitId);
        	newUnit.setHP(unit.getHP());
            newUnit.setCargo(unit.getCargoType(), unit.getCargoAmount());
            newUnit.setxPosition(unit.getXPosition());
            newUnit.setyPosition(unit.getYPosition());
            sBuilder.addUnit(newUnit, newUnit.getxPosition(), newUnit.getyPosition());
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
     * Returns true if either of the units are adjacent to an archer
     */
    public boolean isArcherAdjacent(Integer unit) {
    	int footmanx = state.getUnit(unit).getXPosition();
    	int footmany = state.getUnit(unit).getYPosition();
    	int[] archerx = new int[2];
    	int[] archery = new int[2];
    	int index = 0;
    	for (int id : archerIds) {
    		archerx[index] = state.getUnit(id).getXPosition();
    		archery[index] = state.getUnit(id).getYPosition();
    		index++;
    	}
    	for (int i = 0; i < archerx.length; i++) {
    		if (footmanx == archerx[i] && (footmany == archery[i] + 1 || footmany == archery[i] - 1)) {
    			return true;
    		}
    		if (footmany == archery[i] && (footmanx == archerx[i] + 1 || footmanx == archerx[i] - 1)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Returns a List of the IDs of an adjacent archers
     */
    public List<Integer> adjacentArcherIds(Integer unit) {
    	int footmanx = state.getUnit(unit).getXPosition();
    	int footmany = state.getUnit(unit).getYPosition();
    	int[] archerx = new int[2];
    	int[] archery = new int[2];
    	List<Integer> archerId = new LinkedList<Integer>();
    	int index = 0;
    	for (int id : archerIds) {
    		archerx[index] = state.getUnit(id).getXPosition();
    		archery[index] = state.getUnit(id).getYPosition();
    		index++;
    	}
    	for (int i = 0; i < archerx.length; i++) {
    		if (footmanx == archerx[i] && (footmany == archery[i] + 1 || footmany == archery[i] - 1)) {
    			archerId.add(archerIds.get(i));
    		}
    		if (footmany == archery[i] && (footmanx == archerx[i] + 1 || footmanx == archerx[i] - 1)) {
    			archerId.add(archerIds.get(i));
    		}
    	}
    	return archerId;
    }

    /**
     * Returns true if the archers are dead
     */
    public boolean areArchersDead() {
        return archerIds.size() == 0;
    }
}