package application.neural;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Data
@NoArgsConstructor
public class Neuron implements Comparable<Neuron>{
    private List<Double> weights;
    private List<Double> input;
    private List<Neuron> neurons;
    private int number;
    private double output;
    private BlockingQueue<NeuralSignal> queue;

    public Neuron(int number, List<Double> weights, List<Double> input){
        this.number = number;
        this.weights = weights;
        this.input = input;
        this.queue = new ArrayBlockingQueue<>(weights.size());
    }

    @Override
    public int compareTo(Neuron o) {
        return Integer.compare(number, o.number);
    }

    public Runnable getWorker(){
        return new NeuronWorker(this);
    }
}
