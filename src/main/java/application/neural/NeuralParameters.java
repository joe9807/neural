package application.neural;

import application.ui.NeuralDialog;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class NeuralParameters {
    private String levels = NeuralDialog.INPUT_SIZE+";20;52";
    private String name = "";
    private String epochesNumber = "300";
    private String m = "0.4";

    public int getOutputCount(){
        String[] split =  levels.split(";");
        return Integer.parseInt(split[split.length-1]);
    }

    public String toString(){
        return String.format("Levels: %s; Max Epoches: %s; M: %s", levels, epochesNumber, m);
    }
}
