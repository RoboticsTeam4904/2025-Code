package org.usfirst.frc4904.robot.subsystems;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import org.photonvision.PhotonCamera;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;
import org.usfirst.frc4904.standard.Util;
import swervelib.SwerveDrive;

/** Sponsored by Claude™ 3.7 Sonnet by Anthropic® */
public class VisionSubsystem extends SubsystemBase {
    private final SwerveDrive swerveDrive;
    private final PhotonCamera photonCamera;

    // PID controllers for X, Y positions and rotation
    private final PIDController positionController;
    private final PIDController rotationController;

    // Target pose relative to the AprilTag
    private Transform2d targetPoseRelative;

    // Timeout for positioning
    private final double TIMEOUT_SECONDS = 3.0; // give up if we lose sight of the april tag for this long
    private double startTime = 0;
    private boolean isPositioning = false;

    // Tolerance thresholds for positioning
    private final double POS_TOLERANCE_METERS = 0.05; // 5cm
    private final double ROT_TOLERANCE_DEG = 2.0; // 2 degrees

    // Camera mounting position relative to robot center
    private final Transform2d cameraToRobot;

    /**
     * Creates a new AprilTagPositioningSubsystem
     *
     * @param swerveDrive The YAGSL swerve drive subsystem
     * @param photonCamera The PhotonVision camera
     * @param cameraToRobot The transform from the camera to the robot center
     */
    public VisionSubsystem(SwerveDrive swerveDrive, PhotonCamera photonCamera, Transform2d cameraToRobot) {
        this.swerveDrive = swerveDrive;
        this.photonCamera = photonCamera;
        this.cameraToRobot = cameraToRobot;

        // Initialize PID controllers
        // TODO tune maybe
        positionController = new PIDController(1.0, 0.0, 0.0);
        rotationController = new PIDController(1.0, 0.0, 0.0);

        // Make rotation controller continuous
        rotationController.enableContinuousInput(-Math.PI, Math.PI);

        // Set tolerances
        positionController.setTolerance(POS_TOLERANCE_METERS);
        rotationController.setTolerance(Math.toRadians(ROT_TOLERANCE_DEG));

        // Default target is 0,0,0 relative to AprilTag
        targetPoseRelative = new Transform2d(0, 0, new Rotation2d(0));
    }

    @Override
    public void periodic() {
        if (isPositioning) {
            // Get the latest result from PhotonVision
            PhotonPipelineResult result = photonCamera.getLatestResult();

            if (result.hasTargets()) {
                // Get the best target (usually the one with highest area or lowest ambiguity)
                PhotonTrackedTarget target = result.getBestTarget();

                // Get the transform from the camera to the target
                Transform3d cameraToTarget = target.getBestCameraToTarget();

                // Calculate the current position error relative to our desired position
                Transform2d currentToDesired = calculatePositionError(
                    new Transform2d(
                        cameraToTarget.getX(),
                        cameraToTarget.getY(),
                        cameraToTarget.getRotation().toRotation2d()
                    )
                );

                // Use PID to calculate the needed speeds for X, Y, and rotation
                double xSpeed = positionController.calculate(0, currentToDesired.getX());
                double ySpeed = positionController.calculate(0, currentToDesired.getY());
                double rotSpeed = rotationController.calculate(0, currentToDesired.getRotation().getRadians());

                // Apply deadbands and clamp values for safety
                xSpeed = applyDeadband(xSpeed, 0.05);
                ySpeed = applyDeadband(ySpeed, 0.05);
                rotSpeed = applyDeadband(rotSpeed, 0.05);

                // Max speeds (adjust based on your robot)
                double maxLinearSpeed = 2.0;  // meters per second
                double maxRotSpeed = Math.PI; // radians per second

                xSpeed = Util.clamp(xSpeed, -maxLinearSpeed, maxLinearSpeed);
                ySpeed = Util.clamp(ySpeed, -maxLinearSpeed, maxLinearSpeed);
                rotSpeed = Util.clamp(rotSpeed, -maxRotSpeed, maxRotSpeed);

                // Convert to field-relative speeds
                ChassisSpeeds fieldRelativeSpeeds = ChassisSpeeds.fromFieldRelativeSpeeds(
                    xSpeed,
                    ySpeed,
                    rotSpeed,
                    swerveDrive.getOdometryHeading()
                );

                // Command the swerve drive
                swerveDrive.drive(fieldRelativeSpeeds);

                // Update SmartDashboard with positioning data
                SmartDashboard.putNumber("Position Error X", currentToDesired.getX());
                SmartDashboard.putNumber("Position Error Y", currentToDesired.getY());
                SmartDashboard.putNumber("Rotation Error (deg)", Math.toDegrees(currentToDesired.getRotation().getRadians()));

                // Check if we've reached the target position
                boolean atPosition =
                    Math.abs(currentToDesired.getX()) < POS_TOLERANCE_METERS &&
                    Math.abs(currentToDesired.getY()) < POS_TOLERANCE_METERS &&
                    Math.abs(currentToDesired.getRotation().getDegrees()) < ROT_TOLERANCE_DEG;

                // Check if we've timed out
                boolean timedOut = Timer.getFPGATimestamp() - startTime > TIMEOUT_SECONDS;

                if (atPosition || timedOut) {
                    stopPositioning();
                }
            } else {
                // No AprilTags visible, stop movement
                swerveDrive.drive(new ChassisSpeeds(0, 0, 0));

                // If we've lost sight of the AprilTag for too long, stop positioning
                if (Timer.getFPGATimestamp() - startTime > TIMEOUT_SECONDS) {
                    stopPositioning();
                    SmartDashboard.putString("Positioning Status", "Failed - No AprilTag visible");
                }
            }
        }
    }

    /**
     * Start positioning to a target pose relative to an AprilTag
     *
     * @param targetPoseRelative The target pose relative to the AprilTag
     */
    public void startPositioning(Transform2d targetPoseRelative) {
        this.targetPoseRelative = targetPoseRelative;
        this.startTime = Timer.getFPGATimestamp();
        this.isPositioning = true;

        // Reset PID controllers
        positionController.reset();
        rotationController.reset();

        SmartDashboard.putString("Positioning Status", "In Progress");
    }

    /**
     * Stop the positioning process
     */
    public void stopPositioning() {
        isPositioning = false;
        swerveDrive.drive(new ChassisSpeeds(0, 0, 0));

        SmartDashboard.putString("Positioning Status", "Completed");
    }

    /**
     * Check if the subsystem is currently attempting to position
     *
     * @return true if positioning is in progress
     */
    public boolean isPositioning() {
        return isPositioning;
    }

    /**
     * Calculate the position error between current position and desired position
     *
     * @param cameraToTarget The transform from camera to AprilTag
     * @return The transform from current position to desired position
     */
    private Transform2d calculatePositionError(Transform2d cameraToTarget) {
        // Calculate the transform from robot to AprilTag
        Transform2d robotToTarget = new Transform2d(
            cameraToTarget.getTranslation().plus(cameraToRobot.getTranslation().rotateBy(cameraToTarget.getRotation())),
            cameraToTarget.getRotation().plus(cameraToRobot.getRotation())
        );

        // Calculate the desired robot to target transform
        Transform2d desiredRobotToTarget = targetPoseRelative;

        // Calculate error (difference between current and desired)
        Translation2d translationError = desiredRobotToTarget.getTranslation().minus(robotToTarget.getTranslation());
        Rotation2d rotationError = desiredRobotToTarget.getRotation().minus(robotToTarget.getRotation());

        return new Transform2d(translationError, rotationError);
    }

    /**
     * Apply a deadband to a value
     *
     * @param value The input value
     * @param deadband The deadband range
     * @return The value with deadband applied
     */
    private double applyDeadband(double value, double deadband) {
        if (Math.abs(value) < deadband) {
            return 0.0;
        }
        return value;
    }
}
