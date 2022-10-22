package application.neural;

import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NeuralNetwork {
    @Autowired
    public NeuronExecutor neuronExecutor;

    @Autowired
    public NeuralRepository neuralRepository;

    @Autowired
    private NeuralParameters parameters;

    public void recreate(){
        neuralRepository.deleteAll();

        final AtomicInteger prevCount = new AtomicInteger(0);
        final AtomicInteger levelNumber = new AtomicInteger(0);
        Arrays.stream(parameters.getLevels().split(";")).forEach(value->{
            int neuronCount = Integer.parseInt(value);
            if (prevCount.get() == 0) {//input level is here
                neuralRepository.saveAll(IntStream.range(0, neuronCount).mapToObj(number->
                     new Weight(levelNumber.get(), number, 1.0, 0)
                ).collect(Collectors.toList()));
            } else {//hidden and output levels are here
                IntStream.range(0, neuronCount).forEach(number-> neuralRepository.saveAll(IntStream.range(0, prevCount.get()).mapToObj(backNumber->
                    new Weight(levelNumber.get(), number, Math.random()-0.5, backNumber)
                ).collect(Collectors.toList())));
            }

            prevCount.set(neuronCount);
            levelNumber.incrementAndGet();
        });
    }

    public void saveWeights() {
        neuralRepository.saveAll();;
    }

    public List<List<Double>> calculate(List<Double> input, List<Double> delta){
        List<List<Double>> outputs = new ArrayList<>();
        List<Double> output;
        while ((output = neuronExecutor.calculate(outputs.size(), outputs.stream().findFirst().orElse(input == null?loadInput():input), null)) != null) {
            outputs.add(0, output);
        }

        if (delta == null) return outputs;

        List<List<Double>> deltas = new ArrayList<>();
        while (outputs.size()-deltas.size()>1) {
            deltas.add(0, neuronExecutor.calculate(outputs.size()-deltas.size(), outputs.get(deltas.size()), deltas.stream().findFirst().orElse(delta)));
        }

        int level = outputs.size();
        while (--level>0) {
            neuronExecutor.calculateWeights(neuralRepository.findAllByLevel(level), outputs.get(outputs.size()-level), deltas.get(level-1), parameters);
        }

        return deltas;
    }

    public void generateInput() {
        saveInput(IntStream.range(0, neuralRepository.findAllByLevel(0).size()).mapToObj(id -> Math.random()).collect(Collectors.toList()));
    }

    private void saveInput(List<Double> input) {
        try {
            Files.writeString(Path.of(getClass().getResource("/input.txt").toURI()), input.stream().map(String::valueOf).collect(Collectors.joining("\n")), StandardOpenOption.CREATE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Double> loadInput() {
        try {
            return Files.readAllLines(Path.of(getClass().getResource("/input.txt").toURI())).stream().map(Double::valueOf).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public NeuralParameters getParameters() {
        return parameters;
    }
}
