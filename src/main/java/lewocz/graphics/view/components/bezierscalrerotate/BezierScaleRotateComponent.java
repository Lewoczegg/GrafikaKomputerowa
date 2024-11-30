package lewocz.graphics.view.components.bezierscalrerotate;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import lewocz.graphics.command.Command;
import lewocz.graphics.command.RotateCommand;
import lewocz.graphics.command.ScaleCommand;
import lewocz.graphics.event.EventQueue;
import lewocz.graphics.model.BezierCurveModel;
import lewocz.graphics.model.SerializablePoint;
import lewocz.graphics.model.ShapeModel;
import lewocz.graphics.viewmodel.IMainViewModel;
import org.springframework.stereotype.Component;

import static lewocz.graphics.view.utils.AlertUtils.showAlert;

@Component
public class BezierScaleRotateComponent {

    @FXML
    private TableView<SerializablePoint> controlPointsTable;
    @FXML
    private TableColumn<SerializablePoint, Number> pointIndexColumn;
    @FXML
    private TableColumn<SerializablePoint, Double> pointXColumn;
    @FXML
    private TableColumn<SerializablePoint, Double> pointYColumn;

    @FXML
    private TextField rotationPivotXField;
    @FXML
    private TextField rotationPivotYField;
    @FXML
    private TextField rotationAngleField;
    @FXML
    private Button applyRotationButton;

    @FXML
    private TextField scalingPivotXField;
    @FXML
    private TextField scalingPivotYField;
    @FXML
    private TextField scaleFactorField;
    @FXML
    private Button applyScaleButton;

    private final IMainViewModel mainViewModel;
    private final EventQueue eventQueue;

    private ObservableList<SerializablePoint> controlPointsData = FXCollections.observableArrayList();

    public BezierScaleRotateComponent(IMainViewModel mainViewModel, EventQueue eventQueue) {
        this.mainViewModel = mainViewModel;
        this.eventQueue = eventQueue;
    }

    @FXML
    public void initialize() {
        bindControlPointsTable();
        bindActions();

        mainViewModel.currentShapeProperty().addListener((obs, oldShape, newShape) -> {
            if (newShape instanceof BezierCurveModel) {
                BezierCurveModel bezierCurve = (BezierCurveModel) newShape;
                controlPointsData.setAll(bezierCurve.getControlPoints());
            } else {
                controlPointsData.clear();
            }
        });
    }

    private void bindControlPointsTable() {
        pointIndexColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(controlPointsData.indexOf(cellData.getValue())));
        pointXColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getX()).asObject());
        pointYColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getY()).asObject());

        controlPointsTable.setItems(controlPointsData);

        controlPointsTable.setEditable(true);
        pointXColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        pointYColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        pointXColumn.setOnEditCommit(event -> {
            SerializablePoint point = event.getRowValue();
            int index = controlPointsData.indexOf(point);
            Double newValue = event.getNewValue();
            SerializablePoint newPoint = new SerializablePoint(newValue, point.getY());
            controlPointsData.set(index, newPoint);
            updateBezierControlPoint(index, newPoint);
        });

        pointYColumn.setOnEditCommit(event -> {
            SerializablePoint point = event.getRowValue();
            int index = controlPointsData.indexOf(point);
            Double newValue = event.getNewValue();
            SerializablePoint newPoint = new SerializablePoint(point.getX(), newValue);
            controlPointsData.set(index, newPoint);
            updateBezierControlPoint(index, newPoint);
        });
    }

    private void updateBezierControlPoint(int index, SerializablePoint newPoint) {
        ShapeModel shape = mainViewModel.getCurrentShape();
        if (shape instanceof BezierCurveModel) {
            BezierCurveModel bezierCurve = (BezierCurveModel) shape;
            bezierCurve.getControlPoints().set(index, newPoint);
            controlPointsData.set(index, newPoint);
            mainViewModel.requestRedraw();
        }
    }

    private void bindActions() {
        applyRotationButton.setOnAction(e -> onApplyRotation());
        applyScaleButton.setOnAction(e -> onApplyScaling());
    }

    private void onApplyRotation() {
        try {
            double pivotX = Double.parseDouble(rotationPivotXField.getText());
            double pivotY = Double.parseDouble(rotationPivotYField.getText());
            double angle = Double.parseDouble(rotationAngleField.getText());

            if (mainViewModel.getCurrentShape() == null) {
                showAlert("No Shape Selected", "Please select a shape to rotate.");
                return;
            }

            Command command = new RotateCommand(mainViewModel, angle, pivotX, pivotY);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for pivot point and angle.");
        }
    }

    private void onApplyScaling() {
        try {
            double pivotX = Double.parseDouble(scalingPivotXField.getText());
            double pivotY = Double.parseDouble(scalingPivotYField.getText());
            double factor = Double.parseDouble(scaleFactorField.getText());

            if (mainViewModel.getCurrentShape() == null) {
                showAlert("No Shape Selected", "Please select a shape to scale.");
                return;
            }

            Command command = new ScaleCommand(mainViewModel, factor, pivotX, pivotY);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for pivot point and scale factor.");
        }
    }
}