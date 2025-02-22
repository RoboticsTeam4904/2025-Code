package org.usfirst.frc4904.standard.commands;

import edu.wpi.first.wpilibj2.command.Command;
import java.util.function.BooleanSupplier;

public class RunIf extends RunIfElse {

    /**
     * Run a command based on a conditional callback. For example, if you only want
     * to shoot if a shooter is ready (based on its isReady() function), use: new
     * RunIf(new Shoot(), shooter::isReady) AND's the conditions
     *
     * @param command          The command to be run if the condition is met
     * @param booleanSuppliers A variable number of condition functions (ANDed)
     *                         using Java 8's colon syntax
     */
    public RunIf(Command command, BooleanSupplier... booleanSuppliers) {
        super(command, new Noop(), booleanSuppliers);
    }
}
