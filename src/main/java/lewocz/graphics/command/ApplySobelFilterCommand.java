package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplySobelFilterCommand implements Command {
    private final IMainViewModel mainViewModel;

    public ApplySobelFilterCommand(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public void execute() {
        mainViewModel.applySobelFilter();
    }
}