package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyGrayscaleMaxCommand implements Command {
    private final IMainViewModel mainViewModel;

    public ApplyGrayscaleMaxCommand(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyGrayscaleMax();
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}