package lewocz.graphics;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class Main extends Application {

	private ConfigurableApplicationContext context;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		context = SpringApplication.run(Main.class);

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));

		fxmlLoader.setControllerFactory(context::getBean);

		Parent root = fxmlLoader.load();

		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Grafika komputerowa");
		stage.setMaximized(true);
		stage.show();
	}

	@Override
	public void stop() {
		context.close();
		Platform.exit();
	}
}
