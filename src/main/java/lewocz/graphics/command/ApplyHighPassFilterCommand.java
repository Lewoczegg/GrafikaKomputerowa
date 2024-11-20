package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyHighPassFilterCommand implements Command {
    private final IMainViewModel mainViewModel;

    public ApplyHighPassFilterCommand(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public void execute() {
        mainViewModel.applyHighPassFilter();
    }
}