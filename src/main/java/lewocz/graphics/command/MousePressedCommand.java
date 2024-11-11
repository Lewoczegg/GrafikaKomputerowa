package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class MousePressedCommand implements Command {
    private final IMainViewModel viewModel;
    private final double x, y;

    public MousePressedCommand(IMainViewModel viewModel, double x, double y) {
        this.viewModel = viewModel;
        this.x = x;
        this.y = y;
    }

    @Override
    public void execute() {
        viewModel.onMousePressed(x, y);
    }
}
