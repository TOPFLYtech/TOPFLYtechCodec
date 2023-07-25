package com.topflytech.codec.entities;


/**
 * The type UUSD message.New devices like 8806+ support this message.Old device like 8806,8803Pro does not support this message.
 */
public class USSDMessage extends Message{

    /**
     * Gets message content.
     *
     * @return the message content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets message content.
     *
     * @param content the message content
     */
    public void setContent(String content) {
        this.content = content;
    }

    private String content;
}
