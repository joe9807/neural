package application.neural;

import application.repository.WeightRepository;
import application.repository.entity.Weight;
import application.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NeuralNetwork {
    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    public NeuronLevel neuronLevel;

    @Autowired
    public NeuronBackLevel neuronBackLevel;

    public void recreate(int... neuronsCount){
        Date startDate = new Date();

        weightRepository.deleteAll();

        final AtomicInteger prevCount = new AtomicInteger(0);
        final AtomicInteger levelNumber = new AtomicInteger(0);
        Arrays.stream(neuronsCount).forEach(neuronCount->{
            if (prevCount.get() == 0) {//input level is here
                weightRepository.saveAll(IntStream.range(0, neuronCount).mapToObj(number-> new Weight(levelNumber.get(), number, 1.0, 0)).collect(Collectors.toList()));
            } else {//hidden and output levels are here
                IntStream.range(0, neuronCount).forEach(number-> weightRepository.saveAll(IntStream.range(0, prevCount.get()).mapToObj(pos-> new Weight(levelNumber.get(), number, Math.random()-0.5, pos))
                        .collect(Collectors.toList())));
            }

            prevCount.set(neuronCount);
            levelNumber.incrementAndGet();
        });

        System.out.printf("=============== Recreate weights took: %s\n", Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
    }

    public List<List<Double>> calculate(List<Double> input, List<Double> delta){
        List<List<Double>> outputs = new ArrayList<>();
        for (int levelNumber=0;levelNumber<weightRepository.findLevelsCount();levelNumber++){
            outputs.add(neuronLevel.calculate(levelNumber, outputs.isEmpty()?(input == null?generateInput():input):outputs.get(outputs.size()-1), null));
        }

        List<List<Double>> deltas = new ArrayList<>();
        if (delta != null) {
            for (int levelNumber=outputs.size();levelNumber>1;levelNumber--){
                deltas.add(neuronBackLevel.calculate(levelNumber, outputs.get(levelNumber-1), deltas.isEmpty()?delta:deltas.get(deltas.size()-1)));
            }
        }
        return delta == null?outputs:deltas;
    }

    private List<Double> generateInput() {
        //return loadInput();
        return saveInput(IntStream.range(0, weightRepository.findAllByLevel(0).size()).mapToObj(id -> Math.random()).collect(Collectors.toList()));
    }

    private List<Double> saveInput(List<Double> input) {
        try {
            Files.writeString(Path.of(getClass().getResource("/input.txt").toURI()), input.stream().map(String::valueOf).collect(Collectors.joining("\n")), StandardOpenOption.CREATE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return input;
    }

    private List<Double> loadInput() {
        try {
            return Files.readAllLines(Path.of(getClass().getResource("/input.txt").toURI())).stream().map(Double::valueOf).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
