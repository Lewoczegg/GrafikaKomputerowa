package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyGrayscaleMaxCommand implements Command {
    private final IMainViewModel mainViewModel;

    public ApplyGrayscaleMaxCommand(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public void execute() {
        mainViewModel.applyGrayscaleMax();
    }
}