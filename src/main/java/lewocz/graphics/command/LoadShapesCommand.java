package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class LoadShapesCommand implements Command {
    private final IMainViewModel viewModel;
    private final String filePath;

    public LoadShapesCommand(IMainViewModel viewModel, String filePath) {
        this.viewModel = viewModel;
        this.filePath = filePath;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> viewModel.setIsProcessing(true));
        try {
            viewModel.loadShapesFromFile(filePath);
        } finally {
            Platform.runLater(() -> viewModel.setIsProcessing(false));
        }
    }
}