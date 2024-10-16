package lewocz.graphics.view;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import lewocz.graphics.model.ShapeModel;
import lewocz.graphics.viewmodel.MainViewModel;
import org.springframework.stereotype.Component;

@Component
public class MainView {
    @FXML
    private ToggleGroup toolToggleGroup;
    @FXML
    private ToggleButton selectToolButton;
    @FXML
    private ToggleButton triangleToolButton;
    @FXML
    private ToggleButton quadrilateralToolButton;
    @FXML
    private ToggleButton ellipseToolButton;
    @FXML
    private ToggleButton lineToolButton;
    @FXML
    private ToggleButton freehandToolButton;

    @FXML
    private Pane canvasPane;
    @FXML
    private Canvas canvas;

    private final MainViewModel shapeViewModel;

    private GraphicsContext gc;

    public MainView(MainViewModel shapeViewModel) {
        this.shapeViewModel = shapeViewModel;
    }

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        bindProperties();
        setupCanvasListeners();

        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());

        canvas.widthProperty().addListener(evt -> redrawCanvas());
        canvas.heightProperty().addListener(evt -> redrawCanvas());

        redrawCanvas();
    }

    private void bindProperties() {
        toolToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                ToggleButton selectedButton = (ToggleButton) newToggle;
                shapeViewModel.setToolSelected(selectedButton.getText());
            }
        });
    }

    private void setupCanvasListeners() {
        canvas.setOnMousePressed(mouseEvent -> {
            shapeViewModel.onMousePressed(mouseEvent.getX(), mouseEvent.getY());
            redrawCanvas();
        });
        canvas.setOnMouseDragged(mouseEvent -> {
            shapeViewModel.onMouseDragged(mouseEvent.getX(), mouseEvent.getY());
            redrawCanvas();
        });
        canvas.setOnMouseReleased(mouseEvent -> {
            shapeViewModel.onMouseReleased();
            redrawCanvas();
        });
    }

    private void redrawCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (ShapeModel shape : shapeViewModel.getShapes()) {
            shape.draw(gc);
        }

        if (shapeViewModel.getTempShape() != null) {
            shapeViewModel.getTempShape().draw(gc);
        }
    }
}
