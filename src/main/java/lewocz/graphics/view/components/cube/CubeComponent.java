package lewocz.graphics.view.components.cube;

import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lewocz.graphics.viewmodel.IMainViewModel;
import org.springframework.stereotype.Component;

@Component
public class CubeComponent {
    @FXML
    private Pane cubePane;

    private Group root3D;
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    private double rotateX = 0;
    private double rotateY = 0;

    private final IMainViewModel mainViewModel;

    public CubeComponent(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @FXML
    public void initialize() {
        setUp3DScene();
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

        Group rgbCube = mainViewModel.createRGBColoredCube(30);
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
}
