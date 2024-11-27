package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyClosingCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final boolean[][] structuringElement;

    public ApplyClosingCommand(IMainViewModel mainViewModel, boolean[][] structuringElement) {
        this.mainViewModel = mainViewModel;
        this.structuringElement = structuringElement;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyClosing(structuringElement);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}