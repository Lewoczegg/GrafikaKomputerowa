package lewocz.graphics.command;

import javafx.scene.canvas.GraphicsContext;
import lewocz.graphics.viewmodel.IMainViewModel;

public class LoadCommand implements Command {
    private final IMainViewModel viewModel;
    private final String fileName;
    private final GraphicsContext gc;
    private final String formatType; // "PBM", "PGM", or "PPM"

    public LoadCommand(IMainViewModel viewModel, String fileName, GraphicsContext gc, String formatType) {
        this.viewModel = viewModel;
        this.fileName = fileName;
        this.gc = gc;
        this.formatType = formatType;
    }

    @Override
    public void execute() {
        if (formatType.equals("PBM")) {
            viewModel.loadFromPBM(fileName, gc);
        } else if (formatType.equals("PGM")) {
            viewModel.loadFromPGM(fileName, gc);
        } else if (formatType.equals("PPM")) {
            viewModel.loadFromPPM(fileName, gc);
        }
    }
}