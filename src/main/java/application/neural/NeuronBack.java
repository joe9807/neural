package application.neural;

import lombok.Data;

import java.util.List;

@Data
public class NeuronBack extends Neuron {
    private List<Double> delta;

    public NeuronBack(int level, int number, List<Double> weights, List<Double> input, List<Double> delta) {
        super(level, number, weights, input);
        this.delta = delta;
    }

    public Runnable getWorker(){
        return new NeuronBackWorker(this);
    }
}
