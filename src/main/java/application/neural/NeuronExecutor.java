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
    @Autowired
    private NeuralRepository neuralRepository;

    protected List<List<Double>> getNeurons(int level, boolean back) {
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
        if (level == 0) return input;

        List<Double> result = new ArrayList<>();
        if (delta == null){
            for (List<Double> line : getNeurons(level, false)) {
                double value = 0.0;
                for (int j = 0; j < line.size(); j++) {
                    value += line.get(j) * input.get(j);
                }

                result.add(1 / (1 + Math.exp(-1 * value)));
            }
        } else {
            List<List<Double>> matrix = getNeurons(level, true);
            if (matrix.size() == 0){
                for (int i=0;i<input.size();i++){
                    double tk = delta.get(i);
                    double ok = input.get(i);
                    result.add(ok*(1-ok)*(tk-ok));
                }
            } else {
                for (int j=0;j<input.size();j++){
                    List<Double> line = matrix.get(j);

                    double value = 0.0;
                    for (int k = 0; k < line.size(); k++) {
                        value += line.get(k) * delta.get(k);
                    }

                    result.add(input.get(j)*(1-input.get(j))*value);
                }
            }
        }

        return result;
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
