package org.usfirst.frc4904.robot.subsystems;

import edu.wpi.first.math.Pair;
import edu.wpi.first.math.controller.ElevatorFeedforward;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.*;

import java.util.HashMap;
import java.util.function.DoubleSupplier;

import org.usfirst.frc4904.robot.RobotMap.Component;
import org.usfirst.frc4904.standard.commands.CreateOnInitialize;
import org.usfirst.frc4904.standard.commands.NoOp;
import org.usfirst.frc4904.standard.custom.CustomEncoder;
import org.usfirst.frc4904.standard.custom.motioncontrollers.ezControl;
import org.usfirst.frc4904.standard.custom.motioncontrollers.ezMotion;
import org.usfirst.frc4904.standard.custom.motorcontrollers.SmartMotorController;

public class ElevatorSubsystem extends MultiMotorSubsystem {

    // TODO TUNING: elevator PID
    public static final double kS = 0.1;
    public static final double kV = 2;
    public static final double kA = 0.4;
    public static final double kG = 0.3;

    public static final double kP = 0.4;
    public static final double kI = 0;
    public static final double kD = 0;

    public static final double MAX_VEL = 4;
    public static final double MAX_ACCEL = 4;

    public static final double MIN_HEIGHT = 0;
    public static final double MAX_HEIGHT = 12.5;

    public final ElevatorFeedforward feedforward;
    public final CustomEncoder encoder;

    // make sure that all values defined in this enum are added to the 'positions' map in the constructor
    public enum Position {
        INTAKE,
        L2,
        L3,
    }

    public static HashMap<Position, Double> positions = new HashMap<>();

    public ElevatorSubsystem(SmartMotorController motor1, SmartMotorController motor2, CustomEncoder encoder) {
        super(
            new SmartMotorController[] { motor1, motor2 },
            new double[] { 1, -1 },
            7
        );
        this.feedforward = new ElevatorFeedforward(kS, kG, kV, kA);
        this.encoder = encoder;

        positions.put(Position.INTAKE, 0.0);

        // TODO IMPORTANT: tune (these are EXTREMELY inaccurate right now dont even try to use them)
        positions.put(Position.L2, 5.2);
        positions.put(Position.L3, 7.5);

        for (var pos : Position.values()) {
            if (positions.get(pos) == null) {
                System.err.println(
                    "ElevatorSubsystem.Position." +
                    pos.name() +
                    " is not defined in 'positions' map"
                );
            }
        }
    }

    /**
     * @return The current height of the elevator in Magical Encoder Units™
     */
    public double getHeight() {
        return encoder.get();
    }

    /** Intake at the current elevator position */
    public Command c_intakeRaw() {
        return new SequentialCommandGroup(
            new ParallelDeadlineGroup(
                new WaitCommand(0.8),
                Component.ramp.c_forward()
            ),
            new ParallelDeadlineGroup(
                new WaitCommand(0.35),
                Component.ramp.c_forward(),
                Component.outtake.c_forward()
            ),
            new ParallelCommandGroup(
                Component.ramp.c_stop(),
                Component.outtake.c_stop()
            )
        );
    }

    /** Outtake at the current elevator position */
    public Command c_outtakeRaw() {
        return new SequentialCommandGroup(
            new ParallelDeadlineGroup(
                new WaitCommand(0.5),
                Component.outtake.c_forward()
            ),
            Component.outtake.c_stop()
        );
    }

    /** Outtake at the current elevator position */
    public Command c_rampOuttakeRaw() {
        return new SequentialCommandGroup(
            new ParallelDeadlineGroup(
                new WaitCommand(1),
                Component.outtake.c_backward(),
                Component.ramp.c_backward()
            ),
            new ParallelCommandGroup(
                Component.outtake.c_stop(),
                Component.ramp.c_stop()
            )
        );
    }

    /** Go to the intake position and then intake */
    public Command c_intake() {
        return new SequentialCommandGroup(
            c_gotoPosition(Position.INTAKE),
            new ParallelDeadlineGroup(
                c_intakeRaw(),
                c_controlVelocity(() -> 0)
            )
        );
    }

    /** Go to the intake position and then ramp outtake */
    public Command c_rampOuttake() {
        return new SequentialCommandGroup(
            c_gotoPosition(Position.INTAKE),
            new ParallelDeadlineGroup(
                c_rampOuttakeRaw(),
                c_controlVelocity(() -> 0)
            )
        );
    }

    /** Go to the specified position and then outtake */
    public Command c_outtakeAtPosition(Position pos) {
        return new SequentialCommandGroup(
            c_gotoPosition(pos),
            new ParallelDeadlineGroup(
                c_outtakeRaw(),
                c_controlVelocity(() -> 0)
            )
        );
    }

    public Command c_controlVelocity(DoubleSupplier metersPerSecDealer) {
        var cmd = this.run(() -> {
            var ff = this.feedforward.calculate(metersPerSecDealer.getAsDouble());
            SmartDashboard.putNumber("feedforward", ff);
            this.setVoltage(ff);
        });
        cmd.setName("elevator - c_controlVelocity");

        return cmd;
    }

    public Command c_gotoPosition(Position pos) {
        Double height = positions.get(pos);

        if (height == null) {
            System.err.println("Tried to go to elevator setpoint that does not exist: " + pos.toString());
            return new NoOp();
        }

        return c_gotoHeight(height);
    }

    public Command c_gotoHeight(double height) {
        return new CreateOnInitialize(() -> this.getRawHeightCommand(height));
    }

    private Command getRawHeightCommand(double height) {
        ezControl controller = new ezControl(
            kP, kI, kD,
            (position, velocityMetersPerSec) -> this.feedforward.calculate(velocityMetersPerSec),
            0.02
        );

        TrapezoidProfile profile = new TrapezoidProfile(
            new TrapezoidProfile.Constraints(MAX_VEL, MAX_ACCEL)
        );

        Command cmd = getEzMotion(
            controller,
            profile,
            new TrapezoidProfile.State(getHeight(), 0), // TODO why are we assuming the velocity is 0
            new TrapezoidProfile.State(height, 0)
        );
        cmd.setName("elevator - c_gotoHeight");
        return cmd;
    }

    private ezMotion getEzMotion(
        ezControl controller,
        TrapezoidProfile profile,
        TrapezoidProfile.State current,
        TrapezoidProfile.State goal
    ) {
        return new ezMotion(
            controller,
            this::getHeight,
            this::setVoltage,
            (double t) -> {
                TrapezoidProfile.State result = profile.calculate(t, current, goal);
                return new Pair<>(result.position, result.velocity);
            },
            this
        ) {
            @Override
            public void end(boolean interrupted) {
                setVoltage(0);
            }
        };
    }

    @Override
    public void setVoltage(double voltage) {
        setVoltage(voltage, false);
    }

    public void setVoltage(double voltage, boolean bypassSoftwareStop) {
        if (
            !bypassSoftwareStop && (
                (this.getHeight() >= MAX_HEIGHT && voltage > 0) ||
                (this.getHeight() <= MIN_HEIGHT && voltage < 0)
            )
        ) {
            voltage = 0;
        }
        super.setVoltage(voltage);
    }
}
