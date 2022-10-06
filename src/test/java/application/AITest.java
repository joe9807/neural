package application;

import application.neurolevels.NeuronLevel;
import application.repository.WeightRepository;
import application.repository.entity.Weight;
import application.utils.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest
public class AITest {
    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    public NeuronLevel neuronLevel;

    @Test
    public void test(){
        //createWeights();

        //Input Level
        Date startDate = new Date();

        //List<Double> afterInputLevel = neuronLevel.calculate(0, null);
        List<Double> afterInputLevel = neuronLevel.calculate(0, loadInput());
        printLevel(afterInputLevel);
        System.out.println("--------------- Input level calculation took: "+Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));

        //Hidden Level
        Date hiddenDate = new Date();

        List<Double> afterHiddenLevel = neuronLevel.calculate(1, afterInputLevel);
        printLevel(afterHiddenLevel);
        System.out.println("--------------- Hidden level calculation took: "+Utils.getTimeElapsed(new Date().getTime()-hiddenDate.getTime()));

        //Output Level
        Date outputDate = new Date();

        List<Double> outputLevel = neuronLevel.calculate(2, afterHiddenLevel);
        printLevel(outputLevel);
        System.out.println("--------------- Output level calculation took: "+Utils.getTimeElapsed(new Date().getTime()-outputDate.getTime()));

        System.out.println("--------------- Entire calculation took: "+Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
    }

    private void printLevel(List<Double> level){
        for (int i=0;i<level.size();i++){
            System.out.printf("neuron number %-3s: %s%n", i, level.get(i));
        }
    }

    private void createWeights(){
        Date startDate = new Date();

        //Input Level
        int inputCount = 20;
        IntStream.range(0, inputCount).forEach(number->{
            Weight weight = new Weight();
            weight.setValue(1.0);//1.0 is weight for the Input level only!
            weight.setLevel(0);
            weight.setNumber(number);
            weightRepository.save(weight);
        });

        //Hidden Level
        Date hiddenDate = new Date();
        int hiddenCount = 2;
        IntStream.range(0, hiddenCount).forEach(number-> IntStream.range(0, inputCount).forEach(value->{
            Weight weight = new Weight();
            weight.setValue(Math.random());//random weight
            weight.setLevel(1);
            weight.setNumber(number);
            weightRepository.save(weight);
        }));

        //Output Level
        int outputCount = 1;
        IntStream.range(0, outputCount).forEach(number-> IntStream.range(0, hiddenCount).forEach(value->{
            Weight weight = new Weight();
            weight.setValue(Math.random());//random weight
            weight.setLevel(2);
            weight.setNumber(number);
            weightRepository.save(weight);
        }));

        System.out.println("--------------- Create weights took: "+Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
    }

    private List<Double> loadInput() {
        try {
            return Files.readAllLines(Path.of(getClass().getResource("/input.txt").toURI())).stream().map(line -> Double.parseDouble(line.split(":")[1].trim())).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
