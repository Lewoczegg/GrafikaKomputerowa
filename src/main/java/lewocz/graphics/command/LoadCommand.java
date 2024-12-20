package lewocz.graphics.command;

import javafx.application.Platform;
import lewocz.graphics.model.PNMFormat;
import lewocz.graphics.viewmodel.IMainViewModel;

public class LoadCommand implements Command {
    private final IMainViewModel viewModel;
    private final String fileName;
    private final PNMFormat format;

    public LoadCommand(IMainViewModel viewModel, String fileName, PNMFormat format) {
        this.viewModel = viewModel;
        this.fileName = fileName;
        this.format = format;
    }

    @Override
    public void execute() {
        Platform.runLater(() -> viewModel.setIsProcessing(true));
        try {
            if (format != null) {
                viewModel.loadImage(fileName, format);
            } else {
                viewModel.loadStandardImage(fileName);
            }
        } finally {
            Platform.runLater(() -> viewModel.setIsProcessing(false));
        }
    }
}