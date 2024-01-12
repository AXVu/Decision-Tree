import java.util.ArrayList;
import java.util.Random;
import java.util.Objects;
import java.util.Arrays;
import java.util.HashMap;

// main Tree class
public class Tree {
    int numFeatures;
    ArrayList<String> featureNames;
    ArrayList<ArrayList<String>> featureStates;
    int maxDepth;
    int considerations;
    Node root;
    char type;
    
    // Default constructor
    public Tree(int numFeatures, int maxDepth, int considerations, char type) {
        this.maxDepth = maxDepth;
        this.numFeatures = numFeatures;
        this.type = type;
        this.considerations = considerations;
    }

    // Function to train the tree based on a dataset already converted to featurelabel
    public void buildFromData(ArrayList<FeatureLabel> dataset) {

        this.root = new Node();

        // Arraylist of Nodes as a stack to depth first populate the tree
        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<ArrayList<FeatureLabel>> datasets = new ArrayList<ArrayList<FeatureLabel>>();
        ArrayList<Integer> depths = new ArrayList<Integer>();
        nodes.add(this.root);
        datasets.add(dataset);
        depths.add(0);

        // Build out the rest of the tree
        boolean end = false;
        while (!end) {
            Node workingNode = nodes.removeLast();
            ArrayList<FeatureLabel> workingData = datasets.removeLast();
            int currentDepth = depths.removeLast();

            DataState split = findSplit(workingData);
            workingNode.condition = split.state;
            if (split.datasets.entropy() == 0 || currentDepth >= this.maxDepth) {
                HashMap<String, Integer> labelFreq = new HashMap<String, Integer>();
                
            }

            workingNode.trueNode = new Node();
            nodes.add(workingNode.trueNode);
            datasets.add(split.datasets.first);

            workingNode.falseNode = new Node();
            nodes.add(workingNode.falseNode);
            datasets.add(split.datasets.second);
        }
        
    }

    // Function to find a good split in a dataset, and returns each split as an array
    public DataState findSplit(ArrayList<FeatureLabel> data) {
        Random rand = new Random();

        ArrayList<State> attemptStates = new ArrayList<State>();
        ArrayList<FeatureLabelArrayPair> attemptDatasets = new ArrayList<FeatureLabelArrayPair>();
        ArrayList<Double> attemptEntropy = new ArrayList<Double>();

        // for each consideration, find a random pairing and determine the "entropy" associated with it
        for (int i = 0; i < this.considerations; i++) {
            State pairing;
            // First find a pairing that hasn't been made yet
            do {
                int fIndex = rand.nextInt(this.numFeatures);
                int sIndex = rand.nextInt(this.featureStates.get(fIndex).size());
                pairing = new State(fIndex, this.featureStates.get(fIndex).get(sIndex));
            } while (!attemptStates.contains(pairing));
            

            attemptStates.add(pairing);
            attemptDatasets.add(split(data, pairing));
            attemptEntropy.add(attemptDatasets.getLast().entropy());
        }

        // find which split was the best by idx
        double minEntropy = 2;
        int minIdx = -1;
        for (int i = 0; i < attemptStates.size(); i++) {
            if (attemptEntropy.get(i) < minEntropy) {
                minEntropy = attemptEntropy.get(i);
                minIdx = i;
            }
        }

        return (new DataState(attemptDatasets.get(minIdx), attemptStates.get(minIdx)));

    }

    // splits a dataset based on the presence of a specific feature's state. Returns as a list containing the two
    // datasets, each with two lists inside of it, one with the features, the other with labels
    public FeatureLabelArrayPair split(ArrayList<FeatureLabel> data, State statePair) {

        ArrayList<FeatureLabel> inGroup = new ArrayList<FeatureLabel>();
        ArrayList<FeatureLabel> outGroup = new ArrayList<FeatureLabel>();

        // cycle through datapoints, checking whether the condition is true. If it is, add it to the inGroup otherwise, the outGroup
        for (FeatureLabel sample : data) {
            if (sample.features.get(statePair.idx).equals(statePair.label)) {
                inGroup.add(sample);
            } else {
                outGroup.add(sample);
            }
        }

        return new FeatureLabelArrayPair(inGroup, outGroup);
    }

    // function to convert from arrays to an Arraylist of FeatureLabel
    public ArrayList<FeatureLabel> ArraysToFeatureLabels(String[][] features, String[] labels) {

        // check for equal lengths of features and labels
        if (features.length != labels.length) {
            System.err.println("Array dimenstions do not match");
            return new ArrayList<FeatureLabel>();
        }

        ArrayList<FeatureLabel> dataset = new ArrayList<FeatureLabel>();
        for (int i = 0; i < features.length; i++) {
            dataset.add(new FeatureLabel(new ArrayList<>(Arrays.asList(features[i])), labels[i]));
        }

        return dataset;
    }

    // function to convert from Arraylists to an Arraylist of FeatureLabel
    public ArrayList<FeatureLabel> ArraysToFeatureLabels(ArrayList<ArrayList<String>> features, ArrayList<String> labels) {

        // check for equal lengths of features and labels
        if (features.size() != labels.size()) {
            System.err.println("Array dimenstions do not match");
            return new ArrayList<FeatureLabel>();
        }

        ArrayList<FeatureLabel> dataset = new ArrayList<FeatureLabel>();
        for (int i = 0; i < features.size(); i++) {
            dataset.add(new FeatureLabel(features.get(i), labels.get(i)));
        }

        return dataset;
    }

    // function to find the entropy of a single dataset
    public double arrayEntropy(ArrayList<FeatureLabel> labels) {
        return 0.0;
    }

    // function to return the most common string in an ArrayList of 

}

class Node {
    State condition;
    Node trueNode;
    Node falseNode;
    boolean isLeaf;
    String leafLabel;

    // Constructor for null node
    public Node() {

    }

    // Constructor for Split node
    public Node(int featIndex, String featState) {
        this.isLeaf = false;
        this.condition.idx = featIndex;
        this.condition.label = featState;
    }

    // Constructor for Split node using a State
    public Node(State state) {
        this.isLeaf = false;
        this.condition.idx = state.idx;
        this.condition.label = state.label;
    }

    // Constructor for leaf node
    public Node(String outputClass) {
        this.isLeaf = true;
        this.leafLabel = outputClass;
    }

    // Recursive operation for determining class
    public String determine(ArrayList<String> features) {
        // Base case, return the class for the determined leaf if available
        if (this.isLeaf) {
            return this.leafLabel;
        }
        // Otherwise, go a layer deeper and query a node based whether the node's state is true or false
        if (features.get(this.condition.idx).equals(this.condition.label)) {
            return this.trueNode.determine(features);
        } else {
            return this.falseNode.determine(features);
        }
    }
}

// class FeatureLabel contains both an Array of feature Strings and the corresponding label
class FeatureLabel {
    ArrayList<String> features;
    String label;

    // default constructor
    public FeatureLabel(ArrayList<String> features, String label) {
        this.features = features;
        this.label = label;
    }
}

// class FeatureLabelArrayPair is made to allow the output of Arrays of FeatureLabel
class FeatureLabelArrayPair {
    ArrayList<FeatureLabel> first;
    ArrayList<FeatureLabel> second;

    // default constructor
    public FeatureLabelArrayPair(ArrayList<FeatureLabel> first, ArrayList<FeatureLabel> second) {
        this.first = first;
        this.second = second;
    }

    // INCOMPLETE entropy determines the entropy of this split
    public double entropy() {
        return 0.0;
    }
}

// class State contains an index and string label
class State {
    int idx;
    String label;

    // default constructor
    public State(int idx, String label) {
        this.idx = idx;
        this.label = label;
    }

    // override for equals for state
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        State compare = (State) obj;
        return (compare.idx == this.idx && compare.label.equals(this.label));
    }

    // override for hashCode
    @Override
    public int hashCode() {
        return Objects.hash(this.idx, this.label);
    }

}

// class DataState is a bundle to output a split dataset and a state
class DataState {
    FeatureLabelArrayPair datasets;
    State state;

    // default constructor
    public DataState(FeatureLabelArrayPair data, State state) {
        this.datasets = data;
        this.state = state;
    }
}