package com.redhat.rhevm.api.common.util;

/**
 * A utility class for converting sizes; i.e: bytes to mega-bytes, giga-bytes to bytes, etc.
 *
 * @author ori
 *
 */
public class SizeConverter {

    public static Long BYTES_IN_MEGA = 1024L * 1024L;
    public static Long MEGAS_IN_GIGA = 1024L;

    public static long megasToBytes(int megabytes) {
        return megabytes * BYTES_IN_MEGA;
    }

    public static long megasToBytes(long megabytes) {
        return megabytes * BYTES_IN_MEGA;
    }

    public static long gigasToBytes(int gigabytes) {
        return gigabytes * BYTES_IN_MEGA * MEGAS_IN_GIGA;
    }

    public static long gigasToBytes(long gigabytes) {
        return gigabytes * BYTES_IN_MEGA * MEGAS_IN_GIGA;
    }

    /**
     * Converts bytes to mega-bytes. Rounds down to the nearest mega-byte.
     * @param bytes number of bytes
     * @return number of megabytes.
     */
    public static long bytesToMegas(long bytes) {
        return bytes/BYTES_IN_MEGA;
    }

    /**
     * Converts bytes to giga-bytes. Rounds down to the nearest giga-byte.
     * @param bytes number of bytes
     * @return number of gigabytes.
     */
    public static long bytesToGigas(long bytes) {
        return bytes/(BYTES_IN_MEGA * MEGAS_IN_GIGA);
    }
}
