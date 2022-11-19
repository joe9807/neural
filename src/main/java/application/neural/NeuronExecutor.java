package application.neural;

import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class NeuronExecutor {
    private final ForkJoinPool executor = new ForkJoinPool(200);

    @Value("${neural.executor.single:false}")
    private boolean single;

    @Autowired
    private NeuralRepository neuralRepository;

    protected List<List<Double>> getMatrix(int level, boolean back) {
        List<List<Double>> result = new ArrayList<>();
        final AtomicInteger number = new AtomicInteger();

        List<Double> neuronWeights;
        while ((neuronWeights = neuralRepository.findAllByLevel(level).stream()
                .filter(back? weight->weight.getBackNumber() == number.get():weight->weight.getNumber() == number.get())
                .sorted().map(Weight::getValue).collect(Collectors.toList())).size() !=0) {
            result.add(neuronWeights);
            number.getAndIncrement();
        }
        return result;
    }

    public void calculateWeights(int level, List<Double> input, List<Double> delta, double m){
        final List<Future<?>> futures = new ArrayList<>();
        for (Weight weight : neuralRepository.findAllByLevel(level)) {
            futures.add(executor.submit(new NeuronWeightWorker(weight, input, delta, m)));
        }

        while (futures.size() != 0) futures.removeIf(Future::isDone);
    }

    public List<Double> calculate(int level, List<Double> input, List<Double> delta){
        if (level == 0) return input;

        final List<Neuron> neurons = new ArrayList<>();
        final List<Future<?>> futures = new ArrayList<>();
        final List<Double> singleResult = new ArrayList<>();

        List<List<Double>> matrix = getMatrix(level, delta != null);
        if (delta == null){
            for (int i=0;i<matrix.size();i++) {
                Neuron neuron = new Neuron(i, matrix.get(i), input);
                if (single){
                    neuron.getWorker().run();
                    singleResult.add(neuron.getOutput());
                } else {
                    neurons.add(neuron);
                    futures.add(executor.submit(neuron.getWorker()));
                }
            }
        } else {
            for (int j=0;j<input.size();j++){
                Neuron neuron = new NeuronBack(j, matrix.size() == 0?null:matrix.get(j), input, delta);
                if (single) {
                    neuron.getWorker().run();
                    singleResult.add(neuron.getOutput());
                } else {
                    neurons.add(neuron);
                    futures.add(executor.submit(neuron.getWorker()));
                }
            }
        }

        if (single) {
            return singleResult;
        }

        while (futures.size() != 0) futures.removeIf(Future::isDone);
        return neurons.stream().sorted().map(Neuron::getOutput).collect(Collectors.toList());
    }
}
