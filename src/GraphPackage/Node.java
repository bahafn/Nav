package GraphPackage;

public class Node<type> {
    private type data;
    private int indexInGraph = 0;

    public Node(type data) { this.data = data; }

    public int getIndexInGraph() { return indexInGraph; }

    public void setIndexInGraph(int indexInGraph) { this.indexInGraph = indexInGraph; }

    public type getData() { return data; }
}
