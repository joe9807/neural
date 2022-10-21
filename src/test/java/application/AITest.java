package application;

import application.neural.NeuralNetwork;
import application.utils.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest
public class AITest {
    @Autowired
    private NeuralNetwork neuralNetwork;

    @Test
    public void testRun(){
        neuralNetwork.recreate();
        Utils.printLevel(neuralNetwork.calculate(null, null).stream().reduce((first, second) -> second).orElse(null));
    }

    @Test
    public void testLearn(){
        neuralNetwork.recreate();
        IntStream.range(0, 26).forEach(index-> {
            List<Double> delta = IntStream.range(0, neuralNetwork.getParameters().getOutputCount()).mapToObj(tempIndex-> tempIndex == index?1.0:0.0).collect(Collectors.toList());
            System.out.printf("----------- %s of %s ------------%n", index, neuralNetwork.getParameters().getOutputCount());
            neuralNetwork.calculate(null, delta).forEach(Utils::printLevel);
        });
    }
}
