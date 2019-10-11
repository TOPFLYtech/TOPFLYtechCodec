package com.topflytech.codec.entities;

/**
 * .Driver Behavior Via Acceleration (AST Command Control, Default Disable the feature).Protocol number is 25 25 06.
 */
public class AccelerationDriverBehaviorMessage extends Message{


    /**
     * Gets acceleration data.
     *
     * @return the acceleration data
     */
    public AccelerationData getAccelerationData() {
        return accelerationData;
    }


    /**
     * Sets acceleration data.
     *
     * @param acceleration the acceleration
     */
    public void setAccelerationData(AccelerationData acceleration) {
        this.accelerationData = acceleration;
    }



    /**
     * The Acceleration data.
     */
    protected AccelerationData accelerationData;

    /**
     * Gets behavior type.
     *
     * @return the behavior type
     */
    public int getBehaviorType() {
        return behaviorType;
    }

    /**
     * Sets behavior type.
     *
     * @param behaviorType the behavior type
     */
    public void setBehaviorType(int behaviorType) {
        this.behaviorType = behaviorType;
    }




    private int behaviorType;
    /**
     * The constant BEHAVIOR_TURN_AND_BRAKE.
     */
    public final static int BEHAVIOR_TURN_AND_BRAKE = 0;
    /**
     * The constant BEHAVIOR_ACCELERATE.
     */
    public final static int BEHAVIOR_ACCELERATE = 1;




}
