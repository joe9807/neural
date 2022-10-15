package application;

import application.neural.NeuralNetwork;
import application.repository.WeightRepository;
import application.utils.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

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
        neuralNetwork.recreate(216, 20, 20, 52);
        Utils.printLevel(neuralNetwork.calculate(null));
    }

}
