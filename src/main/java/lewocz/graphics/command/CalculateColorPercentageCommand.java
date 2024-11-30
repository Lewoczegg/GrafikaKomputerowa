package lewocz.graphics.command;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import lewocz.graphics.viewmodel.IMainViewModel;

public class CalculateColorPercentageCommand implements Command {
    private final IMainViewModel viewModel;
    private final Color targetColor;
    private final double tolerance;

    public CalculateColorPercentageCommand(IMainViewModel viewModel, Color targetColor, double tolerance) {
        this.viewModel = viewModel;
        this.targetColor = targetColor;
        this.tolerance = tolerance;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> viewModel.setIsProcessing(true));
        try {
            viewModel.calculateColorPercentage(targetColor, tolerance);
        } finally {
            Platform.runLater(() -> viewModel.setIsProcessing(false));
        }
    }
}