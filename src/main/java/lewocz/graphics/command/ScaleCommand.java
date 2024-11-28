package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class ScaleCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final double factor;
    private final double pivotX;
    private final double pivotY;

    public ScaleCommand(IMainViewModel mainViewModel, double factor, double pivotX, double pivotY) {
        this.mainViewModel = mainViewModel;
        this.factor = factor;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
    }

    @Override
    public void execute() {
        mainViewModel.scaleShape(factor, pivotX, pivotY);
    }
}