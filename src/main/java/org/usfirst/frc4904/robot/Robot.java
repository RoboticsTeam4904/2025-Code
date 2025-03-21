/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved. */
/* Open Source Software - may be modified and shared by FRC teams. The code */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project. */
/*----------------------------------------------------------------------------*/
package org.usfirst.frc4904.robot;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;
// import com.ctre.phoenix6.signals.NeutralModeValue;
// import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.usfirst.frc4904.robot.RobotMap.Component;
import org.usfirst.frc4904.robot.humaninterface.drivers.SwerveGain;
import org.usfirst.frc4904.robot.humaninterface.operators.DefaultOperator;
import org.usfirst.frc4904.standard.CommandRobotBase;
import org.usfirst.frc4904.standard.custom.controllers.CustomCommandJoystick;
import org.usfirst.frc4904.standard.humaninput.Driver;

import java.util.function.Supplier;

public class Robot extends CommandRobotBase {

    public static class AutonConfig {

        /** Whether to run auton at all */
        public static final boolean ENABLED = true;

        /** Whether to flip the path to the other side of the current alliance's field */
        public static final boolean FLIP_SIDE = false;

        /** The auton to run */
        public static Supplier<Command> COMMAND = Auton::c_straightCoral;
    }

    private final Driver driver = new SwerveGain();
    private final DefaultOperator operator = new DefaultOperator();
    private final RobotMap map = new RobotMap();

    protected double scaleGain(double input, double gain, double exp) {
        return Math.pow(Math.abs(input), exp) * gain * Math.signum(input);
    }

    public Robot() {
        super();
        //can set default auton command here
    }

    @Override
    public void initialize() {}

    @Override
    public void teleopInitialize() {
        driver.bindCommands();
        operator.bindCommands();
        //Component.elevator.encoder.reset();

        Component.chassis.setDefaultCommand(
            Component.chassis.driveCommand(driver::getY, driver::getX, driver::getTurnSpeed)
        );
    }

    @Override
    public void teleopExecute() {
        Component.vision.periodic();

        double y = RobotMap.HumanInput.Operator.joystick.getY();
        System.out.println(Math.abs(y));

        if (Math.abs(y) >= 0.1) {
            var currentElevatorCommand = CommandScheduler.getInstance().requiring(Component.elevator);
            if (currentElevatorCommand != null) currentElevatorCommand.cancel();

            Component.elevator.setVoltage(Math.pow(y, 2) * Math.signum(y) * 10.0);
        }
    }

    Timer timer = new Timer();

    @Override
    public void autonomousInitialize() {
        if (!AutonConfig.ENABLED) return;

        Component.navx.reset();

        timer.reset();
        timer.start();

        // AutonConfig.COMMAND.get().schedule();
    }

    @Override
    public void autonomousExecute() {
        if (timer.get() < 2.0) {
            Component.chassis.drive(               
                ChassisSpeeds.fromRobotRelativeSpeeds(
                    -3.0,
                    0.0,
                    0.0,
                    Rotation2d.kZero
                )
            );
        }

        // RobotMap.Component.vision.periodic();

        // logging can go here
    }

    @Override
    public void disabledInitialize() {
        Component.vision.stopPositioning("Robot disabled");

    //     Component.elevatorMotorOne.setBrakeOnNeutral();
    //     Component.elevatorMotorTwo.setBrakeOnNeutral();
     }

    @Override
    public void disabledExecute() {}

    @Override
    public void testInitialize() {
        //do things like setting neutral or brake mode on the mechanism or wheels here
        // Component.elevatorMotorOne.setCoastOnNeutral();
        // Component.elevatorMotorTwo.setCoastOnNeutral();
    }

    @Override
    public void testExecute() {

    }

    @Override
    public void alwaysExecute() {
        // logging stuff can go here
        // if (Component.elevator != null) {
        //     // System.out.println("ELEVATOR ENCODER: " + Component.elevatorEncoder.get());
        // }
    }
}
