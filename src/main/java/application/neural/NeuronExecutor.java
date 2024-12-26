package application.neural;

import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NeuronExecutor {
    private final ForkJoinPool executor = new ForkJoinPool(200, ForkJoinPool.defaultForkJoinWorkerThreadFactory, (t, e) -> e.printStackTrace(System.out), false);

    @Value("${neural.executor.single:false}")
    private boolean single;

    @Autowired
    private NeuralRepository neuralRepository;

    protected double[][] getMatrix(int level, boolean back) {
        List<double[]> result = new ArrayList<>();
        final AtomicInteger number = new AtomicInteger();

        double[] neuronWeights;
        while ((neuronWeights = neuralRepository.findAllByLevel(level).stream()
                .filter(back? weight->weight.getBackNumber() == number.get():weight->weight.getNumber() == number.get())
                .sorted().mapToDouble(Weight::getValue).toArray()).length !=0) {
            result.add(neuronWeights);
            number.getAndIncrement();
        }

        double[][] arr = new double[result.size()][];
        Arrays.setAll(arr, result::get);
        return arr;
    }

    public void calculateWeights(int level, double[] input, double[] delta, double m){
        neuralRepository.findAllByLevel(level).forEach(weight-> weight.setValue(weight.getValue() + m * input[weight.getBackNumber()] * delta[weight.getNumber()]));
    }

    public double[] calculateLevel(int level, double[] input, double[] values){
        if (level == 0) return input;

        final List<Neuron> neurons = new ArrayList<>();
        final List<Double> singleResult = new ArrayList<>();

        double[][] matrix = getMatrix(level, values != null);
        int count = values != null?values.length:matrix.length;
        final double[] result = new double[count];

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
            double[] arr = new double[singleResult.size()];
            Arrays.setAll(arr, singleResult::get);
            return arr;
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
        return result;
    }
}
