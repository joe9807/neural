package application.ui;

import application.neural.NeuralNetwork;
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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

@Service
public class NeuralUI {
    private static final int WIDTH = 26*12;
    private static final int HEIGHT = 400;
    private static final int FONT_SIZE = 15;
    private static final String ALPHABET_UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHABET_LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String FILE_NAME = "text.png";

    private Shell shell;
    private Image image;

    @Autowired
    private NeuralNetwork neuralNetwork;

    public void init(){
        shell = new Shell(new Display(), SWT.CLOSE | SWT.RESIZE);
        shell.setLayout(new RowLayout());
        shell.setText("Neural UI");
        shell.setSize(new Point(WIDTH*3, HEIGHT+50));

        Label label = new Label(shell, SWT.BORDER);
        drawImage(label);

        setMenu(label);
        shell.open();
        while (!shell.isDisposed()) {
            if (!shell.getDisplay().readAndDispatch()) {
                shell.getDisplay().sleep();
            }
        }
    }

    private void setMenu(Label label){
        Menu menu = new Menu(shell, SWT.NONE);
        MenuItem runTestItem = new MenuItem(menu, SWT.NONE);
        runTestItem.setText("Run Test");
        runTestItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                runTest();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        label.setMenu(menu);
    }

    public void runTest(){
        neuralNetwork.recreate();

        ImageData imageData = new ImageData(WIDTH, HEIGHT, 1, new PaletteData(new RGB[] {new RGB(255, 255, 255), new RGB(0, 0, 0) }));
        Label middleLabel = new Label(shell, SWT.BORDER);

        IntStream.range(0, ALPHABET_UPPER_CASE.length()).forEach(index-> {
            IntStream.range(0, 12).forEach(x->{
                IntStream.range(0, FONT_SIZE + 3).forEach(y->{
                    int pixelValue = image.getImageData().getPixel(index * 12+x, y);
                    imageData.setPixel(index * 12+x, y, pixelValue);
                    System.out.println(pixelValue);
                });
            });
        });
        middleLabel.setImage(new Image(shell.getDisplay(), imageData));
        shell.layout();
    }

    private void drawImage(Label label){
        ImageData imageData = new ImageData(WIDTH, HEIGHT, 1, new PaletteData(new RGB[] {new RGB(255, 255, 255), new RGB(0, 0, 0) }));
        image = new Image(shell.getDisplay(), imageData);
        Font font = new Font(shell.getDisplay(), "Courier", FONT_SIZE, SWT.NORMAL);
        GC gcImage = new GC(image);
        gcImage.setFont(font);
        gcImage.drawString(ALPHABET_UPPER_CASE, 0, 0);
        gcImage.drawString(ALPHABET_LOWER_CASE, 0, FONT_SIZE +1);
        //drawRect(gcImage);

        label.setImage(image);

        ImageLoader saver = new ImageLoader();
        saver.data = new ImageData[] { image.getImageData() };
        saver.save(FILE_NAME, SWT.IMAGE_PNG);
    }

    private void drawRect(GC gc){
        IntStream.range(0, ALPHABET_UPPER_CASE.length()).forEach(index-> gc.drawRectangle(index*12, 0, 12, FONT_SIZE+3));
        IntStream.range(0, ALPHABET_LOWER_CASE.length()).forEach(index-> gc.drawRectangle(index*12, FONT_SIZE+3, 12, FONT_SIZE+3));
    }
}
