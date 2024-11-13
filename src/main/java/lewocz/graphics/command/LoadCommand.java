package lewocz.graphics.command;

import javafx.scene.canvas.GraphicsContext;
import lewocz.graphics.viewmodel.IMainViewModel;

public class LoadCommand implements Command {
    private final IMainViewModel viewModel;
    private final String fileName;
    private final String formatType; // "PBM", "PGM", or "PPM"

    public LoadCommand(IMainViewModel viewModel, String fileName, String formatType) {
        this.viewModel = viewModel;
        this.fileName = fileName;
        this.formatType = formatType;
    }

    @Override
    public void execute() {
        switch (formatType) {
            case "PBM":
                viewModel.loadFromPBM(fileName);
                break;
            case "PGM":
                viewModel.loadFromPGM(fileName);
                break;
            case "PPM":
                viewModel.loadFromPPM(fileName);
                break;
        }
    }
}