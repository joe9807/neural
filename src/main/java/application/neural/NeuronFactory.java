package application.neural;

import java.util.List;

public class NeuronFactory {
    public static Neuron getNeuron(int number, List<List<Double>> matrix, List<Double> input, List<Double> values){
        return new Neuron(number, matrix.size() == 0?null:matrix.get(number), input, values == null?null:values.get(number));
    }
}
