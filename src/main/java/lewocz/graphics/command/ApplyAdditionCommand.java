package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyAdditionCommand implements Command {

    private final IMainViewModel mainViewModel;
    private final double addRed;
    private final double addGreen;
    private final double addBlue;

    public ApplyAdditionCommand(IMainViewModel mainViewModel, double addRed, double addGreen, double addBlue) {
        this.mainViewModel = mainViewModel;
        this.addRed = addRed;
        this.addGreen = addGreen;
        this.addBlue = addBlue;
    }

    @Override
    public void execute() {
        mainViewModel.applyAddition(addRed, addGreen, addBlue);
    }
}