package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class AdjustBrightnessCommand implements Command {

    private final IMainViewModel mainViewModel;
    private final double brightnessChange;

    public AdjustBrightnessCommand(IMainViewModel mainViewModel, double brightnessChange) {
        this.mainViewModel = mainViewModel;
        this.brightnessChange = brightnessChange;
    }

    @Override
    public void execute() {
        mainViewModel.adjustBrightness(brightnessChange);
    }
}