package application.ui;

import application.neural.NeuralNetwork;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NeuralUI {
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

    @Autowired
    private NeuralNetwork neuralNetwork;

    public void init(){
        shell = new Shell(new Display(), SWT.CLOSE);
        shell.setLayout(new RowLayout());
        shell.setText("Neural UI");
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
        MenuItem learningItem = new MenuItem(menu, SWT.NONE);
        learningItem.setText("Learn AI");
        learningItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                learn();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        MenuItem runTestItem = new MenuItem(menu, SWT.NONE);
        runTestItem.setText("Run AI");
        runTestItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                run();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        label.setMenu(menu);
    }

    public void learn(){
        neuralNetwork.recreate(216, 20, 26);
        IntStream.range(0, ALPHABET_UPPER_CASE.length()).forEach(index-> {
            List<Double> delta = IntStream.range(0, ALPHABET_UPPER_CASE.length()).mapToObj(tempIndex-> tempIndex == index?1.0:0.0).collect(Collectors.toList());
            neuralNetwork.calculate(getInput(null, index), delta);
        });
    }

    public void run(){
        neuralNetwork.recreate(216, 20, 26);

        ImageData imageData = new ImageData(WIDTH, HEIGHT, 1, new PaletteData(new RGB[] {new RGB(255, 255, 255), new RGB(0, 0, 0) }));

        final AtomicReference<String> scan = new AtomicReference<>(StringUtils.EMPTY);
        IntStream.range(0, ALPHABET_UPPER_CASE.length()).forEach(index-> {
            List<Double> result = neuralNetwork.calculate(getInput(imageData, index), null).stream().reduce((first, second) -> second).orElse(null);
            String ch = ALPHABET.charAt(IntStream.range(0, result.size()).reduce((i, j) -> result.get(i) > result.get(j) ? i : j).getAsInt())+StringUtils.EMPTY;
            scan.set(scan.get()+ch);
        });

        middleLabel.setImage(new Image(shell.getDisplay(), imageData));
        text.setText(scan.get());
        shell.layout();
    }

    private List<Double> getInput(ImageData imageData, int index){
        List<Double> input = new ArrayList<>();
        IntStream.range(0, 12).forEach(x->{
            IntStream.range(0, FONT_SIZE + 3).forEach(y->{
                int pixelValue = image.getImageData().getPixel(index * 12+x, y);
                if (imageData != null) {
                    imageData.setPixel(index * 12+x, y, pixelValue);
                }
                input.add((double) pixelValue);
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
        gcImage.drawString(ALPHABET_LOWER_CASE, 0, FONT_SIZE +1);

        label.setImage(image);

        ImageLoader saver = new ImageLoader();
        saver.data = new ImageData[] { image.getImageData() };
        saver.save(FILE_NAME, SWT.IMAGE_PNG);
    }
}
