package application;

import application.neural.NeuralNetwork;
import application.neural.NeuronLevel;
import application.repository.WeightRepository;
import application.utils.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
    private NeuralNetwork neuralNetwork;

    @Autowired
    public NeuronLevel neuronLevel;

    @Test
    public void test(){
        neuralNetwork.recreate(20, 100, 1);

        //Input Level
        Date startDate = new Date();

        List<Double> afterInputLevel = neuronLevel.calculate(0, generateInput());
        saveInput(afterInputLevel);
        System.out.println("--------------- Input level calculation took: "+Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));

        //Hidden Level
        Date hiddenDate = new Date();

        List<Double> afterHiddenLevel = neuronLevel.calculate(1, afterInputLevel);
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

    private List<Double> generateInput() {
        return IntStream.range(0, weightRepository.findAllByLevel(0).size()).mapToObj(id -> Math.random()).collect(Collectors.toList());
    }

    private List<Double> loadInput() {
        try {
            return Files.readAllLines(Path.of(getClass().getResource("/input.txt").toURI())).stream().map(Double::valueOf).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveInput(List<Double> input) {
        try {
            Files.writeString(Path.of(getClass().getResource("/input.txt").toURI()), input.stream().map(String::valueOf).collect(Collectors.joining("\n")), StandardOpenOption.CREATE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
