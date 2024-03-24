package application.ui;

import application.neural.NeuralNetwork;
import application.utils.Utils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static application.neural.NeuralConstants.ALPHABET;

public class NeuralNetworkDialog extends Dialog {
    private static final int WEIGHT = 520;
    private static final int HEIGHT = 400;
    private static final String onlyInitialInputs = "Only Initial Inputs";
    private static final String onlyWrongInputs = "Only Wrong Inputs";
    private static final String plusWrongInputs = "Plus Wrong Inputs";

    private final NeuralNetwork neuralNetwork;
    private final List<List<Double>> inputs;
    private final List<List<Double>> wrongInputs;
    private final String wrongText;
    private Label labelError;
    private Combo comboInputs;
    private NeuralLearningControl neuralLearningControl;

    protected NeuralNetworkDialog(Shell parentShell, NeuralNetwork neuralNetwork, List<List<Double>> inputs, List<List<Double>> wrongInputs, String wrongText) {
        super(parentShell);
        this.neuralNetwork = neuralNetwork;
        this.inputs = inputs;
        this.wrongInputs = wrongInputs;
        this.wrongText = wrongText;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Neural Network");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(WEIGHT, HEIGHT);
    }

    protected Control createDialogArea(Composite parent) {
        int columnsNumber = 4;
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(columnsNumber, false));

        new Label(composite, SWT.NONE).setText("Name:");
        Combo comboName = new Combo(composite, SWT.BORDER);
        comboName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comboName.addModifyListener(e -> neuralNetwork.getParameters().setName(((Combo)e.widget).getText()));
        neuralNetwork.getAllNames().forEach(comboName::add);

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

        new Label(composite, SWT.NONE).setText("Inputs:");
        comboInputs = new Combo(composite, SWT.CHECK | SWT.READ_ONLY);
        comboInputs.add(onlyInitialInputs);
        comboInputs.add(onlyWrongInputs);
        comboInputs.add(plusWrongInputs);
        comboInputs.select(0);
        comboInputs.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                changeInputs();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        labelError = new Label(composite, SWT.BORDER);
        labelError.setLayoutData(new GridData(GridData.FILL_BOTH));
        ((GridData)labelError.getLayoutData()).horizontalSpan=columnsNumber;

        getShell().layout();
        drawErrors();

        Composite learningComposite = new Composite(composite, SWT.NONE);
        learningComposite.setLayout(new GridLayout(2, false));
        learningComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ((GridData)learningComposite.getLayoutData()).horizontalSpan=columnsNumber;

        neuralLearningControl = new NeuralLearningControl(neuralNetwork, this::drawErrors);
        neuralLearningControl.draw(learningComposite);
        changeInputs();
        return composite;
    }

    private void changeInputs(){
        if (comboInputs.getText().equalsIgnoreCase(onlyInitialInputs)){
            neuralLearningControl.setInputs(inputs);
            neuralNetwork.setLearnText(ALPHABET);
        } else if (comboInputs.getText().equalsIgnoreCase(onlyWrongInputs)){
            neuralLearningControl.setInputs(wrongInputs);
            neuralNetwork.setLearnText(wrongText);
        } else if (comboInputs.getText().equalsIgnoreCase(plusWrongInputs)){
            List<List<Double>> result = new ArrayList<>();
            result.addAll(inputs);
            result.addAll(wrongInputs);
            neuralLearningControl.setInputs(result);
            neuralNetwork.setLearnText(ALPHABET+wrongText);
        }

        neuralLearningControl.redraw();
    }

    public void drawErrors(){
        if (neuralNetwork.getErrors() == null) return;

        ImageData imageData = new ImageData(490, 150, 2, new PaletteData(new RGB[] {new RGB(255, 255, 255), new RGB(200, 0, 0), new RGB(0, 200, 0)}));

        IntStream.range(0, imageData.width).forEach(x-> imageData.setPixel(x, x *imageData.height/imageData.width, 2));

        List<Double> errors = new ArrayList<>(neuralNetwork.getErrors());

        double xScale = imageData.width/ (double) errors.size();
        double min = errors.stream().min(Double::compareTo).orElse(0.0);
        double max = errors.stream().max(Double::compareTo).orElse(1.0);
        double yScale = (imageData.height-1)/(max-min);

        int index = 0;
        double prevError = 0;
        double diff = 0;
        for (Double error:errors) {
            System.out.printf("%,.15f (%,.15f) %s%n", error, prevError-error, prevError-error<diff?"-":"+");
            int x = (int)(index++*xScale);
            int y = imageData.height-1-(int)((error-min)*yScale);
            if (x>=0 && x<imageData.width && y>=0 && y<imageData.height) {
                imageData.setPixel(x, y, 1);
            }
            diff = prevError-error;
            prevError = error;
        }
        labelError.setImage(new Image(labelError.getDisplay(), imageData));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, 7, "Create", true).addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Date startDate = new Date();
                neuralNetwork.recreate();
                neuralNetwork.generateInput();
                drawErrors();
                System.out.printf("=============== Network Create took: %s\n", Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        createButton(parent, 2, "Save", false);
        createButton(parent, 3, "Load", false);
        createButton(parent, 4, "Delete", false);
        createButton(parent, 5, "Run", false);
    }

    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);

        if (buttonId == 2 || buttonId == 3 || buttonId == 4 || buttonId == 5 || buttonId == 6) {
            setReturnCode(buttonId);
            close();
        }
    }
}