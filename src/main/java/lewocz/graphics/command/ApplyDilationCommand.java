package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyDilationCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final boolean[][] structuringElement;

    public ApplyDilationCommand(IMainViewModel mainViewModel, boolean[][] structuringElement) {
        this.mainViewModel = mainViewModel;
        this.structuringElement = structuringElement;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyDilation(structuringElement);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}