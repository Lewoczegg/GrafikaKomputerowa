package lewocz.graphics.command;

import javafx.scene.image.WritableImage;
import lewocz.graphics.model.PNMFormat;
import lewocz.graphics.viewmodel.IMainViewModel;

public class SaveCommand implements Command {
    private final IMainViewModel viewModel;
    private final String fileName;
    private final boolean binaryFormat;
    private final WritableImage image;
    private final PNMFormat format;

    public SaveCommand(IMainViewModel viewModel, String fileName, boolean binaryFormat, WritableImage image, PNMFormat format) {
        this.viewModel = viewModel;
        this.fileName = fileName;
        this.binaryFormat = binaryFormat;
        this.image = image;
        this.format = format;
    }

    @Override
    public void execute() {
        viewModel.saveImage(fileName, format, binaryFormat, image);
    }
}