package application.neural;

import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class NeuronExecutor {
    private final ForkJoinPool executor = new ForkJoinPool(200, ForkJoinPool.defaultForkJoinWorkerThreadFactory, (t, e) -> e.printStackTrace(System.out), false);

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
        neuralRepository.findAllByLevel(level).forEach(weight-> weight.setValue(weight.getValue() + m * input.get(weight.getBackNumber()) * delta.get(weight.getNumber())));
    }

    public List<Double> calculateLevel(int level, List<Double> input, List<Double> values){
        if (level == 0) return input;

        final List<Neuron> neurons = new ArrayList<>();
        final List<Double> singleResult = new ArrayList<>();

        List<List<Double>> matrix = getMatrix(level, values != null);
        int count = values != null?values.size():matrix.size();
        final Double[] result = new Double[count];

        for (int number=0;number<count;number++) {
            Neuron neuron = NeuronFactory.getNeuron(number, matrix, input, values);
            if (single) {
                neuron.getWorker().run();
                singleResult.add(neuron.getOutput());
            } else {
                neurons.add(neuron);
                executor.execute(neuron.getWorker());
            }
        }

        if (single) {
            return singleResult;
        }

        while (neurons.size() != 0) {
            final List<Neuron> temp = new ArrayList<>();

            for (Neuron neuron:neurons) {
                if (neuron.getOutput() != null) {
                    result[neuron.getNumber()] = neuron.getOutput();
                    temp.add(neuron);
                }
            }

            neurons.removeAll(temp);
        }
        return Arrays.asList(result);
    }
}
