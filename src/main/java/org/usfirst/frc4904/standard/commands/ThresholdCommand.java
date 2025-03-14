package org.usfirst.frc4904.standard.commands;

import edu.wpi.first.wpilibj2.command.Command;
import java.util.function.Supplier;

/**
 * Threshold command takes in a command, a supplier, and a threshold. When the
 * threshold is passed by the supplier, it starts the command. When the
 * threshold stops being passed, it cancels the command.
 */
public class ThresholdCommand<T extends Comparable<T>> extends Command {

    protected final Command command;
    protected final Supplier<T> supplier;
    protected final T threshold;
    protected final boolean invert;

    public ThresholdCommand(
        String name,
        Command command,
        Supplier<T> supplier,
        T threshold,
        boolean invert
    ) {
        setName(name);
        this.command = command;
        this.supplier = supplier;
        this.threshold = threshold;
        this.invert = invert;
    }

    public ThresholdCommand(Command command, Supplier<T> supplier, T threshold, boolean invert) {
        this("ThresholdCommand[" + command.getName() + "]", command, supplier, threshold, invert);
    }

    public ThresholdCommand(Command command, Supplier<T> axis, T threshold) {
        this(command, axis, threshold, false);
    }

    protected boolean pastThreshold() {
        int compare = supplier.get().compareTo(threshold);
        return invert ? compare <= 0 : compare >= 0;
    }

    @Override
    public void execute() {
        if (pastThreshold() && !command.isScheduled()) {
            command.schedule();
        } else if (!pastThreshold() && command.isScheduled()) {
            command.cancel();
        }
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
