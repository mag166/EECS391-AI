import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

/**
 * @author Previn Kumar
 * EECS 391 - Gardezi_Kumar
 * An extended resource collecting agent
 * Builds peasants, a farm, a barrack, and footmen 
 */
public class FirstResourceCollectionAgent extends Agent {

	public FirstResourceCollectionAgent(int playernum) {
		super(playernum);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<Integer, Action> initialStep(StateView current_state, HistoryView state_history) {
		// TODO Auto-generated method stub
		return middleStep(current_state, state_history);
	}

	@Override
	public void loadPlayerData(InputStream arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Integer, Action> middleStep(StateView current_state, HistoryView state_history) {
		// TODO Auto-generated method stub
		//Costs to build
		int PEASANT_COST = 400;
		int FARM_GOLD_COST = 500;
		int FARM_WOOD_COST = 250;
		int BARRACKS_GOLD_COST = 700;
		int BARRACKS_WOOD_COST = 400;
		int FOOTMAN_COST = 600;
		
		// Store unit actions
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		
		
		// Lists of units
		List<Integer> myUnitIds = current_state.getUnitIds(playernum);
        List<Integer> peasantIds = new ArrayList<Integer>();
        List<Integer> townhallIds = new ArrayList<Integer>();
        List<Integer> farmIds = new ArrayList<Integer>();
        List<Integer> barrackIds = new ArrayList<Integer>();
        List<Integer> footmanIds = new ArrayList<Integer>();
        
        
        // Classifies each unit ID as a Townhall, Peasant, Farm, or Barracks
        for(Integer unitID : myUnitIds) {
			UnitView unit = current_state.getUnit(unitID);
			String unitTypeName = unit.getTemplateView().getName();
			
			if (unitTypeName.equals("TownHall")) {
				townhallIds.add(unitID);
			}
			else if (unitTypeName.equals("Peasant")) {
				peasantIds.add(unitID);
			}
			else if (unitTypeName.equals("Farm")) {
				farmIds.add(unitID);
			}
			else if (unitTypeName.equals("Barracks")) {
				barrackIds.add(unitID);
			}
			else if (unitTypeName.equals("Footman")) {
				footmanIds.add(unitID);
			}
			else {
				System.err.println("Unexpected Unit type: " + unitTypeName);
			}
        }
        
        // Gets current gold and wood amount
        int current_gold = current_state.getResourceAmount(playernum, ResourceType.GOLD);
        int current_wood = current_state.getResourceAmount(playernum, ResourceType.WOOD);
        
        // Get resource nodes
        List<Integer> goldMines = current_state.getResourceNodeIds(Type.GOLD_MINE);
        List<Integer> trees = current_state.getResourceNodeIds(Type.TREE);
        
        //Decides on an action for the peasant(s)
        if (peasantIds.size() >= 2) {
        	//Builds a farm if there are at least 2 peasants and enough materials
        	if (farmIds.isEmpty() && current_gold >= FARM_GOLD_COST && current_wood >= FARM_WOOD_COST) {
				System.out.println("Building a Farm");
				int peasantID = peasantIds.get(0);
				TemplateView farmTemplate = current_state.getTemplate(playernum, "Farm");
				int farmTemplateID = farmTemplate.getID();
				actions.put(peasantID, Action.createPrimitiveBuild(peasantID, farmTemplateID));
			}
        	//Builds a barracks if there are at least 2 peasants, a farm, and enough materials
        	else if (barrackIds.isEmpty() && current_gold >= BARRACKS_GOLD_COST && current_wood >= BARRACKS_WOOD_COST) {
				System.out.println("Building a Barracks");
				int peasantID = peasantIds.get(0);
				TemplateView barracksTemplate = current_state.getTemplate(playernum, "Barracks");
				int barracksTemplateID = barracksTemplate.getID();
				actions.put(peasantID, Action.createPrimitiveBuild(peasantID, barracksTemplateID));
			}
        	//Builds a footman if there are at least 2 peasants, a farm, a barracks, and enough materials
        	else if (barrackIds.size() >= 1 && footmanIds.size() < 4 && current_gold >= FOOTMAN_COST) {
				System.out.println("Creating a Footman");
				int barracksId = barrackIds.get(0);
				TemplateView footmanTemplate = current_state.getTemplate(playernum, "Footman");
				int footmanTemplateID = footmanTemplate.getID();
				actions.put(barracksId, Action.createCompoundProduction(barracksId, footmanTemplateID));
			}
        	//Set the peasants to collect more materials
        	else {
        		Action action = null;
        		for (int peasantID : peasantIds) {
	    	        if (current_state.getUnit(peasantID).getCargoAmount() > 0) {
	                    // If the agent is carrying cargo then command it to deposit what its carrying at the townhall.
	                    action = new TargetedAction(peasantID, ActionType.COMPOUNDDEPOSIT, townhallIds.get(0));
	    	        }
	    	        else {
	    	            // If the agent isn't carrying anything instruct it to go collect either gold or wood
	    	            if(current_gold < current_wood) {
	    	            	
	    	            	action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, goldMines.get(0));
	    	            }
	    	            else {
	    	            	action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, trees.get(0));
	    	            }
	    	        }
	    	        actions.put(peasantID, action);
        		}
        	}
        }
        else {
        	//Creates a second peasant if there is enough gold
        	if (current_gold >= PEASANT_COST) {
        		System.out.println("Creating a peasant");
			    // Get the peasant template's unique ID
			    TemplateView peasantTemplate = current_state.getTemplate(playernum, "Peasant");
			    int peasantTemplateID = peasantTemplate.getID();
			    int townhallID = townhallIds.get(0);
			
			    // Instructs the specified townhall to build a unit with the peasant template ID.
			    actions.put(townhallID, Action.createCompoundProduction(townhallID, peasantTemplateID));
			    current_gold -= PEASANT_COST;
			}
        	//Set the peasant to collect more materials
        	else {
        		Action action = null;
        		for (int peasantID : peasantIds) {
	    	        if (current_state.getUnit(peasantID).getCargoAmount() > 0) {
	                    // If the agent is carrying cargo then command it to deposit what its carrying at the townhall.
	                    action = new TargetedAction(peasantID, ActionType.COMPOUNDDEPOSIT, townhallIds.get(0));
	    	        }
	    	        else {
	    	            // If the agent isn't carrying anything instruct it to go collect either gold or wood
	    	            if(current_gold < current_wood) {
	    	            	action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, goldMines.get(0));
	    	            }
	    	            else {
	    	            	action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, trees.get(0));
	    	            }
	    	        }
	    	        actions.put(peasantID, action);
        		}
        	}
        }
        
		return actions;
	}

	@Override
	public void savePlayerData(OutputStream arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminalStep(StateView arg0, HistoryView arg1) {
		// TODO Auto-generated method stub

	}

}
