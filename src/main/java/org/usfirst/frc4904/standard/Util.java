package org.usfirst.frc4904.standard;

import edu.wpi.first.hal.util.BoundaryException;

import java.util.concurrent.TimeUnit;

/**
 * Common utilities
 */
public class Util {
    /**
     * Returns true if {@code value} is less than {@code epsilon}. This is useful
     * for floating point numbers, whose arithmetic operations tend to introduce
     * small errors.
     *
     *
     * @param value   The floating point number to be compared
     * @param epsilon The maximum magnitude of var such that it can be considered
     *                zero
     * @return Whether {@code value} is less than {@code epsilon}
     */
    public static boolean isZero(double value, double epsilon) {
        return Math.abs(value) < epsilon;
    }

    /**
     * Returns true if {@code value} is less than {@code epsilon}. This is useful
     * for floating point numbers, whose arithmetic operations tend to introduce
     * small errors.
     *
     * @param value The floating point number to be compared
     * @return Whether {@code value} is effectively zero
     */
    public static boolean isZero(double value) {
        return isZero(value, Math.sqrt(Math.ulp(1.0)));
    }

    // TODO upgrade java version and use Math.clamp
    /**
     * Clamp a value between a minimum and maximum
     *
     * @param value The input value
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     * @return The clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamp a value between a minimum and maximum
     *
     * @param value The input value
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     * @return The clamped value
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Range(double min, double max) {
        public Range {
            if (min > max) {
                throw new BoundaryException("Range min " + min + " greater than max " + max);
            }
        }

        public double getRange() {
            return max - min;
        }

        public boolean contains(double value) {
            return value >= min && value <= max;
        }

        public double getCenter() {
            return (min + max) / 2.0;
        }

        /**
         * Scales a value (between -1 and 1) to the range. Example: (new Range(0,6)).scaleValue(0.5) == 4.5
         *
         * @param value between -1 and 1 (will be limited to [-1, 1])
         * @return the scaled value
         */
        public double scaleValue(double value) {
            return limitValue(getCenter() + value * (getRange() / 2.0));
        }

        /**
         * Limits a value to the range. Example: (new Range(0,6)).limitValue(7) == 6
         *
         * @param value the value to be limited
         * @return the limited value
         */
        public double limitValue(double value) {
            return Math.max(Math.min(value, max), min);
        }
    }

    /**
     * Computes the conversion factor between the first and second {@link TimeUnit}
     * given.
     *
     * @param from the source unit
     * @param to   the target unit
     * @return the conversion factor
     */
    public static double timeConversionFactor(TimeUnit from, TimeUnit to) {
        // TimeUnit.convert returns a long.
        // If from >= to (in terms of units), we can simply use normal conversion, as it
        // will scale up.
        // Otherwise, invert the conversion and take the reciprocal.

        if (from.compareTo(to) >= 0) {
            return (double) to.convert(1, from);
        } else {
            return 1.0 / from.convert(1, to);
        }
    }
}
