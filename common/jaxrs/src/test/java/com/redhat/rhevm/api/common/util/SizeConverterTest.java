package com.redhat.rhevm.api.common.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class SizeConverterTest {

    @Test
    public void testMegasToBytes() {
        int megabytes = 3;
        long bytes = SizeConverter.megasToBytes(megabytes);
        assertEquals(bytes, 3145728);
    }

    @Test
    public void testGigasToBytes() {
        int gigabytes = 3;
        long bytes = SizeConverter.gigasToBytes(gigabytes);
        assertEquals(bytes, 3221225472L);
    }

    @Test
    public void testBytestoGigas() {
        long bytes = 3221228000L;
        int gigabytes = (int)SizeConverter.bytesToGigas(bytes);
        assertEquals(gigabytes, 3);
    }

    @Test
    public void testBytestoMegas() {
        long bytes = 3160000;
        int megabytes = (int)SizeConverter.bytesToMegas(bytes);
        assertEquals(megabytes, 3);
    }
}
