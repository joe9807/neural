package application.ui;

import application.utils.Utils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static application.neural.NeuralConstants.ALPHABET;
import static application.neural.NeuralConstants.COLUMNS;

public class NeuralCorrectionDialog extends Dialog {
    private final int fontSize;
    private final List<Double> result;
    private final int index;
    private final Image leftImage;
    private final String shouldLetter;

    protected NeuralCorrectionDialog(Shell parentShell, int fontSize, List<Double> result, int index, Image leftImage, String shouldLetter) {
        super(parentShell);
        this.fontSize = fontSize;
        this.result = result;
        this.index = index;
        this.leftImage = leftImage;
        this.shouldLetter = shouldLetter;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(String.format("Correction Dialog (%s)", index));
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        Label correctLabel = new Label(composite, SWT.BORDER);
        drawLeftImage(composite, correctLabel);
        composite.layout();
        return composite;
    }

    private void drawLeftImage(Composite composite, Label label){
        GC gc = new GC(composite.getDisplay());
        gc.setFont(Utils.getFont(composite.getDisplay(), fontSize));
        int width = gc.getFontMetrics().getAverageCharWidth()*55;
        int height = COLUMNS*gc.getFontMetrics().getHeight();

        Image image = new Image(composite.getDisplay(), width-50, height);
        GC gcImage = new GC(image);
        gcImage.setFont(gc.getFont());

        List<Double> cloneResult = new ArrayList<>(result);
        IntStream.range(0, result.size()).forEach(pos->{
            int index = Utils.getBestIndex(cloneResult);
            String result = String.valueOf(ALPHABET.charAt(index));

            gcImage.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_BLACK));

            if (result.equals(shouldLetter)){
                gcImage.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_GREEN));
            }

            if (pos == 0 && !result.equals(shouldLetter)) {
                gcImage.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_RED));
            }
            int x = pos/COLUMNS;
            int y = pos%COLUMNS;
            gcImage.drawString(String.format("%s (%s)", result, BigDecimal.valueOf(cloneResult.get(index)).toPlainString()), x*width/2, gcImage.getFontMetrics().getHeight()*y);
            cloneResult.set(index, -1d);
        });

        gcImage.dispose();
        final ImageData id = image.getImageData();
        Utils.getInput(gc, Objects.requireNonNull(leftImage.getImageData()), id, index, 1, 1, false);

        label.setImage(new Image(composite.getDisplay(), id));
    }
}
