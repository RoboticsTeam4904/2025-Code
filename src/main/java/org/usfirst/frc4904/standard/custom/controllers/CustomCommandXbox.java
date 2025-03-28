package org.usfirst.frc4904.standard.custom.controllers;

import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

public class CustomCommandXbox extends CommandXboxController {
    private final double deadZoneSize;
    private final CommandXboxController hid;

    public CustomCommandXbox(int port, double deadZoneSize) {
        super(port);
        hid = new CommandXboxController(port);
        if (deadZoneSize < 0 || deadZoneSize >= 1) {
            throw new IllegalArgumentException("CustomCommandXBox deadzone must be in [0, 1]");
        }
        this.deadZoneSize = deadZoneSize;
    }

    public double getLeftX() {
        return applyDeadzone(hid.getLeftX(), deadZoneSize);
    }

    /**
     * Get the X axis value of right side of the controller.
     *
     * @return The axis value.
     */
    public double getRightX() {
        return applyDeadzone(hid.getRightX(), deadZoneSize);
    }

    /**
     * Get the Y axis value of left side of the controller.
     *
     * @return The axis value.
     */
    public double getLeftY() {
        return applyDeadzone(hid.getLeftY(), deadZoneSize);
    }

    /**
     * Get the Y axis value of right side of the controller.
     *
     * @return The axis value.
     */
    public double getRightY() {
        return applyDeadzone(hid.getRightY(), deadZoneSize);
    }

    @Override
    public double getRightTriggerAxis() {
        return applyDeadzone(hid.getRightTriggerAxis(), deadZoneSize);
    }

    @Override
    public double getLeftTriggerAxis() {
        return applyDeadzone(hid.getLeftTriggerAxis(), deadZoneSize);
    }

    public static double applyDeadzone(double input, double deadZoneSize) {
        if (Math.abs(input) < deadZoneSize) { // return 0 if within the deadzone
            return 0.0;
        }
        return (input - Math.signum(input) * deadZoneSize) / (1 - deadZoneSize); // linear between 0 and 1 in the remaining range
    }
}
