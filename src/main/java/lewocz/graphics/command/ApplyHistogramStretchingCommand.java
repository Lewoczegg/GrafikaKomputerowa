package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyHistogramStretchingCommand implements Command {
    private final IMainViewModel mainViewModel;

    public ApplyHistogramStretchingCommand(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyHistogramStretching();
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}