package application.ui;

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
    private final int max;
    private ProgressBar progressBar;

    protected NeuralProgressBar(Shell parentShell, int max) {
        super(parentShell);
        this.max = max;
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

        new Label(composite, SWT.NONE).setText("Samples:");
        progressBar = new ProgressBar(composite, SWT.SMOOTH);
        progressBar.setMaximum(max);
        progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return composite;
    }

    public void step(Date startDate){
        progressBar.setSelection(progressBar.getSelection()+1);
        if (progressBar.getSelection() == max) {
            System.out.printf("=============== Network Learn took: %s\n", Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
            close();
        }
    }
}
