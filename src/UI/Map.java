package UI;

import GraphPackage.Graph;
import GraphPackage.Node;

import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.LinkedList;

public class Map extends JPanel {
    private static final int POINT_SIZE = 10;

    private Graph<Dimension> points = new Graph<>();
    private int selectedPoint = -1; // -1 meaning no point selected
    private LinkedList<Integer> pathIndexes = new LinkedList<>();

    public Map() {
        setPreferredSize(new Dimension(600, 600));

        addMouseListener(new MouseAdapter() {
            @Override
            // on mouse click
            public void mouseClicked(MouseEvent e) {
                // index of the point the mouse is on
                int selectedIndex = mouseOnPoint(e.getX(), e.getY());

                // add new node if left mouse button is clicked
                if (e.getButton() == MouseEvent.BUTTON1 && selectedIndex == -1) {
                    points.addNode(new Node<>(new Dimension(e.getX(), e.getY())));
                    pathIndexes = new LinkedList<>();
                    repaint(); // repaints panel with newly added node
                }
                // adding edges (or roads) if right mouse button is clicked
                else if (e.getButton() == MouseEvent.BUTTON3) {
                    if (selectedIndex != -1 && selectedPoint != -1) {
                        points.addEdge(selectedPoint, selectedIndex, true);
                        pathIndexes = new LinkedList<>();
                    }

                    selectedPoint = selectedIndex;
                    repaint(); // repaints the panel with newly added edge or different color for selected node
                }
            }
        });
    }

    public void paint(Graphics g) {
        g.clearRect(0, 0, this.getWidth(), this.getHeight()); // used so graphics aren't drawn on top of each other

        for (int i = 0; i < points.size(); i++) {
            // draws a point for each node
            g.setColor(i == selectedPoint ? Color.ORANGE : Color.BLACK);

            Node<Dimension> node = points.getNode(i);
            g.fillOval(node.getData().width, node.getData().height, 10, 10);

            // draw the index of the point next to it
            // 1.5 used so the index is drawn outside the node
            g.drawString(String.valueOf(i), (int) (node.getData().width + POINT_SIZE * 1.5),
                    (int) (node.getData().height + POINT_SIZE * 1.5));
            node.setIndexInGraph(i); // change the index in graph if it doesn't line up with the number of nodes
                                     // used to make sure the shown number and the node's index are the same

            // draws a line on each edge
            g.setColor(Color.BLACK);

            for (Node<Dimension> dstNode : points.getEdgesList(i)) {
                if (dstNode.equals(node))
                    continue;

                g.drawLine(node.getData().width + POINT_SIZE / 2, node.getData().height + POINT_SIZE / 2,
                        dstNode.getData().width + POINT_SIZE / 2, dstNode.getData().height + POINT_SIZE / 2);
            }
        }

        // draw path edges. needs to be drawn after everything so normal edges (black
        // lines) aren't drawn on top of them
        if (pathIndexes.size() < 2)
            return;

        int startingNodeIndex = pathIndexes.get(0);

        for (int i : pathIndexes) {
            g.setColor(Color.GREEN);

            Node<Dimension> startingNode = points.getNode(startingNodeIndex);
            Node<Dimension> dstNode = points.getNode(i);

            startingNodeIndex = i;

            g.drawLine(startingNode.getData().width + POINT_SIZE / 2, startingNode.getData().height + POINT_SIZE / 2,
                    dstNode.getData().width + POINT_SIZE / 2, dstNode.getData().height + POINT_SIZE / 2);
        }
    }

    // returns the index of the point the mouse is on
    private int mouseOnPoint(int mousePosX, int mousePosY) {
        // loop over all points and check distance to the mouse
        for (int i = 0; i < points.size(); i++) {
            Node<Dimension> node = points.getNode(i);

            // muliplied by two so it's easier to click on the nodes
            if (getDistance(node.getData(), new Dimension(mousePosX, mousePosY)) <= POINT_SIZE * 2)
                return i;
        }

        return -1; // -1 meaning no point selected
    }

    private double getDistance(Dimension pos1, Dimension pos2) {
        return Math.sqrt(Math.pow(pos1.width - pos2.width, 2) + Math.pow(pos1.height - pos2.height, 2));
    }

    public void reset() {
        pathIndexes = new LinkedList<>();
        points = new Graph<>();
        selectedPoint = -1;
        repaint();
    }

    public void delete() {
        // stop the function if no node was selected
        if (selectedPoint == -1)
            return;

        Node<Dimension> removedNode = points.getNode(selectedPoint);

        // loop over all nodes and remove any edge with the selected node
        for (int i = 0; i < points.size(); i++)
            points.getEdgesList(i).remove(removedNode);

        points.removeNode(selectedPoint); // remove the node itself

        pathIndexes = new LinkedList<>(); // used so no problems happen while repainting the panel
        selectedPoint = -1; // the point at the selected index either changed or became out of bounds
        repaint(); // repaint without newly removed node
    }

    /**
     * Calculates the shortest path between two nodes. This function uses the least
     * distance method, which means it calculates the sum of the distance between
     * the starting node and every node it has an edge with. The function is then
     * called again with node that has the least sum as the starting node and the
     * same destination. this is done until the node with the least distance is the
     * destination node which means we found the path.
     * 
     * @return <code>true</code> if a path was found and <code>false</code> if
     *         non was found
     * 
     * @implNote While the least distance method works perfectly with nodes that are
     *           all connected to their neighbors (like in video game where the
     *           character can move in all directions), it has some problems with
     *           roads like this (not every node is connected to the nodes beside
     *           it). These problems are addressed with if statements for specific
     *           cases.
     */
    public boolean calculatePath(int index, int dst, LinkedList<Integer> ignore) {
        if (!points.isConnected(index, dst, new LinkedList<>()))
            return false;

        // this means that this is the first time the method was called (before any
        // recursion)
        if (ignore.size() == 0) {
            ignore.add(index); // add the starting node to the nodes we can't return to in the path
            pathIndexes = new LinkedList<>();
            pathIndexes.add(index);
        }

        LinkedList<Node<Dimension>> startingNodeEdges = points.getEdgesList(index); // edge list of starting node

        Node<Dimension> startingNode = startingNodeEdges.get(0);
        Node<Dimension> dstNode = points.getNode(dst);
        Node<Dimension> nextNode = getLeastDistance(startingNodeEdges, startingNode, dstNode, ignore);

        // make sure if the next node has an edge with the last node in the path
        // this is used because the algorithm of the least distance doesn't work in one
        // case (points shaped like a triangle) .if the next node has an edge with the
        // last path node, it means that there is an easier way without going to the
        // current node, and so we remove it from the path
        if (pathIndexes.size() >= 2
                && points.checkEdge(pathIndexes.get(pathIndexes.size() - 2), nextNode.getIndexInGraph()))
            pathIndexes.remove(pathIndexes.size() - 1);

        pathIndexes.add(nextNode.getIndexInGraph()); // add next node's index to the path array

        if (nextNode == dstNode) {
            repaint(); // repaint with newly added path
            return true;
        }

        ignore.add(nextNode.getIndexInGraph()); // add next node to checked nodes, so we can't return to it

        calculatePath(nextNode.getIndexInGraph(), dst, ignore);

        return true;
    }

    /**
     * this function takes a list of Node<Dimension> and returns the one with the
     * least sum of distances between it and the starting node
     * and the destination node
     */
    private Node<Dimension> getLeastDistance(LinkedList<Node<Dimension>> startingNodeEdges,
            Node<Dimension> startingNode, Node<Dimension> dstNode,
            LinkedList<Integer> ignore) {
        Node<Dimension> nextNode = null;

        double leastDistance = 10000000000.0;

        for (int i = 1; i < startingNodeEdges.size(); i++) {
            Node<Dimension> node = startingNodeEdges.get(i);

            if (ignore.contains(node.getIndexInGraph()))
                continue;

            // sum of the distance to the starting point and the distance to the destination point
            double distance = getDistance(startingNode.getData(), node.getData())
                    + getDistance(node.getData(), dstNode.getData());

            LinkedList<Node<Dimension>> ignoredNodes = new LinkedList<>(); // linked list sent to isConnected to make
                                                                           // sure the next node
            ignoredNodes.add(startingNode); // is connected to destination without returning to starting node

            // update next point if it's closer
            if (distance < leastDistance
                    && points.isConnected(node.getIndexInGraph(), dstNode.getIndexInGraph(), ignoredNodes)) {
                leastDistance = distance;
                nextNode = node;
            }
        }

        return nextNode;
    }
}
