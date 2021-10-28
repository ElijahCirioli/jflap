package automata.graph.layout;

import java.awt.Dimension;
import java.util.*;
import java.lang.Math;

import automata.graph.Graph;
import automata.graph.LayoutAlgorithm;

/**
 * An implementation of a barycentric force-directed graph drawing algorithm as described
 * here https://cs.brown.edu/people/rtamassi/gdhandbook/chapters/force-directed.pdf
 *
 * @see LayoutAlgorithm
 * @author Elijah Cirioli
 */
public class ForceDirectedLayoutAlgorithm extends LayoutAlgorithm {
    /**
     * The graph used for this LayoutAlgorithm
     */
    private Graph graph;
    private final int NUM_ITERATIONS = 5000;

    /**
     * Assigns some default values.  To have different values, use the other constructor.
     */
    public ForceDirectedLayoutAlgorithm() {
        super();
    }

    /**
     * Constructor allowing the user to customize certain values.
     *
     * @param pSize   value for <code>size</code>.
     * @param vDim    value for <code>vertexDim</code>.
     * @param vBuffer value for <code>vertexBuffer</code>.
     */
    public ForceDirectedLayoutAlgorithm(Dimension pSize, Dimension vDim, double vBuffer) {
        super(pSize, vDim, vBuffer);
    }


    public void layout(Graph g, Set notMoving) {
        graph = g;
        ArrayList vertices = getMovableVertices(graph, notMoving);
        if (graph == null || vertices.size() == 0)
            return;

        /* wrap the graph vertices in physics objects and create HashMap of undirected neighborhoods */
        HashMap<Object, HashSet<Object>> allNeighborhoods = new HashMap<>();
        ArrayList<PhysicsVertex> physicsVertices = new ArrayList<>(vertices.size());
        for (Object v : vertices) {
            physicsVertices.add(new PhysicsVertex(v, g));
            if (!allNeighborhoods.containsKey(v))
                allNeighborhoods.put(v, new HashSet<>());
            for (Object other : vertices) {
                if (other.equals(v) || !graph.adjacent(v).contains(other)) {
                    continue;
                }
                if (!allNeighborhoods.containsKey(other))
                    allNeighborhoods.put(other, new HashSet<>());
                allNeighborhoods.get(v).add(other);
                allNeighborhoods.get(other).add(v);
            }
        }

        /* add fake connections if the graph is disconnected */
        Object firstVertex = vertices.get(0);
        Set<Object> connections = getConnected(firstVertex);
        if (vertices.size() != connections.size()) {
            for (Object v : vertices) {
                /* if this vertex is disconnected from the start */
                if (!connections.contains(v)) {
                    /* add it to the first vertex's neighborhood */
                    allNeighborhoods.get(firstVertex).add(v);
                    allNeighborhoods.get(v).add(firstVertex);
                }
            }
        }

        /* define important variables */
        double width =  size.getWidth() - 2 * vertexBuffer;
        double height =  size.getHeight() - 2 * vertexBuffer;
        double area = width * height;
        double k = Math.min(Math.sqrt(area / physicsVertices.size()), 4 * vertexDim.getWidth());

        /* run the physics on the vertices */
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            /* temperature cools over time and describes how much the vertices can move */
            double temperature = (width / (5 * Math.sqrt(NUM_ITERATIONS))) * Math.sqrt(NUM_ITERATIONS - i);
            for (PhysicsVertex v : physicsVertices) {
                v.applyForces(physicsVertices, allNeighborhoods.get(v.vertex), k, temperature);
            }
        }

        /* update the positions of the actual graph vertices */
        for (PhysicsVertex v : physicsVertices) {
            graph.moveVertex(v.vertex, v.position);
        }
        shiftOntoScreen(graph, size, vertexDim, vertexBuffer, true);
    }

    private Set<Object> getConnected(Object startingVertex) {
        HashSet<Object> visited = new HashSet<>();
        LinkedList<Object> queue = new LinkedList<>();
        visited.add(startingVertex);
        queue.add(startingVertex);

        while (queue.size() > 0) {
            Object curr = queue.remove();
            Set<Object> neighborhood = graph.adjacent(curr);
            for (Object v : neighborhood) {
               if (!visited.contains(v)) {
                   visited.add(v);
                   queue.addLast(v);
               }
            }
        }

        return visited;
    }
}
