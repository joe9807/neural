package application;

import application.neural.NeuralNetwork;
import application.repository.WeightRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest
public class AITest {
    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    private NeuralNetwork neuralNetwork;

    @Test
    public void test(){
        neuralNetwork.recreate(20, 100, 1);
        printLevel(neuralNetwork.calculate());
    }

    private void printLevel(List<Double> level){
        for (int i=0;i<level.size();i++){
            System.out.printf("neuron number %-3s: %s%n", i, level.get(i));
        }
    }
}
