package application.ui;

import application.neural.NeuralNetwork;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NeuralNetworkDialog extends Dialog {
    private NeuralNetwork neuralNetwork;

    protected NeuralNetworkDialog(Shell parentShell, NeuralNetwork neuralNetwork) {
        super(parentShell);
        this.neuralNetwork = neuralNetwork;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Neural Network");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(650, 150);
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(4, false));

        new Label(composite, SWT.NONE).setText("Name:");
        Combo comboName = new Combo(composite, SWT.BORDER);
        comboName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comboName.addModifyListener(e -> neuralNetwork.getParameters().setName(((Combo)e.widget).getText()));
        neuralNetwork.getAllNames().stream().forEach(comboName::add);

        new Label(composite, SWT.NONE).setText("Levels:");
        Text levelsText = new Text(composite, SWT.BORDER);
        levelsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        levelsText.setText(neuralNetwork.getParameters().getLevels());
        levelsText.addModifyListener(e -> neuralNetwork.getParameters().setLevels(((Text)e.widget).getText()));

        new Label(composite, SWT.NONE).setText("Epoches:");
        Text epochesText = new Text(composite, SWT.BORDER);
        epochesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        epochesText.setText(String.valueOf(neuralNetwork.getParameters().getEpochesNumber()));
        epochesText.addModifyListener(e -> neuralNetwork.getParameters().setEpochesNumber(((Text)e.widget).getText()));

        new Label(composite, SWT.NONE).setText("M:");
        Text mText = new Text(composite, SWT.BORDER);
        mText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mText.setText(String.valueOf(neuralNetwork.getParameters().getM()));
        mText.addModifyListener(e -> neuralNetwork.getParameters().setM(((Text)e.widget).getText()));
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, 0, "Create", true);
        createButton(parent, 2, "Save", false);
        createButton(parent, 3, "Load", false);
        createButton(parent, 4, "Delete", false);
        createButton(parent, 5, "Run", false);
        createButton(parent, 6, "Learn", false);
    }

    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);

        if (buttonId == 2 || buttonId == 3 || buttonId == 4 || buttonId == 5 || buttonId == 6) {
            setReturnCode(buttonId);
            close();
        }
    }
}