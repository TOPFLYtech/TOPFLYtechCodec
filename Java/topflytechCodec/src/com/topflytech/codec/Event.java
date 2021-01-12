package com.topflytech.codec;


/**
 * Device Alarm Type Description.
 */
public class Event {
    /**
     * The constant NONE.
     */
    public static final int NONE                           = 1;
    /**
     * ACC from 0 to 1
     */
    public static final int IGNITION                       = 2;
    /**
     * ACC from 1 to 0
     */
    public static final int PARKING                        = 3;
    /**
     * External power disconnect;
     */
    public static final int ALARM_EXTERNAL_POWER_LOST      = 4;
    /**
     *  Low power alarm(inner power voltage 3.5V)
     */
    public static final int ALARM_LOW_BATTERY              = 5;
    /**
     * SOS alarm
     */
    public static final int ALARM_SOS                      = 6;
    /**
     * Over speed alarm
     */
    public static final int ALARM_OVER_SPEED               = 7;
    /**
     * Drag alarm when set
     */
    public static final int ALARM_TOWING                   = 8;
    /**
     * Device apply address
     */
    public static final int ADDRESS_REQUESTED              = 9;
    /**
     * Anti-theft alarm
     */
    public static final int ALARM_ANTI_THEFT               = 10;
    /**
     * Ananlog 1 voltage increase
     */
    public static final int FILL_TANK                      = 11;
    /**
     * Analog 1 voltage decrease
     */
    public static final int ALARM_FUEL_LEAK                = 12;
    /**
     * In Geofence alarm
     */
    public static final int ALARM_GEOFENCE_OUT             = 13;
    /**
     * Out Geofence alarm
     */
    public static final int ALARM_GEOFENCE_IN              = 14;

    /**
     * Air conditioning opens alarm
     */
    public static final int AC_ON                          = 15;
    /**
     * Air conditioning off alarm
     */
    public static final int AC_OFF                         = 16;
    /**
     * One time idle start, you can define the idle timing
     */
    public static final int IDLE_START                     = 17;
    /**
     * One time idle end
     */
    public static final int IDLE_END                       = 18;

    /**
     * Vibration alarm
     */
    public static final int ALARM_VIBRATION                = 19;

    /**
     * GSM jammer detection start ,this need config
     */
    public static final int GSM_JAMMER_DETECTION_START    = 20;
    /**
     * GSM jammer detection end
     */
    public static final int GSM_JAMMER_DETECTION_END    = 21;
    /**
     * External power recover
     */
    public static final int ALARM_EXTERNAL_POWER_RECOVER = 22;
    /**
     *  External power lower than preset external power , this need config
     */
    public static final int ALARM_EXTERNAL_POWER_LOWER = 23;
    /**
     * Rude driver alert, this need config
     */
    public static final int ALARM_RUDE_DRIVER = 24;
    /**
     * Collision alert, this need config
     */
    public static final int ALARM_COLLISION = 25;
    /**
     * Turn over alert, this need config
     */
    public static final int ALARM_TURN_OVER = 26;
    public static final int ALARM_TIRE_LEAKS = 27;
    public static final int ALARM_TIRE_BLE_POWER_LOWER = 28;
    public static final int ALARM_TRACKER_POWER_LOWER = 29;
    public static final int ALARM_DEVICE_REMOVE = 30;
    public static final int ALARM_DEVICE_CASE_OPEN = 31;
    public static final int ALARM_BOX_OPEN = 32;
    public static final int ALARM_FALL_DOWN = 33;
    public static final int ALARM_BATTERY_POWER_RECOVER = 34;
    public static final int ALARM_INNER_TEMP_HIGH = 35;
    public static final int ALARM_MOVE = 36;
    public static final int ALARM_INCLINE = 37;
    public static final int ALARM_USB_RECHARGE_START = 38;
    public static final int ALARM_USB_RECHARGE_END = 39;
    public static final int ALARM_DEVICE_MOUNTED = 40;
    public static final int ALARM_DEVICE_CASE_CLOSED = 41;
    public static final int ALARM_BOX_CLOSED = 42;
    public static final int ALARM_FALL_DOWN_REC = 43;
    public static final int ALARM_INNER_TEMP_HIGH_REC = 44;
    public static final int ALARM_MOVE_REC = 45;
    public static final int ALARM_COLLISION_REC = 46;
    public static final int ALARM_INCLINE_REC = 47;
    public static final int ALARM_POWER_ON = 48;
    public static final int ALARM_INNER_TEMP_LOW = 49;
    public static final int ALARM_INNER_TEMP_LOW_REC = 50;
    /**
     * Gets event.
     *
     * @param eventByte the event byte
     * @return the event
     */
    public static int getEvent(byte eventByte) {
        switch (eventByte) {
            case 0x01:
                return Event.ALARM_EXTERNAL_POWER_LOST;
            case 0x02:
                return Event.ALARM_LOW_BATTERY;
            case 0x03:
                return Event.ALARM_SOS;
            case 0x04:
                return Event.ALARM_OVER_SPEED;
            case 0x05:
                return Event.ALARM_GEOFENCE_IN;
            case 0x06:
                return Event.ALARM_GEOFENCE_OUT;
            case 0x07:
                return Event.ALARM_TOWING;
            case 0x08:
                return Event.ALARM_VIBRATION;
            case 0x09:
                return Event.ADDRESS_REQUESTED;
            case 0x10:
                return Event.ALARM_ANTI_THEFT;
            case 0x11:
            case 0x13:
                return Event.FILL_TANK;
            case 0x12:
            case 0x14:
                return Event.ALARM_FUEL_LEAK;
            case 0x15:
                return Event.IGNITION;
            case 0x16:
                return Event.PARKING;
            case 0x17:
                return Event.AC_ON;
            case 0x18:
                return Event.AC_OFF;
            case 0x19:
                return Event.IDLE_START;
            case 0x20:
                return Event.IDLE_END;
            case 0x21:
                return Event.GSM_JAMMER_DETECTION_START;
            case 0x22:
                return Event.GSM_JAMMER_DETECTION_END;
            case 0x23:
                return Event.ALARM_EXTERNAL_POWER_RECOVER;
            case 0x24:
                return Event.ALARM_EXTERNAL_POWER_LOWER;
            case 0x25:
                return Event.ALARM_RUDE_DRIVER;
            case 0x26:
                return Event.ALARM_COLLISION;
            case 0x27:
                return Event.ALARM_TURN_OVER;
            default:
                return Event.NONE;
        }
    }

}