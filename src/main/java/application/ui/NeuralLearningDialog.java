package application.ui;

import application.neural.NeuralNetwork;
import application.utils.Utils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import java.util.Date;

public class NeuralLearningDialog extends Dialog {
    private final int maxSamples;
    private final int maxEpoch;
    private ProgressBar progressBarSamples;
    private ProgressBar progressBarEpoch;
    private Label epochesLabel;
    private Label samplesLabel;
    private Label result;

    protected NeuralLearningDialog(Shell parentShell, int maxSamples, int maxEpoch) {
        super(parentShell);
        this.maxSamples = maxSamples;
        this.maxEpoch = maxEpoch;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Learning Dialog");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(300, 200);
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);
        composite.setLayout(new GridLayout(3, false));

        new Label(composite, SWT.NONE).setText("Epoches:");
        progressBarEpoch = new ProgressBar(composite, SWT.SMOOTH);
        progressBarEpoch.setSelection(0);
        progressBarEpoch.setMaximum(maxEpoch);
        progressBarEpoch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        epochesLabel = new Label(composite, SWT.NONE);
        epochesLabel.setText(String.valueOf(maxEpoch));

        new Label(composite, SWT.NONE).setText("Samples:");
        progressBarSamples = new ProgressBar(composite, SWT.SMOOTH);
        progressBarSamples.setSelection(0);
        progressBarSamples.setMaximum(maxSamples);
        progressBarSamples.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        samplesLabel = new Label(composite, SWT.NONE);
        samplesLabel.setText(String.valueOf(maxEpoch));

        result = new Label(composite, SWT.NONE);
        result.setLayoutData(new GridData());
        ((GridData)result.getLayoutData()).horizontalSpan=3;
        return composite;
    }

    public synchronized void step(Date startDate, NeuralNetwork neuralNetwork){
        progressBarSamples.setSelection(progressBarSamples.getSelection()+1);

        if (progressBarSamples.getSelection() == maxSamples) {
            progressBarEpoch.setSelection(progressBarEpoch.getSelection()+1);
            updateLabels();

            neuralNetwork.saveWeights();

            if (progressBarEpoch.getSelection() == maxEpoch) {
                String elapsed = Utils.getTimeElapsed(new Date().getTime()-startDate.getTime());
                result.setText("Network has learned. Time elapsed: "+elapsed);
                System.out.println("=============== Network Learn took: "+elapsed);
            } else {
                progressBarSamples.setSelection(0);
            }
        }

        updateLabels();
    }

    public void updateLabels(){
        epochesLabel.setText(String.valueOf(maxEpoch-progressBarEpoch.getSelection()));
        samplesLabel.setText(String.valueOf(maxSamples-progressBarSamples.getSelection()));
        samplesLabel.pack(true);
        epochesLabel.pack(true);
        result.pack(true);
    }
}
