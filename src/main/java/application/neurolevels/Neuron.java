package application.neurolevels;

import lombok.Data;

import java.util.List;

@Data
public class Neuron implements Comparable<Neuron>{
    private int number;
    private List<Double> weights;
    private List<Double> values;
    private double sum;

    public Neuron(int number, List<Double> weights, List<Double> values){
        this.number = number;
        this.weights = weights;
        this.values = values;
    }

    @Override
    public int compareTo(Neuron o) {
        return Integer.compare(o.number, number);
    }
}
