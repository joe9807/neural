package application.ui;

import application.neural.NeuralNetwork;
import application.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NeuralDialog {
    private static final int WIDTH = 26*12;
    private static final int HEIGHT = 400;
    private static final int FONT_SIZE = 15;
    private static final String ALPHABET_UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHABET_LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHABET = ALPHABET_UPPER_CASE+ALPHABET_LOWER_CASE;
    private static final String FILE_NAME = "text.png";

    private Shell shell;
    private Image image;
    private Label middleLabel;
    private Text text;
    private MenuItem runItem;
    private MenuItem learnItem;
    private MenuItem createItem;

    @Autowired
    private NeuralNetwork neuralNetwork;

    public void init(){
        shell = new Shell(new Display(), SWT.CLOSE);
        shell.setLayout(new RowLayout());
        shell.setText("Neural Network");
        shell.setSize(new Point(WIDTH*3+45, HEIGHT+50));

        Label leftLabel = new Label(shell, SWT.BORDER);
        middleLabel = new Label(shell, SWT.BORDER);
        text = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
        text.setLayoutData(new RowData(WIDTH, HEIGHT));
        drawImage(leftLabel);

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

        createItem = new MenuItem(menu, SWT.NONE);
        createItem.setText("Create Network");
        createItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                NeuralCreateDialog neuralCreateDialog = new NeuralCreateDialog(shell, neuralNetwork.getParameters());
                if (neuralCreateDialog.open() != 0) return;

                Date startDate = new Date();
                neuralNetwork.recreate();
                neuralNetwork.generateInput();
                System.out.printf("=============== Network Create took: %s\n", Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));

                MessageDialog dialog = new MessageDialog(shell, "Question", null, "Neural Network "+ neuralNetwork.getParameters().getLevels()+" is created. Choose further action."
                        , MessageDialog.QUESTION, new String[] {"Run", "Learn"}, 0);
                int result = dialog.open();
                if (result == 0) {
                    runItem.notifyListeners(SWT.Selection, null);
                } else if (result == 1) {
                    learnItem.notifyListeners(SWT.Selection, null);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        runItem = new MenuItem(menu, SWT.NONE);
        runItem.setText("Run Network");
        runItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Date startDate = new Date();
                run();
                System.out.printf("=============== Network Run took: %s\n", Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        learnItem = new MenuItem(menu, SWT.NONE);
        learnItem.setText("Learn Network");
        learnItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                learn();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        label.setMenu(menu);
    }

    public void learn(){
        Date startDate = new Date();

        neuralNetwork.getParameters().setSamplesNumber(ALPHABET.length());
        NeuralLearningDialog neuralLearningDialog = new NeuralLearningDialog(shell, neuralNetwork);
        neuralLearningDialog.setBlockOnOpen(false);
        neuralLearningDialog.open();

        List<List<Double>> deltas = getDeltas();

        IntStream.range(0, Integer.parseInt(neuralNetwork.getParameters().getEpochesNumber())).forEach(epoch->{
            IntStream.range(0, ALPHABET.length()).forEach(index-> {
                Display.getDefault().asyncExec(()->{
                    if (neuralLearningDialog.getReturnCode() != 1) {
                        neuralNetwork.calculate(getInput(null, index, index, -1), deltas.get(index));
                        neuralLearningDialog.step(startDate, neuralNetwork);
                    }
                });
            });
        });
    }

    private List<List<Double>> getDeltas(){
        return IntStream.range(0, ALPHABET.length()).mapToObj(index-> IntStream.range(0, ALPHABET.length()).mapToObj(tempIndex-> tempIndex == index?1.0:0.0)
                .collect(Collectors.toList())).collect(Collectors.toList());
    }

    public void run(){
        ImageData imageData = new ImageData(WIDTH, HEIGHT, 4, new PaletteData(new RGB[] {new RGB(255, 255, 255), new RGB(0, 0, 0),
                new RGB(0, 255, 0), new RGB(255, 0, 0) }));

        final AtomicReference<String> scan = new AtomicReference<>(StringUtils.EMPTY);
        IntStream.range(0, ALPHABET.length()).forEach(index-> {
            List<Double> result = neuralNetwork.calculate(getInput(imageData, index, index, -1), null).stream().findFirst().orElse(null);

            int gotIndex = IntStream.range(0, result.size()).reduce((i, j) -> result.get(i) > result.get(j) ? i : j).getAsInt();
            String gotLetter = String.valueOf(ALPHABET.charAt(gotIndex));
            String shouldLetter = String.valueOf(ALPHABET.charAt(index));

            getInput(imageData, gotIndex, index, gotLetter.equals(shouldLetter)?2:3);
            scan.set(scan.get()+gotLetter);
            if (scan.get().replaceAll("\n", "").length()%26 == 0) {
                scan.set(scan.get()+"\n");
            }
        });

        middleLabel.setImage(new Image(shell.getDisplay(), imageData));
        text.setText(scan.get());
        shell.layout();
    }

    private List<Double> getInput(ImageData imageData, int indexRead, int indexWrite, int pixelToSet){
        int frameX = 12;
        int frameY = FONT_SIZE + 3;
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

    private void drawImage(Label label){
        ImageData imageData = new ImageData(WIDTH, HEIGHT, 1, new PaletteData(new RGB[] {new RGB(255, 255, 255), new RGB(0, 0, 0) }));
        image = new Image(shell.getDisplay(), imageData);
        Font font = new Font(shell.getDisplay(), "Courier", FONT_SIZE, SWT.NORMAL);
        GC gcImage = new GC(image);
        gcImage.setFont(font);
        gcImage.drawString(ALPHABET_UPPER_CASE, 0, 0);
        gcImage.drawString(ALPHABET_LOWER_CASE, 0, FONT_SIZE + 3);

        label.setImage(image);

        ImageLoader saver = new ImageLoader();
        saver.data = new ImageData[] { image.getImageData() };
        saver.save(FILE_NAME, SWT.IMAGE_PNG);
    }
}
