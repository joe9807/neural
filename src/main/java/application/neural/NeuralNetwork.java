package application.neural;

import application.repository.WeightRepository;
import application.repository.entity.Weight;
import application.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NeuralNetwork {
    @Autowired
    private WeightRepository weightRepository;

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

        System.out.println("=========== Create weights took: "+ Utils.getTimeElapsed(new Date().getTime()-startDate.getTime())+" ===================");
    }
}
