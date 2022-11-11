package application.ui;

import application.neural.NeuralNetwork;
import application.utils.Utils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NeuralLearningControl {
    private ProgressBar progressBarEpoch;
    private int progressSamples;
    private int progressEpoches;
    private final NeuralNetwork neuralNetwork;
    private Date startDate;
    private Button learnButton;
    private final List<List<Double>> inputs;
    private final Runnable updateLabel;

    protected NeuralLearningControl(NeuralNetwork neuralNetwork, List<List<Double>> inputs, Runnable updateLabel) {
        this.neuralNetwork = neuralNetwork;
        this.inputs = inputs;
        this.updateLabel = updateLabel;
    }

    public void draw(Composite parent) {
        progressBarEpoch = new ProgressBar(parent, SWT.SMOOTH);
        progressBarEpoch.setSelection(0);
        progressBarEpoch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        learnButton = new Button(parent, SWT.PUSH);
        learnButton.setText("Learn");
        learnButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (learnButton.getText().equalsIgnoreCase("Learn")) {
                    startDate = new Date();
                    learnButton.setText("Stop");
                    progressSamples = 0;
                    progressEpoches = 0;
                    learn();
                } else {
                    stop();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
    }

    public void learn(){
        neuralNetwork.resetErrors();
        progressBarEpoch.setMaximum(Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber()));
        List<List<Double>> deltas = getDeltas(neuralNetwork.getLearnText());

        IntStream.range(0, Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber())).forEach(epoch->{
            IntStream.range(0, neuralNetwork.getLearnText().length()).forEach(index-> {
                Display.getCurrent().asyncExec(()->{
                    if (learnButton.getText().equalsIgnoreCase("Stop")) {
                        neuralNetwork.calculate(inputs.get(index), deltas.get(index));
                        step(neuralNetwork);
                    }
                });
            });
        });
    }

    private List<List<Double>> getDeltas(String learnText){
        return IntStream.range(0, learnText.length()).mapToObj(index-> IntStream.range(0, learnText.length()).mapToObj(tempIndex-> tempIndex == index?1.0:0.0)
                .collect(Collectors.toList())).collect(Collectors.toList());
    }

    public void step(NeuralNetwork neuralNetwork){
        progressSamples++;

        if (progressSamples == neuralNetwork.getLearnText().length()) {
            progressEpoches++;
            neuralNetwork.calculateErrors(neuralNetwork.getLearnText().length());

            if (progressEpoches == Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber())) {
                stop();
            } else {
                progressSamples = 0;
            }
        }

        progressBarEpoch.setSelection(progressEpoches);
    }

    public void stop(){
        learnButton.setText("Learn");
        neuralNetwork.saveWeights();
        String elapsed = Utils.getTimeElapsed(new Date().getTime()-startDate.getTime());
        String string = "Network has learned. Time elapsed: "+elapsed;
        System.out.println(string);
        updateLabel.run();
    }
}
