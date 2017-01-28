
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class MyCombatAgent extends Agent {
	
	private int enemyPlayerNum = 1;

	public MyCombatAgent(int playernum, String[] otherargs) {
		super(playernum);
		
		if(otherargs.length > 0)
		{
			enemyPlayerNum = new Integer(otherargs[0]);
		}
		
		System.out.println("Constructed MyCombatAgent");
	}

	@Override
	public Map<Integer, Action> initialStep(StateView newstate,
			HistoryView statehistory) {
		
		return middleStep(newstate, statehistory);
	}

	@Override
	public Map<Integer, Action> middleStep(StateView newstate,
			HistoryView statehistory) {
		// This stores the action that each unit will perform
		// if there are no changes to the current actions then this
		// map will be empty
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		List<Integer> myUnitIDs = newstate.getUnitIds(playernum);
		List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);
		List<Integer> Footman = new ArrayList<Integer>();
		List<Integer> Ballista = new ArrayList<Integer>();
		List<Integer> Archer = new ArrayList<Integer>();
		List<Integer> enemyFootman = new ArrayList<Integer>();

		
        for(Integer unitID : myUnitIDs) {
			UnitView unit = newstate.getUnit(unitID);
			String unitTypeName = unit.getTemplateView().getName();
			if (unitTypeName.equals("Footman")) {
				Footman.add(unitID);
			}
			if (unitTypeName.equals("Ballista")) {
				Ballista.add(unitID);
			}
			if (unitTypeName.equals("Archer")) {
				Archer.add(unitID);
			}
			
			for(Integer enemy: enemyUnitIDs){
				for(Integer closeAttack:myUnitIDs){
				actions.put(closeAttack, Action.createPrimitiveAttack(closeAttack, enemy));
				}
			}
        }
        
        int enemyTower = 0;
        for(Integer unitID : enemyUnitIDs){
			UnitView unit = newstate.getUnit(unitID);
			String unitTypeName = unit.getTemplateView().getName();
			if (unitTypeName.equals("Footman")) {
				enemyFootman.add(unitID);
			}
			
			else{
				enemyTower = unitID;
			}
        }
		

		System.out.println(enemyFootman);
		System.out.println(Footman);
		System.out.println(Archer);
		System.out.println(Ballista);
		System.out.println(enemyTower);



		// This is a list of enemy units
		
		if(enemyUnitIDs.size() == 0)
		{
			// Nothing to do because there is no one left to attack
			return actions;
		}
		int index = 0;
		for(int footman:Footman){
			actions.put(footman, Action.createCompoundAttack(footman, enemyFootman.get(index)));
			index++;
			if(index == enemyFootman.size()){
				index = 0;
			}
		}
		index = 0;
		for(int archer:Archer){
			actions.put(archer, Action.createCompoundAttack(archer, enemyTower));
		}
		

		for(int ballista:Ballista){
			if(enemyTower != 0){
				actions.put(ballista, Action.createCompoundAttack(ballista, enemyTower));
			}
			else{
				actions.put(ballista, Action.createCompoundAttack(ballista, enemyFootman.get(0)));
			}
		}
		
		
		return actions;
	}

	@Override
	public void terminalStep(StateView newstate, HistoryView statehistory) {
		System.out.println("Finished the episode");
	}

	@Override
	public void savePlayerData(OutputStream os) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPlayerData(InputStream is) {
		// TODO Auto-generated method stub

	}

}