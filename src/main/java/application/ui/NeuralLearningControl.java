package application.ui;

import application.neural.NeuralConstants;
import application.neural.NeuralNetwork;
import application.utils.Utils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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
    private List<List<Double>> inputs;
    private final Runnable updateLabel;
    private String elapsedString;

    protected NeuralLearningControl(NeuralNetwork neuralNetwork, List<List<Double>> inputs, Runnable updateLabel) {
        this.neuralNetwork = neuralNetwork;
        this.inputs = inputs;
        this.updateLabel = updateLabel;
    }

    public void draw(Composite parent) {
        progressBarEpoch = new ProgressBar(parent, SWT.SMOOTH);
        progressBarEpoch.setSelection(0);
        progressBarEpoch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        progressBarEpoch.addPaintListener(e -> {
            if (elapsedString == null) return;

            Point point = progressBarEpoch.getSize();
            Font font = new Font(progressBarEpoch.getDisplay(),"Courier",10,SWT.NORMAL);
            e.gc.setFont(font);
            e.gc.setForeground(progressBarEpoch.getDisplay().getSystemColor(SWT.COLOR_BLACK));

            FontMetrics fontMetrics = e.gc.getFontMetrics();
            int stringWidth = fontMetrics.getAverageCharWidth() * elapsedString.length();
            int stringHeight = fontMetrics.getHeight();

            e.gc.drawString(elapsedString, (point.x-stringWidth)/2 , (point.y-stringHeight)/2, true);
            font.dispose();
        });

        learnButton = new Button(parent, SWT.PUSH);
        learnButton.setText("Learn");
        learnButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (learnButton.getText().equalsIgnoreCase("Learn")) {
                    learn();
                } else {
                    stop();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        new Label(parent, SWT.NONE).setText(neuralNetwork.getLearnText());
    }

    public void learn(){
        startDate = new Date();
        elapsedString = null;
        learnButton.setText("Stop");
        progressSamples = 0;
        progressEpoches = 0;
        neuralNetwork.resetErrors();
        progressBarEpoch.setMaximum(Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber()));
        List<List<Double>> deltas = getDeltas(neuralNetwork.getLearnText());

        IntStream.range(0, Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber())).forEach(epoch->{
            IntStream.range(0, inputs.size()).forEach(index-> {
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
        return IntStream.range(0, learnText.length()).mapToObj(learnTextIndex-> IntStream.range(0, NeuralConstants.ALPHABET.length())
                .mapToObj(index-> learnText.charAt(learnTextIndex) == NeuralConstants.ALPHABET.charAt(index)?1.0:0.0)
                .collect(Collectors.toList())).collect(Collectors.toList());
    }

    public void step(NeuralNetwork neuralNetwork){
        progressSamples++;

        if (progressSamples == inputs.size()) {
            progressEpoches++;
            neuralNetwork.calculateErrors(inputs.size());

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
        elapsedString = "Learned on "+ inputs.size()+" sample(s). Time elapsed: "+Utils.getTimeElapsed(new Date().getTime()-startDate.getTime());
        System.out.println(elapsedString);
        updateLabel.run();
        progressBarEpoch.redraw();
    }

    public void setInputs(List<List<Double>> inputs) {
        this.inputs = inputs;
    }
}
