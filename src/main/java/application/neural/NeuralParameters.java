package application.neural;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class NeuralParameters {
    private String levels = "216;20;26";
    private String epoches = "120";
    private String m = "0.4";

    public int getOutputCount(){
        String[] split =  levels.split(";");
        return Integer.parseInt(split[split.length-1]);
    }
}
