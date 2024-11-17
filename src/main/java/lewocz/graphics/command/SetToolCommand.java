package lewocz.graphics.command;

import lewocz.graphics.model.Tool;
import lewocz.graphics.viewmodel.IMainViewModel;

public class SetToolCommand implements Command {
    private final IMainViewModel viewModel;
    private final Tool tool;

    public SetToolCommand(IMainViewModel viewModel, Tool tool) {
        this.viewModel = viewModel;
        this.tool = tool;
    }

    @Override
    public void execute() {
        viewModel.setSelectedTool(tool);
    }
}