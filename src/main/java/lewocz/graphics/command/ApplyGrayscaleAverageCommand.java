package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyGrayscaleAverageCommand implements Command {
    private final IMainViewModel mainViewModel;

    public ApplyGrayscaleAverageCommand(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public void execute() {
        mainViewModel.applyGrayscaleAverage();
    }
}