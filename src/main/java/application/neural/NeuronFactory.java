package application.neural;

public class NeuronFactory {
    public static Neuron getNeuron(int number, double[][] matrix, double[] input, double[] values){
        return new Neuron(number, matrix.length == 0?null:matrix[number], input, values == null?null:values[number]);
    }
}
