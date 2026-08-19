package com.topflytech.lockActive.data;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IdentityInfoEncoderTest {
    @Test
    public void automaticIdUsesExistingSixByteEncoding() {
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06},
                IdentityInfoEncoder.encodeAutomatic("010203040506"));
    }

    @Test
    public void hexadecimalIdUsesRawBytesAndAllowsTwentyBytes() {
        String value = "00112233445566778899AABBCCDDEEFF00112233";

        assertEquals(20, IdentityInfoEncoder.encodeHex(value).length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexadecimalIdRejectsMoreThanTwentyBytes() {
        IdentityInfoEncoder.encodeHex("00112233445566778899AABBCCDDEEFF0011223344");
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexadecimalIdRejectsOddLength() {
        IdentityInfoEncoder.encodeHex("ABC");
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexadecimalIdRejectsLessThanSixBytes() {
        IdentityInfoEncoder.encodeHex("0011223344");
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexadecimalIdRejectsNonHexCharacters() {
        IdentityInfoEncoder.encodeHex("GG");
    }

    @Test
    public void textUsesUtf8Bytes() {
        String value = "中文";

        assertArrayEquals(value.getBytes(StandardCharsets.UTF_8), IdentityInfoEncoder.encodeText(value));
    }

    @Test(expected = IllegalArgumentException.class)
    public void textRejectsMoreThanTwentyUtf8Bytes() {
        IdentityInfoEncoder.encodeText("abcdefghijklmnopqrstu");
    }

    @Test(expected = IllegalArgumentException.class)
    public void textRejectsLessThanSixUtf8Bytes() {
        IdentityInfoEncoder.encodeText("abcde");
    }
}
