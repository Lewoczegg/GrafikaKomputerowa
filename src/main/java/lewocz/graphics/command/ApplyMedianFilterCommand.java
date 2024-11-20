package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyMedianFilterCommand implements Command {

    private final IMainViewModel mainViewModel;

    public ApplyMedianFilterCommand(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public void execute() {
        mainViewModel.applyMedianFilter();
    }
}