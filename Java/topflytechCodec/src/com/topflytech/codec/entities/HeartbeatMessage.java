package com.topflytech.codec.entities;
 
/**
 * The type Heartbeat message.Protocol number is 25 25 03.
 * Older devices like 8806,8803Pro,You need to respond to the message to the device, otherwise the device will reconnect to the server
 * The new device, like the 8806 plus, needs to be based on the device configuration to decide whether or not to respond to the message
 */
public class HeartbeatMessage  extends Message{
}
