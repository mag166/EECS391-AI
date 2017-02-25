package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class MinimaxAlphaBeta extends Agent {
    
    private final int numPlys;
    
    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);
        
        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }
        
        numPlys = Integer.parseInt(args[0]);
    }
    
    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }
    
    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                                                   numPlys,
                                                   Double.NEGATIVE_INFINITY,
                                                   Double.POSITIVE_INFINITY);
        
        return bestChild.action;
    }
    
    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {
        
    }
    
    @Override
    public void savePlayerData(OutputStream os) {
        
    }
    
    @Override
    public void loadPlayerData(InputStream is) {
        
    }
    
    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     *
     * @author Previn Kumar
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta)
    {
        return maxValue(node, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
    
    /**
     * Recursive function to return the child with the max utility
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The max child of this node with updated values
     * @author Previn Kumar
     */
    private GameStateChild maxValue(GameStateChild node, int depth, double alpha, double beta) {
        GameState state = node.state;
        // returns node if at the depth limit or a goal
        if (depth <= 0 || state.areArchersDead()) {
            node.state.setSavedUtility(state.getUtility());
            return node;
        }
        double v = Double.NEGATIVE_INFINITY;
        List<GameStateChild> children = orderChildrenWithHeuristics(state.getChildren(0));
        GameStateChild best_child = null;
        // find the child with the best utility
        for (GameStateChild child : children) {
            GameStateChild min_child = minValue(child, --depth, alpha, beta);
            // Pick a child with maximum utility
            if (min_child.state.getSavedUtility() > v) {
                v = min_child.state.getSavedUtility();
                best_child = child;
                best_child.state.setSavedUtility(v);
                // prunes the rest of the nodes if >= beta
                if (v >= beta) {
                    return best_child;
                }
                alpha = v > alpha ? v : alpha;
            }
        }
        return best_child;
    }
    
    /**
     * Recursive function to return the child with the min utility
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The min child of this node with updated values
     * @author Previn Kumar
     */
    private GameStateChild minValue(GameStateChild node, int depth, double alpha, double beta) {
        GameState state = node.state;
        // returns node if at the depth limit or a goal
        if (depth <= 0 || state.areArchersDead()) {
            node.state.setSavedUtility(state.getUtility());
            return node;
        }
        double v = Double.POSITIVE_INFINITY;
        List<GameStateChild> children = orderChildrenWithHeuristics(state.getChildren(1));
        GameStateChild best_child = null;
        // find the child with the best utility
        for (GameStateChild child : children) {
            GameStateChild max_child = maxValue(child, --depth, alpha, beta);
            // Pick the child with minimum utility
            if (max_child.state.getSavedUtility() < v) {
                v = max_child.state.getSavedUtility();
                best_child = child;
                best_child.state.setSavedUtility(v);
                // prunes the rest of the nodes if <= beta
                if (v <= alpha) {
                    return best_child;
                }
                beta = v < beta ? v : beta;
            }
        }
        return best_child;
    }
    
    /**
     *
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     *
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     * @author Minhal Gardezi
     */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
    {
        if(children.isEmpty()){
            return children;
        }
        
        GameStateChild holder;
        
        for (int i = 1; i < children.size(); i++) {
            holder = children.get(i);
            int j = i - 1;
            while(j >= 0 && holder.state.getUtility() > children.get(j).state.getUtility()){
                children.add(j + 1, children.get(j));
                children.remove(j + 2);
                j--;
            }
            children.add(j + 1, holder);
            children.remove(j + 2);
        }
        return children;
    }
}
