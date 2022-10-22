package application.neural;

import application.repository.WeightRepository;
import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NeuralNetwork {
    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    public NeuronExecutor neuronExecutor;

    @Autowired
    public NeuronBackExecutor neuronBackExecutor;

    @Autowired
    private NeuralParameters parameters;

    private Map<Integer, List<Weight>> updates;

    public void recreate(){
        weightRepository.deleteAll();

        final AtomicInteger prevCount = new AtomicInteger(0);
        final AtomicInteger levelNumber = new AtomicInteger(0);
        Arrays.stream(Arrays.stream(parameters.getLevels().split(";")).mapToInt(Integer::valueOf).toArray()).forEach(neuronCount->{
            if (prevCount.get() == 0) {//input level is here
                weightRepository.saveAll(IntStream.range(0, neuronCount).mapToObj(number->
                     new Weight(levelNumber.get(), number, 1.0, 0)
                ).collect(Collectors.toList()));
            } else {//hidden and output levels are here
                IntStream.range(0, neuronCount).forEach(number-> weightRepository.saveAll(IntStream.range(0, prevCount.get()).mapToObj(backNumber->
                    new Weight(levelNumber.get(), number, Math.random()-0.5, backNumber)
                ).collect(Collectors.toList())));
            }

            prevCount.set(neuronCount);
            levelNumber.incrementAndGet();
        });
    }

    public void saveWeights() {
        updates.values().forEach(value-> weightRepository.saveAll(value));
        updates = new HashMap<>();
    }

    public List<List<Double>> calculate(List<Double> input, List<Double> delta){
        if (updates == null) updates = new HashMap<>();

        List<List<Double>> outputs = new ArrayList<>();
        for (int levelNumber=0;levelNumber<weightRepository.findLevelsCount();levelNumber++){
            outputs.add(neuronExecutor.calculate(levelNumber, outputs.isEmpty()?(input == null?loadInput():input):outputs.get(outputs.size()-1), null));
        }

        List<List<Double>> deltas = new ArrayList<>();
        if (delta != null) {
            for (int levelNumber=outputs.size();levelNumber>1;levelNumber--){
                deltas.add(neuronBackExecutor.calculate(levelNumber, outputs.get(levelNumber-1), deltas.isEmpty()?delta:deltas.get(deltas.size()-1)));
            }

            for (int levelNumber=outputs.size()-1;levelNumber>0;levelNumber--){
                if (updates.get(levelNumber) == null){
                    updates.put(levelNumber, weightRepository.findAllByLevel(levelNumber));
                }
                neuronExecutor.calculateWeights(updates.get(levelNumber), outputs.get(levelNumber-1), deltas.get(deltas.size()-levelNumber), parameters);
            }
        }
        return delta == null?outputs:deltas;
    }

    public void generateInput() {
        saveInput(IntStream.range(0, weightRepository.findAllByLevel(0).size()).mapToObj(id -> Math.random()).collect(Collectors.toList()));
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
