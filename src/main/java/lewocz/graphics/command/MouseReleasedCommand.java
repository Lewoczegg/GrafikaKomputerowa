package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class MouseReleasedCommand implements Command {
    private final IMainViewModel viewModel;

    public MouseReleasedCommand(IMainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void execute() {
        viewModel.onMouseReleased();
    }
}