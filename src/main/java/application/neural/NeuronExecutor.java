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

@Service
public class NeuronExecutor {
    private ForkJoinPool executor = new ForkJoinPool(200);
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

    public void calculateWeights(int level, List<Double> input, List<Double> delta, NeuralParameters parameters){
        final List<Future<?>> futures = new ArrayList<>();
        for (Weight weight : neuralRepository.findAllByLevel(level)) {
            futures.add(executor.submit(new NeuronWeightWorker(weight, input, delta, parameters)));
        }

        futures(futures);
    }

    public List<Double> calculate(int level, List<Double> input, List<Double> delta){
        if (level == 0) return input;

        final List<Neuron> neurons = new ArrayList<>();
        final List<Future<?>> futures = new ArrayList<>();

        List<List<Double>> matrix = getMatrix(level, delta != null);
        if (delta == null){
            for (int i=0;i<matrix.size();i++) {
                Neuron neuron = new Neuron(i, matrix.get(i), input);
                neurons.add(neuron);
                futures.add(executor.submit(neuron.getWorker()));
            }
        } else {
            for (int j=0;j<input.size();j++){
                Neuron neuron = new NeuronBack(j, matrix.size() == 0?null:matrix.get(j), input, delta);
                neurons.add(neuron);
                futures.add(executor.submit(neuron.getWorker()));
            }
        }

        futures(futures);
        return neurons.stream().sorted().map(Neuron::getOutput).collect(Collectors.toList());
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
