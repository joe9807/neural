package application;

import application.neural.NeuralNetwork;
import application.utils.Utils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.stream.IntStream;

import static application.neural.NeuralConstants.ALPHABET;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest
public class AITest {
    @Autowired
    private NeuralNetwork neuralNetwork;

    @Before
    public void before(){
        neuralNetwork.setLearnText(ALPHABET);
        neuralNetwork.initParameters(240, ALPHABET.length());
    }

    @Test
    public void testRun(){
        neuralNetwork.recreate();
        neuralNetwork.generateInput();
        neuralNetwork.calculate(null, null);
    }

    @Test
    public void testLearn(){
        neuralNetwork.recreate();
        neuralNetwork.generateInput();
        double[][] deltas = getDeltas(ALPHABET.length());
        Date startDate = new Date();

        IntStream.range(0, Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber())).forEach(epoch->{
            IntStream.range(0, 26).forEach(index-> neuralNetwork.calculate(null, deltas[index]));
        });

        System.out.println(Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
    }

    private double[][] getDeltas(int length){
        return IntStream.range(0, length).mapToObj(index-> IntStream.range(0, length).mapToDouble(tempIndex-> tempIndex == index?1.0:0.0)
                .toArray()).toArray(double[][]::new);
    }
}
