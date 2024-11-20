package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyMultiplicationCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final double mulRed;
    private final double mulGreen;
    private final double mulBlue;

    public ApplyMultiplicationCommand(IMainViewModel mainViewModel, double mulRed, double mulGreen, double mulBlue) {
        this.mainViewModel = mainViewModel;
        this.mulRed = mulRed;
        this.mulGreen = mulGreen;
        this.mulBlue = mulBlue;
    }

    @Override
    public void execute() {
        mainViewModel.applyMultiplication(mulRed, mulGreen, mulBlue);
    }
}