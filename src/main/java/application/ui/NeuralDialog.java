package application.ui;

import application.neural.NeuralNetwork;
import application.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static application.neural.NeuralConstants.ALPHABET;
import static application.neural.NeuralConstants.ALPHABET_LOWER_CASE;
import static application.neural.NeuralConstants.ALPHABET_UPPER_CASE;
import static application.neural.NeuralConstants.COLUMNS;
import static application.neural.NeuralConstants.FILE_NAME_INPUT;
import static application.neural.NeuralConstants.FILE_NAME_OUTPUT;
import static application.neural.NeuralConstants.ROWS;

@Service
public class NeuralDialog {
    private Shell shell;
    private GC gc;
    private Image image;
    private Label middleLabel;
    private Text textImage;
    private String text;
    private double noise;
    private int width;
    private int height;

    @Autowired
    private NeuralNetwork neuralNetwork;

    public void init(double noise, int fontSize){
        this.noise = noise;

        shell = new Shell(new Display(), SWT.CLOSE);
        gc = new GC(shell.getDisplay());
        gc.setFont(new Font(shell.getDisplay(), "Courier", fontSize, SWT.NORMAL));
        width = COLUMNS*gc.getFontMetrics().getAverageCharWidth();
        height = ROWS*gc.getFontMetrics().getHeight()+10;
        shell.setLayout(new RowLayout());
        shell.setText("Neural Network");
        shell.setSize(new Point(width *3+50, height +50));

        Label leftLabel = new Label(shell, SWT.BORDER);
        middleLabel = new Label(shell, SWT.BORDER);
        textImage = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
        textImage.setLayoutData(new RowData(width, height));
        text = drawLeftImage(leftLabel);
        neuralNetwork.setLearnText(ALPHABET);
        neuralNetwork.initParameters(gc.getFontMetrics().getAverageCharWidth()*gc.getFontMetrics().getHeight(), ALPHABET.length());

        setMenu(leftLabel);
        shell.open();
        while (!shell.isDisposed()) {
            if (!shell.getDisplay().readAndDispatch()) {
                shell.getDisplay().sleep();
            }
        }
    }

    private void setMenu(Label label){
        Menu menu = new Menu(shell, SWT.NONE);

        MenuItem createItem = new MenuItem(menu, SWT.NONE);
        createItem.setText("Open Network");
        createItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                NeuralNetworkDialog neuralNetworkDialog = new NeuralNetworkDialog(shell, neuralNetwork, getInputs());
                switch (neuralNetworkDialog.open()){
                    case 1: return;
                    case 2: {
                        neuralNetwork.saveWithName();
                        return;
                    }
                    case 3: {
                        neuralNetwork.loadByName();
                        return;
                    }
                    case 4: {
                        neuralNetwork.deleteByName();
                        return;
                    }
                    case 5: {
                        run(text);
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        label.setMenu(menu);
    }

    public List<List<Double>> getInputs(){
        return IntStream.range(0, ALPHABET.length()).mapToObj(index-> getInput(null, index, index, -1)).collect(Collectors.toList());
    }

    public void run(String text){
        Date startDate = new Date();

        ImageData imageData = new ImageData(width, height, 4, new PaletteData(new RGB[] {new RGB(255, 255, 255), new RGB(0, 0, 0),
                new RGB(0, 150, 0), new RGB(255, 0, 0) }));

        final AtomicReference<String> scan = new AtomicReference<>(StringUtils.EMPTY);
        final AtomicInteger count = new AtomicInteger(text.length());
        int number = COLUMNS*ROWS;
        IntStream.range(0, number).forEach(index-> {
            Display.getCurrent().asyncExec(() -> {
                List<Double> result = neuralNetwork.calculate(getInput(null, index, index, -1), null).stream().findFirst().orElse(null);

                int gotIndex = IntStream.range(0, result.size()).reduce((i, j) -> result.get(i) > result.get(j) ? i : j).getAsInt();
                String gotLetter = String.valueOf(ALPHABET.charAt(gotIndex));
                String shouldLetter = String.valueOf(text.charAt(index));
                boolean correct = gotLetter.equals(shouldLetter);
                if (correct) count.getAndDecrement();

                getInput(imageData, gotIndex, index, correct?2:3);
                scan.set(scan.get()+gotLetter);
                if (scan.get().replaceAll("\n", "").length()%COLUMNS == 0) {
                    scan.set(scan.get()+"\n");
                }

                middleLabel.setImage(new Image(shell.getDisplay(), imageData));
                shell.layout();

                if (index == number -1) {
                    int detected = (text.length()-count.get());
                    textImage.setText(scan.get()+"\n"+detected+" of "+text.length()+" characters were properly detected ("+(detected*100/text.length())+"%)");

                    ImageLoader saver = new ImageLoader();
                    saver.data = new ImageData[] { imageData };
                    saver.save(FILE_NAME_OUTPUT, SWT.IMAGE_PNG);

                    System.out.printf("=============== Network Run took: %s\n", Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
                }
            });
        });
    }

    private List<Double> getInput(ImageData imageData, int indexRead, int indexWrite, int pixelToSet){
        int frameX = gc.getFontMetrics().getAverageCharWidth();
        int frameY = gc.getFontMetrics().getHeight();
        int shiftFrameX = image.getImageData().width/frameX;

        int shiftReadY = frameY*(indexRead / shiftFrameX);
        int shiftReadX = indexRead % shiftFrameX;

        int shiftWriteY = frameY*(indexWrite / shiftFrameX);
        int shiftWriteX = indexWrite % shiftFrameX;

        List<Double> input = new ArrayList<>();
        IntStream.range(0, frameX).forEach(x->{
            IntStream.range(0, frameY).forEach(y->{
                int readX = shiftReadX*frameX + x;
                int readY = shiftReadY+y;

                int writeX = shiftWriteX*frameX + x;
                int writeY = shiftWriteY+y;

                int readValue = image.getImageData().getPixel(readX, readY);
                if (imageData != null) {
                    if (readValue == 1) {
                        imageData.setPixel(writeX, writeY, pixelToSet);
                    }

                    if (x == 0 || y == 0) {
                        imageData.setPixel(writeX, writeY, 1);
                    }
                }
                input.add((double) readValue);
            });
        });

        return input;
    }

    private String drawLeftImage(Label label){
        ImageData imageData = new ImageData(width, height, 1, new PaletteData(new RGB[] {new RGB(255, 255, 255), new RGB(0, 0, 0) }));
        image = new Image(shell.getDisplay(), imageData);
        GC gcImage = new GC(image);
        gcImage.setFont(gc.getFont());

        final StringBuilder builder = new StringBuilder();
        IntStream.range(0, ROWS).forEach(row->{
            String result = "";
            if (row == 0) {
                result = ALPHABET_UPPER_CASE;
            } else if (row == 1) {
                result = ALPHABET_LOWER_CASE;
            } else {
                result = IntStream.range(0, COLUMNS).mapToObj(index -> ALPHABET.charAt(ThreadLocalRandom.current().nextInt(0, 52)) + "").collect(Collectors.joining());
            }
            builder.append(result);
            gcImage.setForeground(gcImage.getDevice().getSystemColor(SWT.COLOR_BLACK));
            gcImage.drawString(result, 0, gcImage.getFontMetrics().getHeight()*row);

            if (row>1) {
                randomDots(gcImage, image.getImageData(), row);
            }
        });
        label.setImage(image);

        ImageLoader saver = new ImageLoader();
        saver.data = new ImageData[] { image.getImageData() };
        saver.save(FILE_NAME_INPUT, SWT.IMAGE_PNG);

        return builder.toString();
    }

    private void randomDots(GC gc, ImageData imageData, int row){
        IntStream.range(0, width).forEach(x->{
            IntStream.range(gc.getFontMetrics().getHeight()*row, gc.getFontMetrics().getHeight()*(row+1)).forEach(y->{
                if (Math.random()<noise) {
                    if (imageData.getPixel(x, y) == 1) {
                        gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
                    } else {
                        gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
                    }
                    gc.drawPoint(x, y);
                }
            });
        });
    }
}
