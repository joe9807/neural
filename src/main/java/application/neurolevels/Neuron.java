package application.neurolevels;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class Neuron implements Comparable<Neuron>{
    private List<Double> weights;
    private List<Double> input;
    private int level;
    private int number;
    private double output;

    public Neuron(int level, int number, List<Double> weights, List<Double> input){
        this.level = level;
        this.number = number;
        this.weights = weights;
        this.input = level == 0 ? Collections.singletonList(input.get(number)):input;
    }

    @Override
    public int compareTo(Neuron o) {
        return Integer.compare(o.number, number);
    }
}
