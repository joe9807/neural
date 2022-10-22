package application.neural;

import application.repository.entity.Weight;

import java.util.List;

public class NeuronWeightWorker implements Runnable{
    private final Weight weight;
    private final List<Double> input;
    private final List<Double> delta;
    private final NeuralParameters parameters;

    public NeuronWeightWorker(Weight weight, List<Double> input, List<Double> delta, NeuralParameters parameters){
        this.weight = weight;
        this.input = input;
        this.delta = delta;
        this.parameters = parameters;
    }

    @Override
    public void run() {
        try {
            weight.setValue(weight.getValue() + Double.parseDouble(parameters.getM()) * input.get(weight.getBackNumber()) * delta.get(weight.getNumber()));
        }catch(Exception e){
            System.out.printf("input.size() %s weight.getBackNumber() %s delta.size() %s weight.getNumber() %s%n", input.size(), weight.getBackNumber(), delta.size(), weight.getNumber());
        }
    }
}
