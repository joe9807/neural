package application.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import java.util.stream.IntStream;

public class NeuralUI {
    private static final int WIDTH = 26*13;
    private static final int HEIGHT = 400;
    private static final int FONT_SIZE = 15;
    private static Shell shell;
    private static final String ALPHABET_UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHABET_LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String FILE_NAME = "text.png";

    public static void main(String[] args){
        shell = new Shell(new Display(), SWT.CLOSE);
        shell.setLayout(new FillLayout());
        shell.setText("Neural UI");
        shell.setSize(new Point(WIDTH, HEIGHT));

        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.addListener(SWT.Paint, e -> drawImage(e.gc));

        shell.open();
        while (!shell.isDisposed()) {
            if (!shell.getDisplay().readAndDispatch()) {
                shell.getDisplay().sleep();
            }
        }
    }

    private void setMenu(){
        Menu menu = new Menu(shell, SWT.NONE);
        MenuItem runTestItem = new MenuItem(menu, SWT.NONE);
        runTestItem.setText("Run Test");
        runTestItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        shell.setMenu(menu);
    }

    public void runTest(){

    }

    private static void drawImage(GC gc){
        Image image = new Image(shell.getDisplay(), WIDTH, HEIGHT);
        Font font = new Font(shell.getDisplay(), "Courier", FONT_SIZE, SWT.NORMAL);
        GC gcImage = new GC(image);
        gcImage.setFont(font);
        gcImage.drawString(ALPHABET_UPPER_CASE, 3, 0);
        gcImage.drawString(ALPHABET_LOWER_CASE, 3, FONT_SIZE +1);
        //drawRect(gcImage);

        gc.drawImage(image, 0, 0);

        ImageLoader saver = new ImageLoader();
        saver.data = new ImageData[] { image.getImageData() };
        saver.save(FILE_NAME, SWT.IMAGE_PNG);
    }

    private static void drawRect(GC gc){
        IntStream.range(0, ALPHABET_UPPER_CASE.length()).forEach(index-> gc.drawRectangle(index*12+3, 3, 12, FONT_SIZE));
        IntStream.range(0, ALPHABET_LOWER_CASE.length()).forEach(index-> gc.drawRectangle(index*12+3, FONT_SIZE+3, 12, FONT_SIZE+3));
    }
}
