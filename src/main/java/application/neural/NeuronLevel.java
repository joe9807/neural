package application.neural;

import application.repository.WeightRepository;
import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class NeuronLevel {
    private static final int CORE = 20;
    private final ThreadPoolExecutor executor;

    @Autowired
    private WeightRepository weightRepository;

    public NeuronLevel(){
        executor = new ThreadPoolExecutor(CORE, CORE, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(CORE));
    }

    private List<Neuron> getNeurons(int level, List<Double> input) {
        List<Neuron> neurons = new ArrayList<>();
        List<Weight> allWeights = weightRepository.findAllByLevel(level);

        final AtomicInteger number = new AtomicInteger();
        List<Double> neuronWeights;
        while ((neuronWeights = allWeights.stream().filter(weight-> weight.getNumber() == number.get()).sorted().map(Weight::getValue).collect(Collectors.toList())).size() !=0) {
            neurons.add(new Neuron(level, number.getAndIncrement(), neuronWeights, input));
        }
        return neurons;
    }

    public List<Double> calculate(int level, List<Double> input){
        final List<Neuron> neurons = getNeurons(level, input);
        final List<Neuron> processed = new ArrayList<>();
        final List<Future<?>> futures = new ArrayList<>();

        while (neurons.size() != 0) {
            for (Neuron neuron : neurons) {
                if (executor.getQueue().size() == CORE) continue;

                futures.add(executor.submit(new NeuronWorker(neuron)));
                processed.add(neuron);
            }

            neurons.removeAll(processed);
        }

        while (futures.size() != 0) futures.removeIf(Future::isDone);

        return processed.stream().sorted().map(Neuron::getOutput).collect(Collectors.toList());
    }
}
