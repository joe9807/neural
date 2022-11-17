package application.neural;

public class NeuronWorker implements Runnable {
    private static final double B = 1.0;
    private final Neuron neuron;

    public NeuronWorker(final Neuron neuron){
        this.neuron = neuron;
    }

    @Override
    public void run() {
        try{
            calculate();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void calculate() throws Exception{
        int index = neuron.getWeights().size();
        double v = 0;

        while (index-- != 0){
            NeuralSignal signal = neuron.getQueue().take();
            v+=signal.getValue()*neuron.getWeights().get(signal.getIndex());
        }

        neuron.setOutput(1/(1+Math.exp(-B*v)));
        send();
    }

    public void send(){
        int index = 0;
        for (Neuron neuron : neuron.getNeurons()) {
            neuron.getQueue().offer(new NeuralSignal(index++, neuron.getOutput()));
        }
    }
}
