package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class AdjustBrightnessCommand implements Command {

    private final IMainViewModel mainViewModel;
    private final double brightnessChange;

    public AdjustBrightnessCommand(IMainViewModel mainViewModel, double brightnessChange) {
        this.mainViewModel = mainViewModel;
        this.brightnessChange = brightnessChange;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.adjustBrightness(brightnessChange);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}