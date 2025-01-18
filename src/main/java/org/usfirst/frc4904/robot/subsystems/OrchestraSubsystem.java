package org.usfirst.frc4904.robot.subsystems;

import com.ctre.phoenix6.Orchestra;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.usfirst.frc4904.standard.custom.motorcontrollers.CANTalonFX;
/** Orchestra™ */
public class OrchestraSubsystem extends SubsystemBase {

    Orchestra orchestra = new Orchestra();
    public CANTalonFX motor;

    public Command c_playSound(String file, CANTalonFX motor1, CANTalonFX motor2) {
        orchestra.addInstrument(motor1);
        orchestra.addInstrument(motor2);
        orchestra.loadMusic(file);
        return this.run(() -> orchestra.play());
    }
}
