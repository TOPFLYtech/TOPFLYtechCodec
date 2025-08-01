package com.topflytech.tftble.data;

import java.util.UUID;

public class WriteSensorObj {
    private UUID curUUID;
    private byte[] content;

    public WriteSensorObj(UUID curUUID, byte[] content) {
        this.curUUID = curUUID;
        this.content = content;
    }

    public UUID getCurUUID() {
        return curUUID;
    }

    public void setCurUUID(UUID curUUID) {
        this.curUUID = curUUID;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
