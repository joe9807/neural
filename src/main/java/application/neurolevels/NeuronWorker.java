package application.neurolevels;

import java.util.concurrent.Callable;

public class NeuronWorker implements Callable<Double> {
    private final Neuron neuron;

    public NeuronWorker(final Neuron neuron){
        this.neuron = neuron;
    }

    @Override
    public Double call() {
        int index = 0;
        double result = 0;
        while (index != neuron.getWeights().size() || index != neuron.getValues().size()) {
            result+=neuron.getWeights().get(index)*neuron.getValues().get(index);
            index++;
        }
        return result;
    }
}
