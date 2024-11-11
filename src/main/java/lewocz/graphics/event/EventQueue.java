package lewocz.graphics.event;

import lewocz.graphics.command.Command;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue {
    private final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();
    private volatile boolean isProcessing = false;

    public void enqueue(Command command) {
        commandQueue.offer(command);
        processQueue();
    }

    private void processQueue() {
        if (isProcessing) return;

        isProcessing = true;
        new Thread(() -> {
            try {
                while (!commandQueue.isEmpty()) {
                    Command command = commandQueue.poll();
                    if (command != null) {
                        command.execute();
                    }
                }
            } finally {
                isProcessing = false;
            }
        }).start();
    }
}
