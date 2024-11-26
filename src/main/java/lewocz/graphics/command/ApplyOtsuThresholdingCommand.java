package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyOtsuThresholdingCommand implements Command {
    private final IMainViewModel mainViewModel;

    public ApplyOtsuThresholdingCommand(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyOtsuThresholding();
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}