package lewocz.graphics.command;

import javafx.scene.image.WritableImage;
import lewocz.graphics.viewmodel.IMainViewModel;

public class SaveCommand implements Command {
    private final IMainViewModel viewModel;
    private final String fileName;
    private final boolean binaryFormat;
    private final WritableImage image;
    private final String formatType; // "PBM", "PGM", or "PPM"

    public SaveCommand(IMainViewModel viewModel, String fileName, boolean binaryFormat, WritableImage image, String formatType) {
        this.viewModel = viewModel;
        this.fileName = fileName;
        this.binaryFormat = binaryFormat;
        this.image = image;
        this.formatType = formatType;
    }

    @Override
    public void execute() {
        if (formatType.equals("PBM")) {
            viewModel.saveToPBM(fileName, binaryFormat, image);
        } else if (formatType.equals("PGM")) {
            viewModel.saveToPGM(fileName, binaryFormat, image);
        } else if (formatType.equals("PPM")) {
            viewModel.saveToPPM(fileName, binaryFormat, image);
        }
    }
}