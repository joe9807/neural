package application.neurolevels;

public class NeuronWorker implements Runnable {
    private final Neuron neuron;

    public NeuronWorker(final Neuron neuron){
        this.neuron = neuron;
    }

    @Override
    public void run() {
        int index = 0;
        double v = 0;
        while (index != neuron.getWeights().size() || index != neuron.getValues().size()) {
            v+=neuron.getWeights().get(index)*neuron.getValues().get(index);
            index++;
        }
        if (neuron.getLevel() != 0) {
            activate(v);
        }
    }

    private void activate(double v){
        neuron.setSum(1/(1+Math.exp(-v)));
    }
}
