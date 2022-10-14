package application.neural;

import application.repository.WeightRepository;
import application.repository.entity.Weight;
import application.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NeuralNetwork {
    @Autowired
    private WeightRepository weightRepository;

    public void recreateWeights(){
        Date startDate = new Date();

        weightRepository.deleteAll();

        //Input Level
        int inputCount = 20;
        weightRepository.saveAll(IntStream.range(0, inputCount).mapToObj(number-> new Weight(0, number, 1.0)).collect(Collectors.toList()));

        //Hidden Level
        int hiddenCount = 100;
        IntStream.range(0, hiddenCount).forEach(number-> {
            weightRepository.saveAll(IntStream.range(0, inputCount).mapToObj(value-> new Weight(1, number, Math.random())).collect(Collectors.toList()));
        });

        //Output Level
        int outputCount = 1;
        IntStream.range(0, outputCount).forEach(number-> {
            weightRepository.saveAll(IntStream.range(0, hiddenCount).mapToObj(value-> new Weight(2, number, Math.random())).collect(Collectors.toList()));
        });

        System.out.println("=========== Create weights took: "+ Utils.getTimeElapsed(new Date().getTime()-startDate.getTime())+" ===================");
    }
}
