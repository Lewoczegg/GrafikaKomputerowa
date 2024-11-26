package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyPercentBlackSelectionCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final double percentBlack;

    public ApplyPercentBlackSelectionCommand(IMainViewModel mainViewModel, double percentBlack) {
        this.mainViewModel = mainViewModel;
        this.percentBlack = percentBlack;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyPercentBlackSelection(percentBlack);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}