package application;

import application.neurolevels.NeuronLevel;
import application.utils.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import application.repository.WeightRepository;
import application.repository.entity.Weight;

import java.util.Collections;
import java.util.Date;
import java.util.List;
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
    public void testZeroLevel(){
        Date startDate = new Date();

        //Input Level
        Date inputDate = new Date();
        int inputCount = 20;
        IntStream.range(0, inputCount).forEach(number->{
            Weight weight = new Weight();
            weight.setValue(1.0);//1.0 is weight for the Input level only!
            weight.setLevel(0);
            weight.setNeuron(number);
            weightRepository.save(weight);
        });

        List<Double> afterInputLevel = neuronLevel.calculate(0, null);//just simple test value
        printLevel(afterInputLevel);
        System.out.println("--------------- Input level calculation took: "+Utils.getTimeElapsed(new Date().getTime()-inputDate.getTime()));

        //Hidden Level
        Date hiddenDate = new Date();
        int hiddenCount = 100;
        IntStream.range(0, hiddenCount).forEach(number-> IntStream.range(0, inputCount).forEach(value->{
            Weight weight = new Weight();
            weight.setValue(Math.random());//random weight
            weight.setLevel(1);
            weight.setNeuron(number);
            weightRepository.save(weight);
        }));

        List<Double> afterHiddenLevel = neuronLevel.calculate(1, afterInputLevel);
        printLevel(afterHiddenLevel);
        System.out.println("--------------- Hidden level calculation took: "+Utils.getTimeElapsed(new Date().getTime()-hiddenDate.getTime()));

        //Output Level
        Date outputDate = new Date();
        int outputCount = 1;
        IntStream.range(0, outputCount).forEach(number-> IntStream.range(0, hiddenCount).forEach(value->{
            Weight weight = new Weight();
            weight.setValue(Math.random());//random weight
            weight.setLevel(2);
            weight.setNeuron(number);
            weightRepository.save(weight);
        }));

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
}
