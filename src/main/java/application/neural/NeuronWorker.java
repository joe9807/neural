package application.neural;

public class NeuronWorker implements Runnable {
    private static final double B = 1.0;
    private final Neuron neuron;

    public NeuronWorker(final Neuron neuron){
        this.neuron = neuron;
    }

    @Override
    public void run() {
        int index = 0;
        double v = 0;
        while (index != neuron.getWeights().size() || index != neuron.getInput().size()) {
            v+=neuron.getWeights().get(index)*neuron.getInput().get(index);
            index++;
        }
        neuron.setOutput(activate(v));
    }

    private double activate(double v){
        if (neuron.getLevel() == 0) return v;
        return 1/(1+Math.exp(-B*v));
    }
}
