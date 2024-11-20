package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyMultiplicationCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final double mulRed;
    private final double mulGreen;
    private final double mulBlue;

    public ApplyMultiplicationCommand(IMainViewModel mainViewModel, double mulRed, double mulGreen, double mulBlue) {
        this.mainViewModel = mainViewModel;
        this.mulRed = mulRed;
        this.mulGreen = mulGreen;
        this.mulBlue = mulBlue;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyMultiplication(mulRed, mulGreen, mulBlue);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}