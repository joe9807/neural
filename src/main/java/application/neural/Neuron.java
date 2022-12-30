package application.neural;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Neuron {
    private List<Double> weights;
    private List<Double> input;
    private int number;
    private Double output = null;
    private Double value = null;

    public Neuron(int number, List<Double> weights, List<Double> input, Double value){
        this.number = number;
        this.weights = weights;
        this.input = input;
        this.value = value;
    }

    public Runnable getWorker(){
        return new NeuronWorker(this);
    }
}
