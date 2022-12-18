package application.neural;

import java.util.List;

public class NeuronFactory {
    public static Neuron getNeuron(int number, List<List<Double>> matrix, List<Double> input, List<Double> delta){
        if (delta == null) return new Neuron(number, matrix.get(number), input);
        return new NeuronBack(number, matrix.size() == 0?null:matrix.get(number), input, delta);
    }
}
