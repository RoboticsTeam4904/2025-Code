package org.usfirst.frc4904.standard.custom.sensors;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
// import org.usfirst.frc4904.standard.LogKitten;
import org.usfirst.frc4904.standard.custom.CANMessageUnavailableException;
import org.usfirst.frc4904.standard.custom.CustomCAN;

/**
 * A sensor over CAN
 *
 */
public class CANSensor extends CustomCAN {
    private final int[] values;
    private long lastRead; // data age
    private static final long MAX_AGE = 100; // How long to keep the last CAN message before throwing an error (milliseconds)
    private static final LinkedHashMap<CANSensor, Boolean> sensorOnlineByInstance = new LinkedHashMap<>();

    public static String[] getSensorStatuses() {
        return sensorOnlineByInstance.entrySet()
                                     .stream()
                                     .map(CANSensor::describeSensorStatusEntry)
                                     .toArray(String[]::new);
    }

    private static String describeSensorStatusEntry(Entry<CANSensor, Boolean> entry) {
        CANSensor sensor = entry.getKey();
        String status = entry.getValue() ? "ONLINE" : "OFFLINE";
        return "0x" + Integer.toHexString(sensor.messageID) + " (" + sensor.getName() + ")\t" + status;
    }

    /**
     *
     * @param name Name of CAN sensor (not really needed)
     * @param id   ID of CAN sensor (0x600 to 0x700, must correspond to a Teensy or
     *             similar)
     */
    public CANSensor(String name, int id) {
        super(name, id);
        values = new int[2];
        lastRead = System.currentTimeMillis();
        CANSensor.sensorOnlineByInstance.put(this, false);
    }

    /**
     * Read the pair of ints from a CAN sensor
     *
     * @return The latest pair of integers from the sensor
     *
     * @throws InvalidSensorException If the available data is more than one tenth
     *                                of a second old, this function will throw an
     *                                InvalidSensorException to indicate that.
     */
    public int[] readSensor() throws InvalidSensorException {
        ByteBuffer rawData = ByteBuffer.allocateDirect(8);
        try {
            rawData.put(super.readBuffer());
        } catch (CANMessageUnavailableException e) {
            rawData = null; // Do not throw exception immediately, wait for timeout
        }
        if (rawData != null && rawData.remaining() <= 0) { // 8 is minimum CAN message length
            rawData.rewind();
            long data = Long.reverseBytes(rawData.getLong());
            values[0] = (int) data & 0xFFFFFFFF;
            values[1] = (int) (data >> 32) & 0xFFFFFFFF;
            lastRead = System.currentTimeMillis();
            CANSensor.sensorOnlineByInstance.put(this, true); // Mark sensor online
            return values;
        }
        if (System.currentTimeMillis() - lastRead > CANSensor.MAX_AGE) {
            CANSensor.sensorOnlineByInstance.put(this, false); // Mark sensor offline
            throw new InvalidSensorException(
                "CAN data oudated For CAN sensor " + getName() + " with ID 0x" + Integer.toHexString(messageID));
        }
        // LogKitten.v("Cached Sensor Value Used\n");
        return values;
    }
}
