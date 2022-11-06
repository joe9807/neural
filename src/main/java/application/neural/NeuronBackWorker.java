package application.neural;

public class NeuronBackWorker implements Runnable{
    private final NeuronBack neuron;

    public NeuronBackWorker(final NeuronBack neuron){
        this.neuron = neuron;
    }

    @Override
    public void run() {
       try{
           calculate();
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    private void calculate(){
        double inputK = neuron.getInput().get(neuron.getNumber());
        double delta = 0;

        if (neuron.getWeights() == null) {
            delta = neuron.getDelta().get(neuron.getNumber())-inputK;
        } else {
            int index = 0;
            while (index != neuron.getWeights().size() || index != neuron.getDelta().size()) {
                delta+=neuron.getWeights().get(index)*neuron.getDelta().get(index);
                index++;
            }
        }

        neuron.setOutput(inputK*(1-inputK)*delta);
    }
}
