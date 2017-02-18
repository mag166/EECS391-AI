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
        double utility = maxValue(node, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        GameState state = node.state;
        List<GameStateChild> children = state.getChildren();
        // find the child with the best utility
        for (GameStateChild child : children) {
            if (child.state.getUtility() == utility) {
                return child;
            }
        }
        return null;
    }
    
    /**
     * Recursive function to calculate the max utility value of a state
     * @param state  Possible child state to examine
     * @return The utility value of the state
     * @author Previn Kumar
     */
    private double maxValue(GameStateChild node, int depth, double alpha, double beta) {
        GameState state = node.state;
        // returns node if at the depth limit or a goal
        if (depth == 0 || state.isArcherAdjacent()) {
            return node.state.getUtility();
        }
        double v = Double.NEGATIVE_INFINITY;
        List<GameStateChild> children = state.getChildren();
        // find the child with the best utility
        for (GameStateChild child : children) {
            double min = minValue(child, --depth, alpha, beta);
            if (min > v) {
                v = min;
                if (v >= beta) {
                    return v;
                }
                alpha = v > alpha ? v : alpha;
            }
        }
        return v;
    }
    
    /**
     * Recursive function to calculate the min utility value of a state
     * @param state  Possible child state to examine
     * @return The utility value of the state
     * @author Previn Kumar
     */
    private double minValue(GameStateChild node, int depth, double alpha, double beta) {
        GameState state = node.state;
        // returns node if at the depth limit or a goal
        if (depth == 0 || state.isArcherAdjacent()) {
            return node.state.getUtility();
        }
        double v = Double.POSITIVE_INFINITY;
        List<GameStateChild> children = state.getChildren();
        // find the child with the worst utility
        for (GameStateChild child : children) {
            double max = maxValue(child, --depth, alpha, beta);
            if (max > v) {
                v = max;
                if (v <= alpha) {
                    return v;
                }
                beta = v < beta? v : beta;
            }
        }
        return v;
    }

    /**
     * You will implement this.
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
     */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
    {
        return children;
    }
}
