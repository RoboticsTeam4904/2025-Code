// This is the subsystem for the arm extension. 
// Code by Russell from 4904

package org.usfirst.frc4904.robot.subsystems;

import org.usfirst.frc4904.robot.RobotMap;
import org.usfirst.frc4904.standard.subsystems.motor.TalonMotorSubsystem;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ArmExtension extends SubsystemBase {
    
    private final TalonMotorSubsystem motor;
    private final static double EXTENSION_SPEED = 0.5;
    
    /**
     * Constructs a new ArmExtension subsystem.
     *
     * @param motor the motor controller used to extend the arm
     */
    public ArmExtension(TalonMotorSubsystem motor) {
        this.motor = motor;
    }
    
    /**
     * Returns the motor controller used to extend the arm.
     *
     * @return the motor controller used to extend the arm
     */
    public TalonMotorSubsystem getMotor() {
        return motor;
    }
}

