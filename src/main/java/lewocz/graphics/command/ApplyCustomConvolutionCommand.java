package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyCustomConvolutionCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final double[][] kernel;

    public ApplyCustomConvolutionCommand(IMainViewModel mainViewModel, double[][] kernel) {
        this.mainViewModel = mainViewModel;
        this.kernel = kernel;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyCustomConvolution(kernel);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}