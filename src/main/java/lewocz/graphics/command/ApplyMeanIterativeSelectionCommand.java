package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyMeanIterativeSelectionCommand implements Command {
    private final IMainViewModel mainViewModel;

    public ApplyMeanIterativeSelectionCommand(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyMeanIterativeSelection();
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}