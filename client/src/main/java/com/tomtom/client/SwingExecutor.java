package com.tomtom.client;

import javax.swing.*;
import java.util.concurrent.Executor;

public class SwingExecutor implements Executor {
    @Override
    public void execute(final Runnable runnable) {
        SwingUtilities.invokeLater(runnable);
    }
}
