package application.neurolevels;

public class NeuronWorker implements Runnable {
    private static final double B = 1/20.0;
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
        } else {
            neuron.setOutput(v);
        }
    }

    private void activate(double v){
        neuron.setOutput(1/(1+Math.exp(-B*v)));
    }
}
