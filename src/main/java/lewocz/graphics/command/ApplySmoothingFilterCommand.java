package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplySmoothingFilterCommand implements Command {
    private final IMainViewModel mainViewModel;

    public ApplySmoothingFilterCommand(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public void execute() {
        mainViewModel.applySmoothingFilter();
    }
}