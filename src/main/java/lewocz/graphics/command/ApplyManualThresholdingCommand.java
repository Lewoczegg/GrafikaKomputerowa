package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyManualThresholdingCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final int threshold;

    public ApplyManualThresholdingCommand(IMainViewModel mainViewModel, int threshold) {
        this.mainViewModel = mainViewModel;
        this.threshold = threshold;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyManualThresholding(threshold);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}