package com.topflytech.lockActive.data;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class IdentityInfoEncoder {
    private static final int MIN_BYTES = 6;
    private static final int MAX_BYTES = 20;
    private static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]+");

    private IdentityInfoEncoder() {
    }

    public static byte[] encodeAutomatic(String value) {
        if (value == null || value.length() != 12 || !HEX_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Automatic ID must be 12 hexadecimal characters");
        }
        return MyByteUtils.hexString2Bytes(value);
    }

    public static byte[] encodeHex(String value) {
        if (value == null || value.length() < MIN_BYTES * 2 || value.length() % 2 != 0
                || value.length() > MAX_BYTES * 2 || !HEX_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Hexadecimal ID must contain 6 to 20 bytes");
        }
        return MyByteUtils.hexString2Bytes(value);
    }

    public static byte[] encodeText(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_BYTES || bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("Description must contain 6 to 20 UTF-8 bytes");
        }
        return bytes;
    }
}
