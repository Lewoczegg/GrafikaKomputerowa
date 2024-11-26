package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplySauvolaThresholdingCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final int windowSize;
    private final double k;
    private final double r;

    public ApplySauvolaThresholdingCommand(IMainViewModel mainViewModel, int windowSize, double k, double r) {
        this.mainViewModel = mainViewModel;
        this.windowSize = windowSize;
        this.k = k;
        this.r = r;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applySauvolaThresholding(windowSize, k, r);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}