package application.neural;

import application.repository.WeightRepository;
import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class NeuronExecutor {
    @Autowired
    private WeightRepository weightRepository;

    protected List<Neuron> getNeurons(int level, List<Double> input, List<Double> delta) {
        List<Neuron> neurons = new ArrayList<>();
        List<Weight> allWeights = weightRepository.findAllByLevel(level);

        final AtomicInteger number = new AtomicInteger();
        List<Double> neuronWeights;
        while ((neuronWeights = allWeights.stream().filter(weight-> weight.getNumber() == number.get()).sorted().map(Weight::getValue).collect(Collectors.toList())).size() !=0) {
            neurons.add(new Neuron(level, number.getAndIncrement(), neuronWeights, input));
        }
        return neurons;
    }

    public void calculateWeights(List<Weight> weights, List<Double> input, List<Double> delta){
        final ForkJoinPool executor = new ForkJoinPool(20);

        final List<Weight> processed = new ArrayList<>();
        final List<Future<?>> futures = new ArrayList<>();

        while (weights.size() != 0) {
            for (Weight weight : weights) {
                futures.add(executor.submit(new NeuronWeightWorker(weight, input, delta)));
                processed.add(weight);
            }

            weights.removeAll(processed);
        }

        while (!futures.isEmpty()) futures.removeIf(Future::isDone);
        weights.addAll(processed);
    }

    public List<Double> calculate(int level, List<Double> input, List<Double> delta){
        final ForkJoinPool executor = new ForkJoinPool(20);

        final List<Neuron> neurons = getNeurons(level, input, delta);
        final List<Neuron> processed = new ArrayList<>();
        final List<Future<?>> futures = new ArrayList<>();

        while (neurons.size() != 0) {
            for (Neuron neuron : neurons) {
                futures.add(executor.submit(neuron.getWorker()));
                processed.add(neuron);
            }

            neurons.removeAll(processed);
        }

        while (!futures.isEmpty()) futures.removeIf(Future::isDone);

        return processed.stream().sorted().map(Neuron::getOutput).collect(Collectors.toList());
    }
}
