package com.topflytech.codec.entities;

import java.util.List;


/**
 * The type Accident acceleration message.Accident Data (AST Command Control, Default Disable the feature,From Tracker)
 * Protocol number is 25 25 07.
 */
public class AccidentAccelerationMessage extends Message {

    /**
     * Gets acceleration list.
     *
     * @return the acceleration list
     */
    public List<AccelerationData> getAccelerationList() {
        return accelerationList;
    }

    /**
     * Sets acceleration list.
     *
     * @param accelerationList the acceleration list
     */
    public void setAccelerationList(List<AccelerationData> accelerationList) {
        this.accelerationList = accelerationList;
    }



    /**
     * The Acceleration list.
     */
    protected List<AccelerationData> accelerationList;

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
