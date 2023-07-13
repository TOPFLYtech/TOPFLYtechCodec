package com.topflytech.codec.entities;
 

/**
 * The type Sign in message.Protocol number is 25 25 01.
 * Older devices like 8806,8803Pro,You need to respond to the message to the device, otherwise the device will not send other data.
 * The new device, like the 8806 plus, needs to be based on the device configuration to decide whether or not to respond to the message
 */
public class SignInMessage extends Message{


    /**
     * Gets software version.like 1.1.1
     *
     * @return the software
     */
    public String getSoftware() {
        return software;
    }

    /**
     * Sets software.
     *
     * @param software the software
     */
    public void setSoftware(String software) {
        this.software = software;
    }

    /**
     * Gets firmware version.like 1.1
     *
     * @return the firmware
     */
    public String getFirmware() {
        return firmware;
    }

    /**
     * Sets firmware.
     *
     * @param firmware the firmware
     */
    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    /**
     * Gets hardware version.like 5.0
     *
     * @return the hardware
     */
    public String getHardware() {
        return hardware;
    }

    /**
     * Sets hardware.
     *
     * @param hardware the hardware
     */
    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    /**
     * Gets platform. like 6250
     *
     * @return the platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Sets platform.
     *
     * @param platform the platform
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }




    private String software;
    private String firmware;
    private String hardware;
    private String platform;

    /**
     * Gets obd software version.like 1.1.1
     *
     * @return the obd software
     */
    public String getObdSoftware() {
        return obdSoftware;
    }
    /**
     * Sets obd software.
     *
     * @param obdSoftware the obd software
     */
    public void setObdSoftware(String obdSoftware) {
        this.obdSoftware = obdSoftware;
    }




    /**
     * Gets obd hardware version.like 5.0
     *
     * @return the obd hardware
     */
    public String getObdHardware() {
        return obdHardware;
    }
    /**
     * Sets obd hardware.
     *
     * @param obdHardware the obd hardware
     */
    public void setObdHardware(String obdHardware) {
        this.obdHardware = obdHardware;
    }




    private String obdSoftware;
    private String obdHardware;
    private String obdBootVersion;
    private String obdDataVersion;
    private int model;

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;
    }

    public String getObdBootVersion() {
        return obdBootVersion;
    }

    public void setObdBootVersion(String obdBootVersion) {
        this.obdBootVersion = obdBootVersion;
    }

    public String getObdDataVersion() {
        return obdDataVersion;
    }

    public void setObdDataVersion(String obdDataVersion) {
        this.obdDataVersion = obdDataVersion;
    }
}
