package org.usfirst.frc4904.standard;


import org.usfirst.frc4904.standard.custom.CommandSendableChooser;
import org.usfirst.frc4904.standard.custom.TypedNamedSendableChooser;
import org.usfirst.frc4904.standard.humaninput.Driver;
import org.usfirst.frc4904.standard.humaninput.Operator;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * IterativeRobot is normally the base class for command based code, but we
 * think certain features will almost always be needed, so we created the
 * CommandRobotBase class. Robot should extend this instead of iterative robot.
 */
public abstract class CommandRobotBase extends TimedRobot {

	private Command autonomousCommand;
	protected Command teleopCommand;
	protected CommandSendableChooser autoChooser;
	protected TypedNamedSendableChooser<Driver> driverChooser;
	protected TypedNamedSendableChooser<Operator> operatorChooser;

	/**
	 * This displays our choosers. The default choosers are for autonomous type,
	 * driver control, sand operator control.
	 */
	protected final void displayChoosers() {
		SmartDashboard.putData("Auton Routine Selector", autoChooser);
		SmartDashboard.putData("Driver control scheme chooser", driverChooser);
		SmartDashboard.putData("Operator control scheme chooser", operatorChooser);
	}

	/**
	 * This stops all commands from running on initialization of a state so as to
	 * prevent commands from the previous state from interfering with the current
	 * robot mode.
	 */
	private void cleanup() {
		if (autonomousCommand != null) {
			autonomousCommand.cancel();
		}
		if (teleopCommand != null) {
			teleopCommand.cancel();
		}
	}

	// HACK FIXME, incredibly cursed and potentially bad
	public static Driver drivingConfig = new Driver("uhohhh") {
		@Override
		public double getX() {
			for (int i=0; i<1000000; i++) System.err.println("DRIVER NOT CONFIGED");
			return 0;
		}

		@Override
		public double getY() {return 0;}

		@Override
		public double getTurnSpeed() {return 0;}

		@Override
		public void bindCommands() {}
	};
	
	/**
	 * This initializes the entire robot. It is called by WPILib on robot code
	 * launch. Year-specific code should be written in the initialize function.
	 */
	@Override
	public final void robotInit() {
		// Initialize choosers
		autoChooser = new CommandSendableChooser();
		driverChooser = new TypedNamedSendableChooser<Driver>();
		operatorChooser = new TypedNamedSendableChooser<Operator>();
		// Run user-provided initialize function
		initialize();
		// Display choosers on SmartDashboard
		displayChoosers();
	}

	/**
	 * Function for year-specific code to be run on robot code launch.
	 * setHealthChecks should be called here if needed.
	 */
	public abstract void initialize();

	/**
	 * This initializes the teleoperated portion of the robot code. It is called by
	 * WPILib on teleop enable. Year-specific code should be written in the
	 * teleopInitialize() function.
	 */
	@Override
	public final void teleopInit() {
		cleanup();
		if (driverChooser.getSelected() != null) {
			// LogKitten.d("Loading driver " + driverChooser.getSelected().getName());
			CommandRobotBase.drivingConfig = driverChooser.getSelected();
			driverChooser.getSelected().bindCommands();
		}
		if (operatorChooser.getSelected() != null) {
			// LogKitten.d("Loading operator " + operatorChooser.getSelected().getName());
			operatorChooser.getSelected().bindCommands();
		}
		teleopInitialize();
		if (teleopCommand != null) {
			teleopCommand.schedule();
		}
	}

	/**
	 * Function for year-specific code to be run on teleoperated initialize.
	 * teleopCommand should be set in this function.
	 */
	public abstract void teleopInitialize();

	/**
	 * This function is called by WPILib periodically during teleop. Year-specific
	 * code should be written in the teleopExecute() function.
	 */
	@Override
	public final void teleopPeriodic() {
		CommandScheduler.getInstance().run();
		teleopExecute();
		alwaysExecute();
	}

	/**
	 * Function for year-specific code to be run during teleoperated time.
	 */
	public abstract void teleopExecute();

	/**
	 * This initializes the autonomous portion of the robot code. It is called by
	 * WPILib on auton enable. Year-specific code should be written in the
	 * autonomousInitialize() function.
	 */
	@Override
	public final void autonomousInit() {
		cleanup();
		autonomousCommand = autoChooser.getSelected();
		if (autonomousCommand != null) {
			autonomousCommand.schedule();
		}
		autonomousInitialize();
	}

	/**
	 * Function for year-specific code to be run on autonomous initialize.
	 */
	public abstract void autonomousInitialize();

	/**
	 * This function is called by WPILib periodically during auton. Year-specific
	 * code should be written in the autonomousExecute() function.
	 */
	@Override
	public final void autonomousPeriodic() {
		CommandScheduler.getInstance().run();
		autonomousExecute();
		alwaysExecute();
	}

	/**
	 * Function for year-specific code to be run during autonomous.
	 */
	public abstract void autonomousExecute();

	/**
	 * This function is called by WPILib when the robot is disabled. Year-specific
	 * code should be written in the disabledInitialize() function.
	 */
	@Override
	public final void disabledInit() {
		cleanup();
		disabledInitialize();
	}

	/**
	 * Function for year-specific code to be run on disabled initialize.
	 */
	public abstract void disabledInitialize();

	/**
	 * This function is called by WPILib periodically while disabled. Year-specific
	 * code should be written in the disabledExecute() function.
	 */
	@Override
	public final void disabledPeriodic() {
		CommandScheduler.getInstance().run();
		disabledExecute();
		alwaysExecute();
	}

	/**
	 * Function for year-specific code to be run while disabled.
	 */
	public abstract void disabledExecute();

	/**
	 * This function is called by WPILib when the robot is in test mode.
	 * Year-specific-code should be written in the disabledInitialize() function.
	 */
	@Override
	public final void testInit() {
		cleanup();
		testInitialize();
	}

	/**
	 * Function for year-specific code to be run on disabled initialize.
	 */
	public abstract void testInitialize();

	/**
	 * This function is called by WPILib periodically while in test mode.
	 * Year-specific code should be written in the testExecute() function.
	 */
	@Override
	public void testPeriodic() {
		CommandScheduler.getInstance().run();
		testExecute();
		alwaysExecute();
	}

	/**
	 * Function for year-specific code to be run while in test mode.
	 */
	public abstract void testExecute();

	/**
	 * Function for year-specific code to be run in every robot mode.
	 */
	public abstract void alwaysExecute();
}
