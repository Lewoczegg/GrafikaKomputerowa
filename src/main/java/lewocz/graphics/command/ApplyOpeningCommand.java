package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyOpeningCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final boolean[][] structuringElement;

    public ApplyOpeningCommand(IMainViewModel mainViewModel, boolean[][] structuringElement) {
        this.mainViewModel = mainViewModel;
        this.structuringElement = structuringElement;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyOpening(structuringElement);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}