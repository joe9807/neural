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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NeuralDialog {
    private static final int WIDTH = 26*12;
    private static final int HEIGHT = 410;
    private static final int FONT_SIZE = 15;
    private static final int ROWS = 20;
    private static final int COLUMNS = 26;
    private static final String ALPHABET_UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHABET_LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHABET = ALPHABET_UPPER_CASE+ALPHABET_LOWER_CASE;
    private static final String FILE_NAME = "text.png";

    private Shell shell;
    private Image image;
    private Label middleLabel;
    private Text textImage;
    private String text;

    @Autowired
    private NeuralNetwork neuralNetwork;

    public void init(){
        shell = new Shell(new Display(), SWT.CLOSE);
        shell.setLayout(new RowLayout());
        shell.setText("Neural Network");
        shell.setSize(new Point(WIDTH*3+45, HEIGHT+50));

        Label leftLabel = new Label(shell, SWT.BORDER);
        middleLabel = new Label(shell, SWT.BORDER);
        textImage = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
        textImage.setLayoutData(new RowData(WIDTH, HEIGHT));
        text = drawImage(leftLabel);
        neuralNetwork.setLearnText(ALPHABET);

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
                    case 0: {
                        Date startDate = new Date();
                        neuralNetwork.recreate();
                        neuralNetwork.generateInput();
                        System.out.printf("=============== Network Create took: %s\n", Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
                        break;
                    }
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
        List<List<Double>> inputs = new ArrayList<>();
        IntStream.range(0, ALPHABET.length()).forEach(index-> {
            //Display.getDefault().asyncExec(()->{
                inputs.add(getInput(null, index, index, -1));
            //});
        });

        return inputs;
    }

    public void run(String text){
        Date startDate = new Date();

        ImageData imageData = new ImageData(WIDTH, HEIGHT, 4, new PaletteData(new RGB[] {new RGB(255, 255, 255), new RGB(0, 0, 0),
                new RGB(0, 255, 0), new RGB(255, 0, 0) }));

        final AtomicReference<String> scan = new AtomicReference<>(StringUtils.EMPTY);
        IntStream.range(0, COLUMNS*ROWS).forEach(index-> {
            List<Double> result = neuralNetwork.calculate(getInput(imageData, index, index, -1), null).stream().findFirst().orElse(null);

            int gotIndex = IntStream.range(0, result.size()).reduce((i, j) -> result.get(i) > result.get(j) ? i : j).getAsInt();
            String gotLetter = String.valueOf(ALPHABET.charAt(gotIndex));
            String shouldLetter = String.valueOf(text.charAt(index));

            getInput(imageData, gotIndex, index, gotLetter.equals(shouldLetter)?2:3);
            scan.set(scan.get()+gotLetter);
            if (scan.get().replaceAll("\n", "").length()%COLUMNS == 0) {
                scan.set(scan.get()+"\n");
            }
        });

        middleLabel.setImage(new Image(shell.getDisplay(), imageData));
        textImage.setText(scan.get());
        shell.layout();

        System.out.printf("=============== Network Run took: %s\n", Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
    }

    private List<Double> getInput(ImageData imageData, int indexRead, int indexWrite, int pixelToSet){
        int frameX = 12;
        int frameY = FONT_SIZE + 5;
        int shiftFrameX = image.getImageData().width/frameX;


        int shiftReadY = frameY*(indexRead / shiftFrameX);
        int shiftReadX = indexRead % shiftFrameX;

        int shiftWriteY = frameY*(indexWrite / shiftFrameX);
        int shiftWriteX = indexWrite % shiftFrameX;

        List<Double> input = new ArrayList<>();
        IntStream.range(0, frameX).forEach(x->{
            IntStream.range(0, frameY).forEach(y->{
                int readX = shiftReadX*frameX + x;
                int readY = shiftReadY+y+2;

                int writeX = shiftWriteX*frameX + x;
                int writeY = shiftWriteY+y+2;

                int readValue = image.getImageData().getPixel(readX, readY);
                if (imageData != null) {
                    if (indexRead == indexWrite || readValue != 0){
                        imageData.setPixel(writeX, writeY, pixelToSet<0?readValue:(readValue==1?pixelToSet:0));
                    }
                }
                input.add((double) readValue);
            });
        });

        return input;
    }

    private String drawImage(Label label){
        ImageData imageData = new ImageData(WIDTH, HEIGHT, 1, new PaletteData(new RGB[] {new RGB(255, 255, 255), new RGB(0, 0, 0) }));
        image = new Image(shell.getDisplay(), imageData);
        GC gcImage = new GC(image);
        gcImage.setFont(new Font(shell.getDisplay(), "Courier", FONT_SIZE, SWT.NORMAL));

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
            gcImage.drawString(result, 0, (FONT_SIZE + 5)*row);
        });
        label.setImage(image);

        ImageLoader saver = new ImageLoader();
        saver.data = new ImageData[] { image.getImageData() };
        saver.save(FILE_NAME, SWT.IMAGE_PNG);

        return builder.toString();
    }
}
