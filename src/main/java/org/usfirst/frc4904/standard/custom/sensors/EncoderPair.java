package org.usfirst.frc4904.standard.custom.sensors;

// import org.usfirst.frc4904.standard.LogKitten;
import org.usfirst.frc4904.standard.Util;

/**
 * Amalgamates the data of several encoders for the purpose of controlling a
 * single motion controller.
 *
 * @warning The amalgamation will be the average. Verify before using this class
 *          that all the encoders will be rotating in the same direction with
 *          the same rate (before setDistancePerTick).
 */
public class EncoderPair implements CustomEncoder {
	private final CustomEncoder[] encoders;
	private final double[] offset; // Do not reset encoders, just store an offset value
	private boolean reverseDirection;
	private double distancePerPulse;
	private final double distanceTolerance;
	private final double rateTolerance;
	protected static final double DEFAULT_DISTANCE_TOLERANCE = 10;
	protected static final double DEFAULT_RATE_TOLERANCE = 10;

	/**
	 * Amalgamates the data of two encoders for the purpose of controlling a single
	 * motion controller.
	 *
	 * @warning The amalgamation will be the average. Verify before using this class
	 *          that all the encoders will be rotating in the same direction with
	 *          the same rate (before setDistancePerTick).
	 *
	 * @param encoder1          The first encoder to amalgamate.
	 * @param encoder2          The second encoder to amalgamate.
	 * @param distanceTolerance The distance by which the encoders can be different
	 *                          before isInSync() returns false
	 * @param rateTolerance     The rate by which the encoders can be different
	 *                          before isInSync() returns false
	 */
	public EncoderPair(CustomEncoder encoder1, CustomEncoder encoder2, double distanceTolerance, double rateTolerance) {
		encoders = new CustomEncoder[] { encoder1, encoder2 };
		offset = new double[] { 0.0, 0.0 };
		this.distanceTolerance = distanceTolerance;
		this.rateTolerance = rateTolerance;
		reverseDirection = false;
	}

	/**
	 * Amalgamates the data of two encoders for the purpose of controlling a single
	 * motion controller.
	 *
	 * @warning The amalgamation will be the average. Verify before using this class
	 *          that all the encoders will be rotating in the same direction with
	 *          the same rate (before setDistancePerTick).
	 *
	 * @param encoders The encoders to amalgamate.
	 */
	public EncoderPair(CustomEncoder encoder1, CustomEncoder encoder2) {
		this(encoder1, encoder2, EncoderPair.DEFAULT_DISTANCE_TOLERANCE, EncoderPair.DEFAULT_RATE_TOLERANCE);
	}

	@Override
	public double getDistanceSafely() throws InvalidSensorException {
		return ((encoders[0].getDistanceSafely() - offset[0] * encoders[0].getDistancePerPulse())
				+ (encoders[1].getDistanceSafely() - offset[1] * encoders[1].getDistancePerPulse())) / 2.0;
	}

	@Override
	public double getDistance() {
		try {
			return getDistanceSafely();
		} catch (InvalidSensorException e) {
			e.printStackTrace();
			// LogKitten.ex(e);
			return 0;
		}
	}

	@Override
	public boolean getDirection() {
		return getRate() > 0;
	}

	@Override
	public boolean getDirectionSafely() throws InvalidSensorException {
		return getRateSafely() > 0;
	}

	@Override
	public boolean getStopped() {
		return Util.isZero(getRate());
	}

	@Override
	public boolean getStoppedSafely() throws InvalidSensorException {
		return Util.isZero(getRateSafely());
	}

	@Override
	public double getRateSafely() throws InvalidSensorException {
		return (encoders[0].getRateSafely() + encoders[1].getRateSafely()) / 2.0;
	}

	@Override
	public double getRate() {
		try {
			return getRateSafely();
		} catch (InvalidSensorException e) {
			e.printStackTrace();
			// LogKitten.ex(e);
			return 0;
		}
	}

	/**
	 * Get whether this entire encoder is inverted.
	 *
	 * @return isInverted The state of inversion, true is inverted.
	 */
	@Override
	public boolean getReverseDirection() {
		return reverseDirection;
	}

	/**
	 * Sets the direction inversion of all encoder substituents. This respects the
	 * original inversion state of each encoder when constructed, and will only
	 * invert encoders if this.getReverseDirection() != the input.
	 *
	 * @param reverseDirection The state of inversion, true is inverted.
	 */
	@Override
	public void setReverseDirection(boolean reverseDirection) {
		if (getReverseDirection() != reverseDirection) {
			encoders[0].setReverseDirection(!encoders[0].getReverseDirection());
			encoders[1].setReverseDirection(!encoders[1].getReverseDirection());
		}
		this.reverseDirection = reverseDirection;
	}

	@Override
	public double getDistancePerPulse() {
		return distancePerPulse;
	}

	@Override
	public void setDistancePerPulse(double distancePerPulse) {
		this.distancePerPulse = distancePerPulse;
		encoders[0].setDistancePerPulse(distancePerPulse);
		encoders[1].setDistancePerPulse(distancePerPulse);
	}

	@Override
	public void reset() {
		offset[0] = encoders[0].getDistance();
		offset[1] = encoders[1].getDistance();
	}

	public double getDifference() {
		try {
			return getDifferenceSafely();
		} catch (InvalidSensorException e) {
			e.printStackTrace();
			// LogKitten.ex(e);
			return 0.0; // TO DO: is this a reasonable default
		}
	}

	public double getDifferenceSafely() throws InvalidSensorException {
		return (encoders[0].getDistanceSafely() - offset[0] * encoders[0].getDistancePerPulse())
				- (encoders[1].getDistanceSafely() - offset[1] * encoders[1].getDistancePerPulse());
	}

	public double getRateDifference() {
		try {
			return getRateDifferenceSafely();
		} catch (InvalidSensorException e) {
			e.printStackTrace();
			// LogKitten.ex(e);
			return 0.0; // TO DO: is this a reasonable default
		}
	}

	public double getRateDifferenceSafely() throws InvalidSensorException {
		return encoders[0].getRateSafely() - encoders[1].getRateSafely();
	}

	public boolean isInSync() {
		try {
			return isInSyncSafely();
		} catch (InvalidSensorException e) {
			e.printStackTrace();
			// LogKitten.ex(e);
			return false; // If a sensor is broken, it is not in sync.
		}
	}

	public boolean isInSyncSafely() throws InvalidSensorException {
		return Math.abs(getDifferenceSafely()) < distanceTolerance
				&& Math.abs(getRateDifferenceSafely()) < rateTolerance;
	}
// WAS PID SOURCE EncoderDifference class here
}
