package application.neural;

import application.repository.entity.Weight;

import java.util.List;

public class NeuronWeightWorker implements Runnable{
    private static final double m = 0.4;

    private final Weight weight;
    private final List<Double> input;
    private final List<Double> delta;

    public  NeuronWeightWorker(Weight weight, List<Double> input, List<Double> delta){
        this.weight = weight;
        this.input = input;
        this.delta = delta;
    }

    @Override
    public void run() {
        weight.setValue(weight.getValue()+m*input.get(weight.getBackNumber())*delta.get(weight.getNumber()));
    }
}
