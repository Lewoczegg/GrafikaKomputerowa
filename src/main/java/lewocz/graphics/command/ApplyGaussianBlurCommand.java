package lewocz.graphics.command;

import lewocz.graphics.viewmodel.IMainViewModel;

public class ApplyGaussianBlurCommand implements Command {
    private final IMainViewModel mainViewModel;
    private final int kernelSize;
    private final double sigma;

    public ApplyGaussianBlurCommand(IMainViewModel mainViewModel, int kernelSize, double sigma) {
        this.mainViewModel = mainViewModel;
        this.kernelSize = kernelSize;
        this.sigma = sigma;
    }

    @Override
    public void execute() {
        mainViewModel.applyGaussianBlur(kernelSize, sigma);
    }
}