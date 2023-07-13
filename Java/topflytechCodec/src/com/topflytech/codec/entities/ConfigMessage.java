package com.topflytech.codec.entities;
 

/**
 * The type Config message.When the server sends a configuration command to the device,
 * the device returns the result of the setup. This class describes the setting result
 */
public class ConfigMessage  extends Message{

    /**
     * Gets config result content.
     *
     * @return the config result content
     */
    public String getConfigResultContent() {
        return configResultContent;
    }

    /**
     * Sets config result content.
     *
     * @param configResultContent the config result content
     */
    public void setConfigResultContent(String configResultContent) {
        this.configResultContent = configResultContent;
    }

    /**
     * The Config result content.
     */
    protected String configResultContent;
}
