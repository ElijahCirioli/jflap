package automata.graph.layout;


import automata.graph.Graph;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * A helper class for the Force-Directed layout algorithm that turns vertices
 * into pseudo-physics objects
 *
 * @see ForceDirectedLayoutAlgorithm
 * @author Elijah Cirioli
 */
public class PhysicsVertex {
    final double ATTRACTION_SCALAR = 1;
    final double REPULSION_SCALAR = 1;

    private Graph graph;
    private Point2D.Double displacement;
    public Point2D.Double position;
    public Object vertex;

    public PhysicsVertex(Object v, Graph g) {
        graph = g;
        vertex = v;
        position = new Point2D.Double(graph.pointForVertex(v).getX(), graph.pointForVertex(v).getY());
        displacement = new Point2D.Double(0, 0);
    }


    public void applyForces(ArrayList<PhysicsVertex> vertices, HashSet<Object> neighborhood, double k, double temperature) {
        displacement.setLocation(0,0);
        for (PhysicsVertex v : vertices) {
            if (v.equals(this)) {
                continue;
            }
            /* repulsive forces */
            Point2D.Double repulsionForce = repulsionForce(v, k);
            displacement.x += repulsionForce.x * REPULSION_SCALAR;
            displacement.y += repulsionForce.y * REPULSION_SCALAR;
            if (neighborhood.contains(v.vertex)) {
                /* attractive forces */
                Point2D.Double attractionForce = attractionForce(v, k);
                displacement.x += attractionForce.x * ATTRACTION_SCALAR;
                displacement.y += attractionForce.y * ATTRACTION_SCALAR;
            }
        }
        /* max length of displacement at temperature */
        double dispMagnitude = calculateMagnitude(displacement);
        if (dispMagnitude > temperature) {
            displacement.x *= temperature / dispMagnitude;
            displacement.y *= temperature / dispMagnitude;
        }

        /* move the vertex */
        position.x += displacement.x;
        position.y += displacement.y;
    }

    private double calculateMagnitude(Point2D.Double v) {
        return Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y, 2));
    }

    private Point2D.Double repulsionForce(PhysicsVertex v, double k) {
        Point2D.Double difference = new Point2D.Double(v.position.x - position.x, v.position.y - position.y );
        double distance = calculateMagnitude(difference);
        if (distance < 1)
            distance = 1;
        double force = -Math.pow(k, 2) / distance ;
        return new Point2D.Double(force * difference.x / distance, force * difference.y  / distance);
    }

    private Point2D.Double attractionForce(PhysicsVertex v, double k) {
        Point2D.Double difference = new Point2D.Double(v.position.x - position.x, v.position.y - position.y );
        double distance = calculateMagnitude(difference);
        if (distance < 1)
            distance = 1;
        double force = Math.pow(distance, 2) / k ;
        return new Point2D.Double(force * difference.x / distance, force * difference.y  / distance);
    }
}