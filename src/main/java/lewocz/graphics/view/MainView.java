package lewocz.graphics.view;

import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.converter.NumberStringConverter;
import lewocz.graphics.command.*;
import lewocz.graphics.event.EventQueue;
import lewocz.graphics.model.ShapeModel;
import lewocz.graphics.viewmodel.IMainViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MainView {
    private static final Logger log = LoggerFactory.getLogger(MainView.class);
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

    // Color Preview and Labels
    @FXML
    private Rectangle colorPreview;
    @FXML
    private Label rgbValueLabel;
    @FXML
    private Label cmykValueLabel;
    @FXML
    private Label hsvValueLabel;

    // RGB Controls
    @FXML
    private Slider redSlider;
    @FXML
    private TextField redTextField;
    @FXML
    private Slider greenSlider;
    @FXML
    private TextField greenTextField;
    @FXML
    private Slider blueSlider;
    @FXML
    private TextField blueTextField;

    // CMYK Controls
    @FXML
    private Slider cyanSlider;
    @FXML
    private TextField cyanTextField;
    @FXML
    private Slider magentaSlider;
    @FXML
    private TextField magentaTextField;
    @FXML
    private Slider yellowSlider;
    @FXML
    private TextField yellowTextField;
    @FXML
    private Slider keySlider;
    @FXML
    private TextField keyTextField;

    // HSV Controls
    @FXML
    private Slider hueSlider;
    @FXML
    private TextField hueTextField;
    @FXML
    private Slider saturationSlider;
    @FXML
    private TextField saturationTextField;
    @FXML
    private Slider valueSlider;
    @FXML
    private TextField valueTextField;

    @FXML
    private Pane canvasPane;
    @FXML
    private Canvas canvas;

    @FXML
    private Pane cubePane;

    private Group root3D;
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    private double rotateX = 0;
    private double rotateY = 0;

    private final IMainViewModel mainViewModel;
    private final EventQueue eventQueue = new EventQueue();

    private GraphicsContext gc;

    public MainView(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        bindProperties();
        setupCanvasListeners();
        bindColorProperties();
        setUp3DScene();
        mainViewModel.setRedrawCanvasCallback(this::redrawCanvas);
        redrawCanvas();
    }

    private void setUp3DScene() {
        root3D = new Group();

        SubScene subScene3D = new SubScene(root3D, 200, 200, true, SceneAntialiasing.BALANCED);
        subScene3D.widthProperty().bind(cubePane.widthProperty());
        subScene3D.heightProperty().bind(cubePane.heightProperty());

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
                new Rotate(-20, Rotate.Y_AXIS),
                new Rotate(-20, Rotate.X_AXIS),
                new Translate(0, 0, -500)
        );
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        subScene3D.setCamera(camera);

        Group rgbCube =  mainViewModel.createRGBColoredCube(30);
        root3D.getChildren().add(rgbCube);

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        root3D.getChildren().add(ambientLight);

        addMouseControl(root3D, subScene3D);

        cubePane.getChildren().add(subScene3D);
    }

    private void addMouseControl(Group group, SubScene scene) {
        scene.setOnMousePressed((MouseEvent event) -> {
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        scene.setOnMouseDragged((MouseEvent event) -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            rotateX += (mousePosY - mouseOldY);
            rotateY += (mousePosX - mouseOldX);
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            group.getTransforms().clear();
            group.getTransforms().addAll(
                    new Rotate(rotateY, Rotate.Y_AXIS),
                    new Rotate(rotateX, Rotate.X_AXIS)
            );
        });
    }

    private void bindProperties() {
        toolToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                ToggleButton selectedButton = (ToggleButton) newToggle;
                Command command = new SetToolCommand(mainViewModel, selectedButton.getText());
                eventQueue.enqueue(command);
            }
        });

        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());
    }

    private void bindColorProperties() {
        // RGB bindings
        redSlider.valueProperty().bindBidirectional(mainViewModel.redProperty());
        redTextField.textProperty().bindBidirectional(mainViewModel.redProperty(), new NumberStringConverter());

        greenSlider.valueProperty().bindBidirectional(mainViewModel.greenProperty());
        greenTextField.textProperty().bindBidirectional(mainViewModel.greenProperty(), new NumberStringConverter());

        blueSlider.valueProperty().bindBidirectional(mainViewModel.blueProperty());
        blueTextField.textProperty().bindBidirectional(mainViewModel.blueProperty(), new NumberStringConverter());

        // CMYK bindings
        cyanSlider.valueProperty().bindBidirectional(mainViewModel.cyanProperty());
        cyanTextField.textProperty().bindBidirectional(mainViewModel.cyanProperty(), new NumberStringConverter());

        magentaSlider.valueProperty().bindBidirectional(mainViewModel.magentaProperty());
        magentaTextField.textProperty().bindBidirectional(mainViewModel.magentaProperty(), new NumberStringConverter());

        yellowSlider.valueProperty().bindBidirectional(mainViewModel.yellowProperty());
        yellowTextField.textProperty().bindBidirectional(mainViewModel.yellowProperty(), new NumberStringConverter());

        keySlider.valueProperty().bindBidirectional(mainViewModel.keyProperty());
        keyTextField.textProperty().bindBidirectional(mainViewModel.keyProperty(), new NumberStringConverter());

        // HSV bindings
        hueSlider.valueProperty().bindBidirectional(mainViewModel.hueProperty());
        hueTextField.textProperty().bindBidirectional(mainViewModel.hueProperty(), new NumberStringConverter());

        saturationSlider.valueProperty().bindBidirectional(mainViewModel.saturationProperty());
        saturationTextField.textProperty().bindBidirectional(mainViewModel.saturationProperty(), new NumberStringConverter());

        valueSlider.valueProperty().bindBidirectional(mainViewModel.valueProperty());
        valueTextField.textProperty().bindBidirectional(mainViewModel.valueProperty(), new NumberStringConverter());

        // Update color preview and labels
        mainViewModel.selectedColorProperty().addListener((obs, oldColor, newColor) -> {
            colorPreview.setFill(newColor);

            rgbValueLabel.setText(String.format("RGB: %d, %d, %d",
                    mainViewModel.redProperty().get(),
                    mainViewModel.greenProperty().get(),
                    mainViewModel.blueProperty().get()));

            cmykValueLabel.setText(String.format("CMYK: %.1f%%, %.1f%%, %.1f%%, %.1f%%",
                    mainViewModel.cyanProperty().get(),
                    mainViewModel.magentaProperty().get(),
                    mainViewModel.yellowProperty().get(),
                    mainViewModel.keyProperty().get()));

            hsvValueLabel.setText(String.format("HSV: %.1f°, %.1f%%, %.1f%%",
                    mainViewModel.hueProperty().get(),
                    mainViewModel.saturationProperty().get(),
                    mainViewModel.valueProperty().get()));
        });

        // Initialize the color preview
        colorPreview.setFill(mainViewModel.selectedColorProperty().get());
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
