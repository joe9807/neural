package application.neurolevels;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Neuron {
    private int number;
    private List<Double> weights;
    private List<Double> values;
}
