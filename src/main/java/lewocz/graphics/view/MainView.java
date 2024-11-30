package lewocz.graphics.view;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import lewocz.graphics.command.*;
import lewocz.graphics.event.EventQueue;
import lewocz.graphics.model.*;
import lewocz.graphics.viewmodel.IMainViewModel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Optional;

import static lewocz.graphics.view.utils.AlertUtils.showAlert;

@Component
public class MainView {
    @FXML
    private Pane canvasPane;
    @FXML
    private Canvas canvas;
    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem loadMenuItem;
    @FXML
    private MenuItem saveShapesMenuItem;
    @FXML
    private MenuItem loadShapesMenuItem;

    private final IMainViewModel mainViewModel;
    private final EventQueue eventQueue;

    private GraphicsContext gc;

    public MainView(IMainViewModel mainViewModel, EventQueue eventQueue) {
        this.mainViewModel = mainViewModel;
        this.eventQueue = eventQueue;
    }

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        bindProperties();
        setupCanvasListeners();
        mainViewModel.setRedrawCanvasCallback(this::redrawCanvas);
        redrawCanvas();
    }

    private void bindProperties() {
        loadingIndicator.visibleProperty().bind(mainViewModel.isProcessingProperty());
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());

        saveMenuItem.setOnAction(e -> onSaveMenuItemClicked());
        loadMenuItem.setOnAction(e -> onLoadMenuItemClicked());
        saveShapesMenuItem.setOnAction(e -> onSaveShapesMenuItemClicked());
        loadShapesMenuItem.setOnAction(e -> onLoadShapesMenuItemClicked());
    }

    private void setupCanvasListeners() {
        canvas.setOnMousePressed(mouseEvent -> {
            Command command = new MousePressedCommand(mainViewModel, mouseEvent.getX(), mouseEvent.getY());
            eventQueue.enqueue(command);
        });
        canvas.setOnMouseDragged(mouseEvent -> {
            Command command = new MouseDraggedCommand(mainViewModel, mouseEvent.getX(), mouseEvent.getY());
            eventQueue.enqueue(command);
        });
        canvas.setOnMouseReleased(mouseEvent -> {
            Command command = new MouseReleasedCommand(mainViewModel);
            eventQueue.enqueue(command);
        });

        canvas.widthProperty().addListener(evt -> redrawCanvas());
        canvas.heightProperty().addListener(evt -> redrawCanvas());
    }

    private void onSaveMenuItemClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNM Files", "*.pbm", "*.pgm", "*.ppm")
        );
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Select Image Format");
            alert.setHeaderText("Choose the format to save:");
            ButtonType pbmTextButton = new ButtonType("PBM Textual (P1)");
            ButtonType pbmBinaryButton = new ButtonType("PBM Binary (P4)");
            ButtonType pgmTextButton = new ButtonType("PGM Textual (P2)");
            ButtonType pgmBinaryButton = new ButtonType("PGM Binary (P5)");
            ButtonType ppmTextButton = new ButtonType("PPM Textual (P3)");
            ButtonType ppmBinaryButton = new ButtonType("PPM Binary (P6)");
            alert.getButtonTypes().setAll(
                    pbmTextButton, pbmBinaryButton,
                    pgmTextButton, pgmBinaryButton,
                    ppmTextButton, ppmBinaryButton,
                    ButtonType.CANCEL
            );

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.CANCEL) {
                WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, snapshot);

                String filePath = file.getAbsolutePath();
                PNMFormat format = null;
                boolean binaryFormat = false;

                if (result.get() == pbmTextButton) {
                    format = PNMFormat.PBM;
                    binaryFormat = false;
                    // Ensure file extension is .pbm
                    if (!filePath.toLowerCase().endsWith(".pbm")) {
                        filePath += ".pbm";
                    }
                } else if (result.get() == pbmBinaryButton) {
                    format = PNMFormat.PBM;
                    binaryFormat = true;
                    if (!filePath.toLowerCase().endsWith(".pbm")) {
                        filePath += ".pbm";
                    }
                } else if (result.get() == pgmTextButton) {
                    format = PNMFormat.PGM;
                    binaryFormat = false;
                    if (!filePath.toLowerCase().endsWith(".pgm")) {
                        filePath += ".pgm";
                    }
                } else if (result.get() == pgmBinaryButton) {
                    format = PNMFormat.PGM;
                    binaryFormat = true;
                    if (!filePath.toLowerCase().endsWith(".pgm")) {
                        filePath += ".pgm";
                    }
                } else if (result.get() == ppmTextButton) {
                    format = PNMFormat.PPM;
                    binaryFormat = false;
                    if (!filePath.toLowerCase().endsWith(".ppm")) {
                        filePath += ".ppm";
                    }
                } else if (result.get() == ppmBinaryButton) {
                    format = PNMFormat.PPM;
                    binaryFormat = true;
                    if (!filePath.toLowerCase().endsWith(".ppm")) {
                        filePath += ".ppm";
                    }
                }

                Command command = new SaveCommand(mainViewModel, filePath, binaryFormat, snapshot, format);
                eventQueue.enqueue(command);
            }
        }
    }

    private void onLoadMenuItemClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Image Files", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif", "*.pbm", "*.pgm", "*.ppm"),
                new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("BMP Files", "*.bmp"),
                new FileChooser.ExtensionFilter("GIF Files", "*.gif"),
                new FileChooser.ExtensionFilter("PNM Files", "*.pbm", "*.pgm", "*.ppm")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            String fileName = file.getName().toLowerCase();
            PNMFormat format = null;

            if (fileName.endsWith(".pbm")) {
                format = PNMFormat.PBM;
            } else if (fileName.endsWith(".pgm")) {
                format = PNMFormat.PGM;
            } else if (fileName.endsWith(".ppm")) {
                format = PNMFormat.PPM;
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")
                    || fileName.endsWith(".bmp") || fileName.endsWith(".gif")) {
                format = null; // Standard image formats
            } else {
                showAlert("Unsupported File Format", "The selected file format is not supported.");
                return;
            }

            Command command = new LoadCommand(mainViewModel, file.getAbsolutePath(), format);
            eventQueue.enqueue(command);
        }
    }

    private void onSaveShapesMenuItemClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Shapes File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Shapes Files", "*.shapes")
        );
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            Command command = new SaveShapesCommand(mainViewModel, file.getAbsolutePath());
            eventQueue.enqueue(command);
        }
    }

    private void onLoadShapesMenuItemClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Shapes File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Shapes Files", "*.shapes")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Command command = new LoadShapesCommand(mainViewModel, file.getAbsolutePath());
            eventQueue.enqueue(command);
        }
    }

    private void redrawCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (ShapeModel shape : mainViewModel.getShapes()) {
            shape.draw(gc);
        }

        if (mainViewModel.getTempShape() != null) {
            mainViewModel.getTempShape().draw(gc);
        }
    }
}
