package lewocz.graphics.view.components.toolbar;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ToggleGroup;
import lewocz.graphics.command.Command;
import lewocz.graphics.command.SetToolCommand;
import lewocz.graphics.event.EventQueue;
import lewocz.graphics.model.Tool;
import lewocz.graphics.viewmodel.IMainViewModel;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ToolBarComponent {

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
    private ToggleButton bezierToolButton;
    @FXML
    private ToggleButton polygonToolButton;
    @FXML
    private Button finishPolygonButton;
    @FXML
    private ToggleButton rotateToolButton;
    @FXML
    private ToggleButton scaleToolButton;

    private IMainViewModel mainViewModel;
    private EventQueue eventQueue;

    public ToolBarComponent(EventQueue eventQueue, IMainViewModel mainViewModel) {
        this.eventQueue = eventQueue;
        this.mainViewModel = mainViewModel;
    }

    @FXML
    public void initialize() {
        // Initialize user data for tools
        selectToolButton.setUserData(Tool.SELECT);
        triangleToolButton.setUserData(Tool.TRIANGLE);
        quadrilateralToolButton.setUserData(Tool.QUADRILATERAL);
        ellipseToolButton.setUserData(Tool.ELLIPSE);
        lineToolButton.setUserData(Tool.LINE);
        freehandToolButton.setUserData(Tool.FREEHAND);
        bezierToolButton.setUserData(Tool.BEZIER);
        polygonToolButton.setUserData(Tool.POLYGON);
        rotateToolButton.setUserData(Tool.ROTATE);
        scaleToolButton.setUserData(Tool.SCALE);

        // Set up the toggle group listener
        toolToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null && mainViewModel != null) {
                ToggleButton selectedButton = (ToggleButton) newToggle;
                Tool selectedTool = (Tool) selectedButton.getUserData();
                Command command = new SetToolCommand(mainViewModel, selectedTool);
                eventQueue.enqueue(command);

                if (selectedTool == Tool.BEZIER) {
                    promptForBezierDegree();
                }
            }
        });
    }

    private void promptForBezierDegree() {
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog("3");
            dialog.setTitle("Bezier Curve Degree");
            dialog.setHeaderText("Enter the degree of the Bezier curve:");
            dialog.setContentText("Degree:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(degreeStr -> {
                try {
                    int degree = Integer.parseInt(degreeStr);
                    mainViewModel.setBezierDegree(degree);
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid integer for the degree.");
                }
            });
        });
    }

    @FXML
    private void onFinishPolygon() {
        mainViewModel.finishPolygon();
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
