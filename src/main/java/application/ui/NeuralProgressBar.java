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

public class NeuralProgressBar extends Dialog {
    private final int maxSamples;
    private final int maxEpoch;
    private ProgressBar progressBarSamples;
    private ProgressBar progressBarEpoch;

    protected NeuralProgressBar(Shell parentShell, int maxSamples, int maxEpoch) {
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
        composite.setLayout(new GridLayout(2, false));

        new Label(composite, SWT.NONE).setText("Epoches:");
        progressBarEpoch = new ProgressBar(composite, SWT.SMOOTH);
        progressBarEpoch.setSelection(0);
        progressBarEpoch.setMaximum(maxEpoch);
        progressBarEpoch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE).setText("Samples:");
        progressBarSamples = new ProgressBar(composite, SWT.SMOOTH);
        progressBarSamples.setSelection(0);
        progressBarSamples.setMaximum(maxSamples);
        progressBarSamples.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public synchronized void step(Date startDate, NeuralNetwork neuralNetwork){
        progressBarSamples.setSelection(progressBarSamples.getSelection());

        if (progressBarSamples.getSelection() == maxSamples) {
            neuralNetwork.saveWeights();
            progressBarEpoch.setSelection(progressBarEpoch.getSelection()+1);

            if (progressBarEpoch.getSelection() == maxEpoch) {
                System.out.printf("=============== Network Learn took: %s\n", Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
                close();
            } else {
                progressBarSamples.setSelection(0);
            }
        }
    }
}
