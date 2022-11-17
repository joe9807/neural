package application.neural;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NeuralSignal {
    private int index;
    private double value;
}
