package application.neural;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class Neuron implements Comparable<Neuron>{
    private List<Double> weights;
    private List<Double> input;
    private int number;
    private double output;

    public Neuron(int number, List<Double> weights, List<Double> input){
        this.number = number;
        this.weights = weights;
        this.input = input;
    }

    @Override
    public int compareTo(Neuron o) {
        return Integer.compare(number, o.number);
    }

    public Runnable getWorker(){
        return new NeuronWorker(this);
    }
}
