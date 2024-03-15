package GraphPackage;

import java.util.ArrayList;
import java.util.LinkedList;

public class Graph<type> {
    private ArrayList<LinkedList<Node<type>>> nodes = new ArrayList<>();

    public void addNode(Node<type> node) {
        LinkedList<Node<type>> list = new LinkedList<>(); // new linked list for the new node
        list.add(node); // first index is always the node itself
        node.setIndexInGraph(nodes.size());
        nodes.add(list);
    }

    public void removeNode(int index) {
        nodes.remove(index);
    }

    public void addEdge(int index, int dst, boolean twoWayEdge) {
        // don't add edge if it already exists
        if (checkEdge(index, dst))
            return;

        getEdgesList(index).add(getNode(dst)); // adds the main edge
        // adds the edge the other way around
        if (twoWayEdge)
            addEdge(dst, index, false);
    }

    public boolean checkEdge(int index, int dst) { return getEdgesList(index).contains(getNode(dst)); }

    public boolean isConnected(int index, int dst, LinkedList<Node<type>> checkedNodes) {
        // if there is an edge between the nodes, they are connected
        if (checkEdge(index, dst))
            return true;

        // otherwise, if any of the nodes with edges has an edge, the nodes are connected
        for (int i = 1; i < getEdgesList(index).size(); i++) {
            Node<type> node = getEdgesList(index).get(i);

            // ignore the starting node and any other already checked node. can enter an
            // endless recursion if removed because two nodes keep returning to each other
            if (checkedNodes.contains(node))
                continue;

            if (checkEdge(index, dst))
                return true;
            else {
                checkedNodes.add(node);

                // DON'T return the value of isConnected for the new node
                // we don't want to return false if one sibling is not connected but another is
                if (isConnected(node.getIndexInGraph(), dst, checkedNodes))
                    return true;
            }
        }

        // if we get here, the nodes aren't connected
        return false;
    }

    public Node<type> getNode(int index) { return nodes.get(index).get(0); }

    public LinkedList<Node<type>> getEdgesList(int index) { return nodes.get(index); }

    public int size() { return nodes.size(); }
}
