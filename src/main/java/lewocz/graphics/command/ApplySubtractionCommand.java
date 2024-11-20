package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplySubtractionCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final double subRed;
    private final double subGreen;
    private final double subBlue;

    public ApplySubtractionCommand(IMainViewModel mainViewModel, double subRed, double subGreen, double subBlue) {
        this.mainViewModel = mainViewModel;
        this.subRed = subRed;
        this.subGreen = subGreen;
        this.subBlue = subBlue;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applySubtraction(subRed, subGreen, subBlue);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}