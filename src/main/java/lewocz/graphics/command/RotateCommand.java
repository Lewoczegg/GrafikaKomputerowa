package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class RotateCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final double angle;
    private final double pivotX;
    private final double pivotY;

    public RotateCommand(IMainViewModel mainViewModel, double angle, double pivotX, double pivotY) {
        this.mainViewModel = mainViewModel;
        this.angle = angle;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
    }

    @Override
    public void execute() {
        mainViewModel.rotateShape(angle, pivotX, pivotY);
    }
}