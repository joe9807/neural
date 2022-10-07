package application.neurolevels;

import lombok.Data;

import java.util.List;

@Data
public class Neuron implements Comparable<Neuron>{
    private List<Double> weights;
    private List<Double> values;
    private int level;
    private int number;
    private double output;

    public Neuron(int level, int number, List<Double> weights, List<Double> values){
        this.level = level;
        this.number = number;
        this.weights = weights;
        this.values = values;
    }

    @Override
    public int compareTo(Neuron o) {
        return Integer.compare(o.number, number);
    }
}
