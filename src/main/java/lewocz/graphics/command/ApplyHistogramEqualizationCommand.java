package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyHistogramEqualizationCommand implements Command {
    private final IMainViewModel mainViewModel;

    public ApplyHistogramEqualizationCommand(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyHistogramEqualization();
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}