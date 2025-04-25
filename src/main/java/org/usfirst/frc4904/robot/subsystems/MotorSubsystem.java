package org.usfirst.frc4904.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import org.usfirst.frc4904.standard.custom.motorcontrollers.SmartMotorController;

import java.util.stream.DoubleStream;

public class MotorSubsystem extends SubsystemBase {

    public final SmartMotorController[] motors;
    public final double[] relativeVoltages;

    public final double forwardVoltage;
    public final double backwardVoltage;

    /**
     * Control multiple motors with one subsystem. This constructor only controls one motor.
     *
     * @param motor the motor that the subsystem controls
     * @param voltage voltage when motors are running
     */
    public MotorSubsystem(SmartMotorController motor, double voltage) {
        this(new SmartMotorController[] { motor }, voltage, voltage);
    }

    /**
     * Control multiple motors with one subsystem. This constructor only controls one motor.
     *
     * @param motor the motor that the subsystem controls
     * @param forwardVoltage voltage when the motor is running forwards
     * @param backwardVoltage voltage when the motor is running backwards - should be POSITIVE (is negated later)
     */
    public MotorSubsystem(SmartMotorController motor, double forwardVoltage, double backwardVoltage) {
        this(new SmartMotorController[] { motor }, forwardVoltage, backwardVoltage);
    }

    /**
     * Control multiple motors with one subsystem. For example, to have two motors, use:
     * <pre>{@code
     *     new MultiMotorSubsystem(
     *         new CANTalonFX[] { motor1, motor2 },
     *         voltage
     *     )
     * }</pre>
     *
     * @param motors the motors that the subsystem controls
     * @param voltage voltage when motors are running
     */
    public MotorSubsystem(SmartMotorController[] motors, double voltage) {
        this(motors, voltage, voltage);
    }

    /**
     * Control multiple motors with one subsystem. For example, to have two motors, use:
     * <pre>{@code
     *     new MultiMotorSubsystem(
     *         new CANTalonFX[] { motor1, motor2 },
     *         forwardVoltage,
     *         backwardVoltage
     *     )
     * }</pre>
     *
     * @param motors the motors that the subsystem controls
     * @param forwardVoltage voltage when motors are running forwards
     * @param backwardVoltage voltage when motors are running backwards - should be POSITIVE (is negated later)
     */
    public MotorSubsystem(SmartMotorController[] motors, double forwardVoltage, double backwardVoltage) {
        this(
            motors,
            DoubleStream.generate(() -> 1).limit(motors.length).toArray(),
            forwardVoltage,
            backwardVoltage
        );
    }

    /**
     * Control multiple motors with one subsystem. For example, to have two motors where the second one is inverted, use:
     * <pre>{@code
     *     new MultiMotorSubsystem(
     *         new CANTalonFX[] { motor1, motor2 },
     *         new int[] { 1, -1 },
     *         voltage
     *     )
     * }</pre>
     *
     * @param motors the motors that the subsystem controls
     * @param relativeVoltages multiplied by forward/backwardVoltage for each motor - use to invert some motors, for example
     * @param voltage voltage when motors are running
     */
    public MotorSubsystem(SmartMotorController[] motors, double[] relativeVoltages, double voltage) {
        this(motors, relativeVoltages, voltage, voltage);
    }

    /**
     * Control multiple motors with one subsystem. For example, to have two motors where the second one is inverted, use:
     * <pre>{@code
     *     new MultiMotorSubsystem(
     *         new CANTalonFX[] { motor1, motor2 },
     *         new int[] { 1, -1 },
     *         forwardVoltage,
     *         backwardVoltage
     *     )
     * }</pre>
     *
     * @param motors the motors that the subsystem controls
     * @param relativeVoltages multiplied by forward/backwardVoltage for each motor - use to invert some motors, for example
     * @param forwardVoltage voltage when motors are running forwards
     * @param backwardVoltage voltage when motors are running backwards - should be POSITIVE (is negated later)
     */
    public MotorSubsystem(
        SmartMotorController[] motors,
        double[] relativeVoltages,
        double forwardVoltage,
        double backwardVoltage
    ) {
        this.motors = motors;
        this.relativeVoltages = relativeVoltages;
        this.forwardVoltage = forwardVoltage;
        this.backwardVoltage = backwardVoltage;

        for (SmartMotorController motor : motors) {
            motor.setBrakeOnNeutral();
        }
    }

    public void setVoltage(double voltage) {
        for (int i = 0; i < motors.length; i++) {
            motors[i].setVoltage(voltage * relativeVoltages[i]);
        }
    }

    public void stop() {
        for (var motor : motors) motor.setVoltage(0);
    }

    public void setBrakeOnNeutral() {
        for (var motor : motors) motor.setBrakeOnNeutral();
    }

    public void setCoastOnNeutral() {
        for (var motor : motors) motor.setCoastOnNeutral();
    }

    public void brake() {
        setBrakeOnNeutral();
        stop();
    }

    public void coast() {
        setCoastOnNeutral();
        stop();
    }

    public Command c_holdVoltage(double voltage) {
        return this.run(() -> setVoltage(voltage));
    }

    public Command c_forward() {
        return c_holdVoltage(forwardVoltage);
    }

    public Command c_backward() {
        return c_holdVoltage(-backwardVoltage);
    }

    public Command c_stop() {
        return this.runOnce(this::stop);
    }

    public Command c_brake() {
        return this.runOnce(this::brake);
    }

    public Command c_coast() {
        return this.runOnce(this::coast);
    }
}
