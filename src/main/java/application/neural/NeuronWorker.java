package application.neural;

public class NeuronWorker implements Runnable {
    private static final double B = 1.0;
    private final Neuron neuron;

    public NeuronWorker(final Neuron neuron){
        this.neuron = neuron;
    }

    @Override
    public void run() {
        calculate();
    }

    public void calculate(){
        double v = 0;
        if (neuron.getWeights() == null) {
            v = neuron.getInput()[neuron.getNumber()]-neuron.getValue();
        } else {
            int index = 0;
            while (index != neuron.getWeights().length || index != neuron.getInput().length) {
                v+=neuron.getWeights()[index]*neuron.getInput()[index];
                index++;
            }
        }

        if (neuron.getValue() == null) {
            neuron.setOutput(1 / (1 + Math.exp(-B * v)));
        } else {
            neuron.setOutput(neuron.getValue() * (1 - neuron.getValue()) * v);
        }
    }
}
