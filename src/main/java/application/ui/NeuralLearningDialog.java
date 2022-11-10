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

public class NeuralLearningDialog {
    private ProgressBar progressBarEpoch;
    private final int maxEpoch;
    private final int maxSamples;
    private int progressSamples;
    private int progressEpoches;
    private final NeuralNetwork neuralNetwork;
    private Date startDate;
    private Button learnButton;
    private final List<List<Double>> inputs;
    private Runnable updateLabel;

    protected NeuralLearningDialog(NeuralNetwork neuralNetwork, List<List<Double>> inputs, Runnable updateLabel) {
        this.neuralNetwork = neuralNetwork;
        this.maxEpoch = Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber());
        this.maxSamples = neuralNetwork.getLearnText().length();
        this.inputs = inputs;
        this.updateLabel = updateLabel;
    }

    public void draw(Composite parent) {
        progressBarEpoch = new ProgressBar(parent, SWT.SMOOTH);
        progressBarEpoch.setSelection(0);
        progressBarEpoch.setMaximum(Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber()));
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
                    learn(neuralNetwork.getLearnText());
                } else {
                    stop();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
    }

    public void learn(String learnText){
        List<List<Double>> deltas = getDeltas(learnText);

        IntStream.range(0, Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber())).forEach(epoch->{
            IntStream.range(0, learnText.length()).forEach(index-> {
                Display.getDefault().asyncExec(()->{
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

        if (progressSamples == maxSamples) {
            progressEpoches++;
            neuralNetwork.calculateErrors(maxSamples);

            if (progressEpoches == maxEpoch) {
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
