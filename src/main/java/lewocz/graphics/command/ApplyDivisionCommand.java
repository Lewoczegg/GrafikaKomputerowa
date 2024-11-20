package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyDivisionCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final double divRed;
    private final double divGreen;
    private final double divBlue;

    public ApplyDivisionCommand(IMainViewModel mainViewModel, double divRed, double divGreen, double divBlue) {
        this.mainViewModel = mainViewModel;
        this.divRed = divRed;
        this.divGreen = divGreen;
        this.divBlue = divBlue;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyDivision(divRed, divGreen, divBlue);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}