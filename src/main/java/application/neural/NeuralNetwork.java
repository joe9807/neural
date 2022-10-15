package application.neural;

import application.repository.WeightRepository;
import application.repository.entity.Weight;
import application.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NeuralNetwork {
    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    public NeuronLevel neuronLevel;

    public void recreate(int... neuronsCount){
        Date startDate = new Date();

        weightRepository.deleteAll();

        final AtomicInteger prevCount = new AtomicInteger(0);
        final AtomicInteger levelNumber = new AtomicInteger(0);
        Arrays.stream(neuronsCount).forEach(neuronCount->{
            if (prevCount.get() == 0) {//input level is here
                weightRepository.saveAll(IntStream.range(0, neuronCount).mapToObj(number-> new Weight(levelNumber.get(), number, 1.0)).collect(Collectors.toList()));
            } else {//hidden and output levels are here
                IntStream.range(0, neuronCount).forEach(number-> {
                    weightRepository.saveAll(IntStream.range(0, prevCount.get()).mapToObj(value-> new Weight(levelNumber.get(), number, Math.random())).collect(Collectors.toList()));
                });
            }

            prevCount.set(neuronCount);
            levelNumber.incrementAndGet();
        });

        System.out.printf("=============== Recreate weights took: %s\n", Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
    }

    public List<Double> calculate(){
        AtomicReference<List<Double>> result = new AtomicReference<>();
        IntStream.range(0, weightRepository.findLevelsCount()).forEach(levelNumber->{
            Date startDate = new Date();
            result.set(neuronLevel.calculate(levelNumber, result.get() == null?generateInput():result.get()));
            System.out. printf("--------------- '%s' level calculation took: %s\n", levelNumber, Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
        });

        return result.get();
    }

    private List<Double> generateInput() {
        return IntStream.range(0, weightRepository.findAllByLevel(0).size()).mapToObj(id -> Math.random()).collect(Collectors.toList());
    }
}
