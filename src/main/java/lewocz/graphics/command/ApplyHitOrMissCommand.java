package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyHitOrMissCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final boolean[][] hitMask;
    private final boolean[][] missMask;

    public ApplyHitOrMissCommand(IMainViewModel mainViewModel, boolean[][] hitMask, boolean[][] missMask) {
        this.mainViewModel = mainViewModel;
        this.hitMask = hitMask;
        this.missMask = missMask;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> mainViewModel.setIsProcessing(true));
        try {
            mainViewModel.applyHitOrMiss(hitMask, missMask);
        } finally {
            Platform.runLater(() -> mainViewModel.setIsProcessing(false));
        }
    }
}