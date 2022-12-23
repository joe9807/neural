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
    private Double value;

    public Neuron(int number, List<Double> weights, List<Double> input, Double value){
        this.number = number;
        this.weights = weights;
        this.input = input;
        this.value = value;
    }

    @Override
    public int compareTo(Neuron o) {
        return Integer.compare(number, o.number);
    }

    public Runnable getWorker(){
        return new NeuronWorker(this);
    }
}
