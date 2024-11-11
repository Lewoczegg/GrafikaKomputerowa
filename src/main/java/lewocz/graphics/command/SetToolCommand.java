package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class SetToolCommand implements Command {
    private final IMainViewModel viewModel;
    private final String tool;

    public SetToolCommand(IMainViewModel viewModel, String tool) {
        this.viewModel = viewModel;
        this.tool = tool;
    }

    @Override
    public void execute() {
        viewModel.setToolSelected(tool);
    }
}