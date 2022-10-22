package application.neural;

import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NeuronExecutor {
    @Autowired
    private NeuralRepository neuralRepository;

    protected List<Neuron> getNeurons(int level, List<Double> input) {
        List<Neuron> neurons = new ArrayList<>();
        List<Weight> allWeights = neuralRepository.findAllByLevel(level);

        final AtomicInteger number = new AtomicInteger();
        List<Double> neuronWeights;
        while ((neuronWeights = allWeights.stream().filter(weight-> weight.getNumber() == number.get()).sorted().map(Weight::getValue).collect(Collectors.toList())).size() !=0) {
            neurons.add(new Neuron(level, number.getAndIncrement(), neuronWeights, input));
        }
        return neurons;
    }

    protected List<Neuron> getNeurons(int level, List<Double> input, List<Double> delta) {
        List<Neuron> neurons = new ArrayList<>();
        List<Weight> allWeights = neuralRepository.findAllByLevel(level);

        final AtomicInteger number = new AtomicInteger();
        IntStream.range(0, input.size()).forEach(backNumber->
                neurons.add(new NeuronBack(level-1, number.getAndIncrement(), allWeights.stream().filter(weight->
                        weight.getBackNumber() == backNumber).sorted().map(Weight::getValue).collect(Collectors.toList()), input, delta)));

        return neurons;
    }

    public void calculateWeights(List<Weight> weights, List<Double> input, List<Double> delta, NeuralParameters parameters){
        final ForkJoinPool executor = new ForkJoinPool(200);

        final List<Weight> processed = new ArrayList<>();
        final List<Future<?>> futures = new ArrayList<>();

        while (weights.size() != 0) {
            for (Weight weight : weights) {
                futures.add(executor.submit(new NeuronWeightWorker(weight, input, delta, parameters)));
                processed.add(weight);
            }

            weights.removeAll(processed);
        }

        futures(futures);
        weights.addAll(processed);
    }

    public List<Double> calculate(int level, List<Double> input, List<Double> delta){
        final ForkJoinPool executor = new ForkJoinPool(200);

        final List<Neuron> neurons = delta == null?getNeurons(level, input):getNeurons(level, input, delta);
        if (neurons == null || neurons.isEmpty()) return null;
        final List<Neuron> processed = new ArrayList<>();
        final List<Future<?>> futures = new ArrayList<>();

        while (neurons.size() != 0) {
            for (Neuron neuron : neurons) {
                futures.add(executor.submit(neuron.getWorker()));
                processed.add(neuron);
            }

            neurons.removeAll(processed);
        }

        futures(futures);
        return processed.stream().sorted().map(Neuron::getOutput).collect(Collectors.toList());
    }

    private void futures(List<Future<?>> futures){
        while (!futures.isEmpty()) {
            List<Future<?>> remove = new ArrayList<>();
            for (Future<?> future:futures){
                if (future.isDone()){
                    try {
                        future.get();
                        remove.add(future);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            futures.removeAll(remove);
        }
    }
}
