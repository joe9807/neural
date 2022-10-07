package application.neurolevels;

public class NeuronWorker implements Runnable {
    private final Neuron neuron;

    public NeuronWorker(final Neuron neuron){
        this.neuron = neuron;
    }

    @Override
    public void run() {
        int index = 0;
        double result = 0;
        while (index != neuron.getWeights().size() || index != neuron.getValues().size()) {
            result+=neuron.getWeights().get(index)*neuron.getValues().get(index);
            index++;
        }
        neuron.setSum(result);
    }
}
