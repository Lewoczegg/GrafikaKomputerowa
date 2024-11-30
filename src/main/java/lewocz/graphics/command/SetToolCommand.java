package lewocz.graphics.command;

import lewocz.graphics.model.Tool;
import lewocz.graphics.viewmodel.IMainViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetToolCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(SetToolCommand.class);
    private final IMainViewModel viewModel;
    private final Tool tool;

    public SetToolCommand(IMainViewModel viewModel, Tool tool) {
        this.viewModel = viewModel;
        this.tool = tool;
    }

    @Override
    public void execute() {
        log.info("Set tool: " + tool.name());
        viewModel.setSelectedTool(tool);
    }
}