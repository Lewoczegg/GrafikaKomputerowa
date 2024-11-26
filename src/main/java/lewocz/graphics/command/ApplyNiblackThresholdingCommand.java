package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyNiblackThresholdingCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final int windowSize;
    private final double k;

    public ApplyNiblackThresholdingCommand(IMainViewModel mainViewModel, int windowSize, double k) {
        this.mainViewModel = mainViewModel;
        this.windowSize = windowSize;
        this.k = k;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyNiblackThresholding(windowSize, k);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}