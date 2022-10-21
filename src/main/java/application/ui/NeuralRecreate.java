package application.ui;

import application.neural.NeuralParameters;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NeuralRecreate extends Dialog {
    private final NeuralParameters parameters;

    protected NeuralRecreate(Shell parentShell, NeuralParameters parameters) {
        super(parentShell);
        this.parameters = parameters;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Neural Network Parameters");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(300, 200);
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));

        new Label(composite, SWT.NONE).setText("Levels:");
        Text levelsText = new Text(composite, SWT.BORDER);
        levelsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        levelsText.setText(parameters.getLevels());
        levelsText.addModifyListener(e -> parameters.setLevels(((Text)e.widget).getText()));

        new Label(composite, SWT.NONE).setText("Epoches:");
        Text epochesText = new Text(composite, SWT.BORDER);
        epochesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        epochesText.setText(String.valueOf(parameters.getEpoches()));
        epochesText.addModifyListener(e -> parameters.setEpoches(((Text)e.widget).getText()));

        new Label(composite, SWT.NONE).setText("M:");
        Text mText = new Text(composite, SWT.BORDER);
        mText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mText.setText(String.valueOf(parameters.getM()));
        mText.addModifyListener(e -> parameters.setM(((Text)e.widget).getText()));
        return composite;
    }
}