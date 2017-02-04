import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * EECS 391
 * Project 2
 * AstarAgent Search Implementation
 * @author Previn Kumar
 * Group gardezi_kumar_391s17
 */
public class AstarAgentImp2 extends Agent {

    class MapLocation
    {
        public int x, y;
        int cost;
        MapLocation start;
        MapLocation goal;

        public MapLocation(int x, int y, MapLocation start, int cost)
        {
            this.x = x;
            this.y = y;
            this.cost = cost;
            this.start = start;
            this.goal = null;
        }
        
        public MapLocation(int x, int y, int cost, MapLocation start, MapLocation goal)
        {
            this.x = x;
            this.y = y;
            this.cost = cost;
            this.start = start;
            this.goal = goal;
        }
        
        public void setStart(MapLocation start) {
        	this.start = start;
        }
        
        public void setGoal(MapLocation goal) {
        	this.goal = goal;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            else if (!(obj instanceof MapLocation)) {
                return false;
            }
            else {
            	return this.x == ((MapLocation) obj).x && this.y == ((MapLocation) obj).y;
            }
        }
    }

    Stack<MapLocation> path;
    int footmanID, townhallID, enemyFootmanID;
    MapLocation nextLoc;

    private long totalPlanTime = 0; // nsecs
    private long totalExecutionTime = 0; //nsecs

    public AstarAgentImp2(int playernum)
    {
        super(playernum);

        System.out.println("Constructed AstarAgent");
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        // get the footman location
        List<Integer> unitIDs = newstate.getUnitIds(playernum);

        if(unitIDs.size() == 0)
        {
            System.err.println("No units found!");
            return null;
        }

        footmanID = unitIDs.get(0);

        // double check that this is a footman
        if(!newstate.getUnit(footmanID).getTemplateView().getName().equals("Footman"))
        {
            System.err.println("Footman unit not found");
            return null;
        }

        // find the enemy playernum
        Integer[] playerNums = newstate.getPlayerNumbers();
        int enemyPlayerNum = -1;
        for(Integer playerNum : playerNums)
        {
            if(playerNum != playernum) {
                enemyPlayerNum = playerNum;
                break;
            }
        }

        if(enemyPlayerNum == -1)
        {
            System.err.println("Failed to get enemy playernumber");
            return null;
        }

        // find the townhall ID
        List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);

        if(enemyUnitIDs.size() == 0)
        {
            System.err.println("Failed to find enemy units");
            return null;
        }

        townhallID = -1;
        enemyFootmanID = -1;
        for(Integer unitID : enemyUnitIDs)
        {
            Unit.UnitView tempUnit = newstate.getUnit(unitID);
            String unitType = tempUnit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall"))
            {
                townhallID = unitID;
            }
            else if(unitType.equals("footman"))
            {
                enemyFootmanID = unitID;
            }
            else
            {
                System.err.println("Unknown unit type");
            }
        }

        if(townhallID == -1) {
            System.err.println("Error: Couldn't find townhall");
            return null;
        }

        long startTime = System.nanoTime();
        path = findPath(newstate);
        totalPlanTime += System.nanoTime() - startTime;

        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        long startTime = System.nanoTime();
        long planTime = 0;

        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        if(shouldReplanPath(newstate, statehistory, path)) {
            long planStartTime = System.nanoTime();
            path = findPath(newstate);
            planTime = System.nanoTime() - planStartTime;
            totalPlanTime += planTime;
        }

        Unit.UnitView footmanUnit = newstate.getUnit(footmanID);

        int footmanX = footmanUnit.getXPosition();
        int footmanY = footmanUnit.getYPosition();

        if(!path.empty() && (nextLoc == null || (footmanX == nextLoc.x && footmanY == nextLoc.y))) {

            // stat moving to the next step in the path
            nextLoc = path.pop();

            System.out.println("Moving to (" + nextLoc.x + ", " + nextLoc.y + ")");
        }

        if(nextLoc != null && (footmanX != nextLoc.x || footmanY != nextLoc.y))
        {
            int xDiff = nextLoc.x - footmanX;
            int yDiff = nextLoc.y - footmanY;

            // figure out the direction the footman needs to move in
            Direction nextDirection = getNextDirection(xDiff, yDiff);

            actions.put(footmanID, Action.createPrimitiveMove(footmanID, nextDirection));
        } else {
            Unit.UnitView townhallUnit = newstate.getUnit(townhallID);

            // if townhall was destroyed on the last turn
            if(townhallUnit == null) {
                terminalStep(newstate, statehistory);
                return actions;
            }

            if(Math.abs(footmanX - townhallUnit.getXPosition()) > 1 ||
                    Math.abs(footmanY - townhallUnit.getYPosition()) > 1)
            {
                System.err.println("Invalid plan. Cannot attack townhall");
                totalExecutionTime += System.nanoTime() - startTime - planTime;
                return actions;
            }
            else {
                System.out.println("Attacking TownHall");
                // if no more movements in the planned path then attack
                actions.put(footmanID, Action.createPrimitiveAttack(footmanID, townhallID));
            }
        }

        totalExecutionTime += System.nanoTime() - startTime - planTime;
        return actions;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {
        System.out.println("Total turns: " + newstate.getTurnNumber());
        System.out.println("Total planning time: " + totalPlanTime/1e9);
        System.out.println("Total execution time: " + totalExecutionTime/1e9);
        System.out.println("Total time: " + (totalExecutionTime + totalPlanTime)/1e9);
    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this method.
     *
     * This method should return true when the path needs to be replanned
     * and false otherwise. This will be necessary on the dynamic map where the
     * footman will move to block your unit.
     *
     * @param state
     * @param history
     * @param currentPath
     * @return
     */
    private boolean shouldReplanPath(State.StateView state, History.HistoryView history, Stack<MapLocation> currentPath)
    {
        return false;
    }

    /**
     * This method is implemented for you. You should look at it to see examples of
     * how to find units and resources in Sepia.
     *
     * @param state
     * @return
     */
    private Stack<MapLocation> findPath(State.StateView state)
    {
        Unit.UnitView townhallUnit = state.getUnit(townhallID);
        Unit.UnitView footmanUnit = state.getUnit(footmanID);

        MapLocation startLoc = new MapLocation(footmanUnit.getXPosition(), footmanUnit.getYPosition(), null, 0);

        MapLocation goalLoc = new MapLocation(townhallUnit.getXPosition(), townhallUnit.getYPosition(), null, 0);

        MapLocation footmanLoc = null;
        if(enemyFootmanID != -1) {
            Unit.UnitView enemyFootmanUnit = state.getUnit(enemyFootmanID);
            footmanLoc = new MapLocation(enemyFootmanUnit.getXPosition(), enemyFootmanUnit.getYPosition(), null, 0);
        }

        // get resource locations
        List<Integer> resourceIDs = state.getAllResourceIds();
        Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
        for(Integer resourceID : resourceIDs)
        {
            ResourceNode.ResourceView resource = state.getResourceNode(resourceID);

            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
        }

        return AstarSearch(startLoc, goalLoc, state.getXExtent(), state.getYExtent(), footmanLoc, resourceLocations);
    }
    /**
     * This is the method you will implement for the assignment. Your implementation
     * will use the A* algorithm to compute the optimum path from the start position to
     * a position adjacent to the goal position.
     *
     * You will return a Stack of positions with the top of the stack being the first space to move to
     * and the bottom of the stack being the last space to move to. If there is no path to the townhall
     * then return null from the method and the agent will print a message and do nothing.
     * The code to execute the plan is provided for you in the middleStep method.
     *
     * As an example consider the following simple map
     *
     * F - - - -
     * x x x - x
     * H - - - -
     *
     * F is the footman
     * H is the townhall
     * x's are occupied spaces
     *
     * xExtent would be 5 for this map with valid X coordinates in the range of [0, 4]
     * x=0 is the left most column and x=4 is the right most column
     *
     * yExtent would be 3 for this map with valid Y coordinates in the range of [0, 2]
     * y=0 is the top most row and y=2 is the bottom most row
     *
     * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
     *
     * The path would be
     *
     * (1,0)
     * (2,0)
     * (3,1)
     * (2,2)
     * (1,2)
     *
     * Notice how the initial footman position and the townhall position are not included in the path stack
     *
     * @param start Starting position of the footman
     * @param goal MapLocation of the townhall
     * @param xExtent Width of the map
     * @param yExtent Height of the map
     * @param resourceLocations Set of positions occupied by resources
     * @return Stack of positions with top of stack being first move in plan
     * @author Previn Kumar
     */
    private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent, MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations)
    {
    	PriorityQueue<MapLocation> open_nodes = new PriorityQueue<MapLocation>(locationDistance(start, goal), new HeuristicDistanceComparator());
    	List<MapLocation> closed_nodes = new ArrayList<MapLocation>();
    	start.setGoal(goal);
    	start.setStart(null);
    	open_nodes.add(start);
    	
    	while (!open_nodes.isEmpty()) {
    		MapLocation location = open_nodes.poll();
    		
    		// return path is location is goal
    		if (location.equals(goal)) {
    			return generateSolutionPath(location);
    		}
    		
    		Set<MapLocation> child_nodes = generateChildNodes(location, goal, xExtent, yExtent, resourceLocations);
    		for (MapLocation child : child_nodes) {
    			if (closed_nodes.contains(child)) {
    				MapLocation node = closed_nodes.get(closed_nodes.indexOf(child));
    				if (node.cost <= child.cost) {
    					closed_nodes.add(child);
    				}
    				else {
    					open_nodes.add(child);
    				}
    			}
    			else {
    				open_nodes.add(child);
    			}
    		}
    		closed_nodes.add(location);
    	}
    	
        // return null if there is no path to the townhall
        return null;
    }
    
    
    /**
     * A comparator class used to compare the values of potential child states
     * @author Previn Kumar
     */
    private class HeuristicDistanceComparator implements Comparator<MapLocation> {

		@Override
		public int compare(MapLocation o1, MapLocation o2) {
    		return estimatedDistanceToGoal(o1) - estimatedDistanceToGoal(o2);
		}
    }
    
    /**
     * Returns the total estimated cost of a solution path through a node
     */
    private int estimatedDistanceToGoal(MapLocation node) {
    	if (node == null || node.goal == null) {
    		System.err.println("Attempting to estimate distance of node with null value");
    	}
    	return node.cost + chebyshev(node, node.goal);
    }
    
    /**
     * returns the total distance between two MapLocation objects
     */
    private int locationDistance(MapLocation start, MapLocation end) {
    	return Math.abs(end.x - start.x) + Math.abs(end.y - start.y);
    }
    
    /**
     * Returns the Chebyshev distance estimation between two MapLocations
     */
    private int chebyshev(MapLocation start, MapLocation end) {
    	int x_dist = Math.abs(end.x - start.x);
    	int y_dist = Math.abs(end.y - start.y);
    	
    	return (x_dist > y_dist)? x_dist : y_dist;
    }
    
    /**
     * Generates the successor nodes of a given MapLocation
     * Assumes the parent is a valid MapLocation
     */
    private Set<MapLocation> generateChildNodes(MapLocation parent, MapLocation goal, int xExtent, int yExtent, Set<MapLocation> resources) {
    	Set<MapLocation> child_nodes = new HashSet<MapLocation>();
    	if (parent.x - 1 >= 0 && validateNonResourceNode(parent.x - 1, parent.y, resources)) {
    		child_nodes.add(new MapLocation(parent.x - 1, parent.y, parent.cost + 1, parent, goal));
    	}
    	if (parent.x + 1 < xExtent && validateNonResourceNode(parent.x + 1, parent.y, resources)) {
    		child_nodes.add(new MapLocation(parent.x + 1, parent.y, parent.cost + 1, parent, goal));
    	}
    	if (parent.y - 1 >= 0 && validateNonResourceNode(parent.x, parent.y - 1, resources)) {
    		child_nodes.add(new MapLocation(parent.x, parent.y - 1, parent.cost + 1, parent, goal));
    	}
    	if (parent.y + 1 < yExtent && validateNonResourceNode(parent.x, parent.y + 1, resources)) {
    		child_nodes.add(new MapLocation(parent.x, parent.y + 1, parent.cost + 1, parent, goal));
    	}
    	return child_nodes;
    }
    
    /**
     * Validates that a potential MapLocation is not occupied by a resource node
     */
    private boolean validateNonResourceNode(int x, int y, Set<MapLocation> resources) {
    	MapLocation potential_loc = new MapLocation(x, y, null, 1);
    	boolean available = true;
    	for (MapLocation resource : resources) {
    		available = available && !(potential_loc.equals(resource));
    	}
    	return available;
    }
    
    /**
     * Creates a Stack of MapLocation objects from child to first parent with a null reference to parent
     */
    private Stack<MapLocation> generateSolutionPath(MapLocation deepest_child) {
    	Stack<MapLocation> solution_path = new Stack<MapLocation>();
    	solution_path.add(deepest_child);
    	MapLocation child = deepest_child;
    	while (child.start != null) {
    		child = child.start;
    		solution_path.add(child);
    	}
    	return solution_path;
    }

    /**
     * Primitive actions take a direction (e.g. NORTH, NORTHEAST, etc)
     * This converts the difference between the current position and the
     * desired position to a direction.
     *
     * @param xDiff Integer equal to 1, 0 or -1
     * @param yDiff Integer equal to 1, 0 or -1
     * @return A Direction instance (e.g. SOUTHWEST) or null in the case of error
     */
    private Direction getNextDirection(int xDiff, int yDiff) {

        // figure out the direction the footman needs to move in
        if(xDiff == 1 && yDiff == 1)
        {
            return Direction.SOUTHEAST;
        }
        else if(xDiff == 1 && yDiff == 0)
        {
            return Direction.EAST;
        }
        else if(xDiff == 1 && yDiff == -1)
        {
            return Direction.NORTHEAST;
        }
        else if(xDiff == 0 && yDiff == 1)
        {
            return Direction.SOUTH;
        }
        else if(xDiff == 0 && yDiff == -1)
        {
            return Direction.NORTH;
        }
        else if(xDiff == -1 && yDiff == 1)
        {
            return Direction.SOUTHWEST;
        }
        else if(xDiff == -1 && yDiff == 0)
        {
            return Direction.WEST;
        }
        else if(xDiff == -1 && yDiff == -1)
        {
            return Direction.NORTHWEST;
        }

        System.err.println("Invalid path. Could not determine direction");
        return null;
    }
}