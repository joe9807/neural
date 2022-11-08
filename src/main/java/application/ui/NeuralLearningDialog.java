package application.ui;

import application.neural.NeuralNetwork;
import application.utils.Utils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
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
    private ProgressBar progressBarEpoch;
    private Label result;
    private final int maxEpoch;
    private final int maxSamples;
    private int progressSamples;
    private int progressEpoches;
    private final NeuralNetwork neuralNetwork;

    protected NeuralLearningDialog(Shell parentShell, NeuralNetwork neuralNetwork) {
        super(parentShell);
        this.neuralNetwork = neuralNetwork;
        this.maxEpoch = Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber());
        this.maxSamples = neuralNetwork.getParameters().getSamplesNumber();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Learning Dialog");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(350, 200);
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));

        progressBarEpoch = new ProgressBar(composite, SWT.SMOOTH);
        progressBarEpoch.setSelection(0);
        progressBarEpoch.setMaximum(Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber()));
        progressBarEpoch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        result = new Label(composite, SWT.NONE);
        result.setLayoutData(new GridData());
        result.setText(neuralNetwork.getParameters().toString());

        progressBarEpoch.addPaintListener(e -> {
            String string = String.format("Epoches: %3s; Samples: %3s", maxEpoch-progressEpoches, maxSamples-progressSamples);

            Point point = progressBarEpoch.getSize();
            Font font = new Font(getParentShell().getDisplay(), "Tahoma", 8, SWT.NORMAL);
            e.gc.setFont(font);
            e.gc.setForeground(getParentShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));

            FontMetrics fontMetrics = e.gc.getFontMetrics();
            int stringWidth = fontMetrics.getAverageCharWidth() * string.length();
            int stringHeight = fontMetrics.getHeight();

            e.gc.drawString(string, (point.x-stringWidth)/2 , (point.y-stringHeight)/2, true);
            font.dispose();
        });
        return composite;
    }

    public void step(Date startDate, NeuralNetwork neuralNetwork){
        progressSamples++;

        if (progressSamples == maxSamples) {
            progressEpoches++;
            neuralNetwork.calculateErrors(maxSamples);

            if (progressEpoches == maxEpoch) {
                neuralNetwork.saveWeights();

                String elapsed = Utils.getTimeElapsed(new Date().getTime()-startDate.getTime());
                result.setText("Network has learned. Time elapsed: "+elapsed);
                result.update();
                System.out.println("=============== Network Learn took: "+elapsed);
            } else {
                progressSamples = 0;
            }
        }

        progressBarEpoch.setSelection(progressEpoches);
    }
}
