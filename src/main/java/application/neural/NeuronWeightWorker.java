package application.neural;

import application.repository.entity.Weight;

import java.util.List;

public class NeuronWeightWorker implements Runnable{
    private final Weight weight;
    private final List<Double> input;
    private final List<Double> delta;
    private final double m;

    public NeuronWeightWorker(Weight weight, List<Double> input, List<Double> delta, double m){
        this.weight = weight;
        this.input = input;
        this.delta = delta;
        this.m = m;
    }

    @Override
    public void run() {
        try{
            calculate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void calculate(){
        weight.setValue(weight.getValue() + m * input.get(weight.getBackNumber()) * delta.get(weight.getNumber()));
    }
}
