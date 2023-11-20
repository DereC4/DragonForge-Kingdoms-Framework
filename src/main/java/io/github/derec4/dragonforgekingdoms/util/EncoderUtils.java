package io.github.derec4.dragonforgekingdoms.util;

public class EncoderUtils {

    /*
    Converts a byte into 4 floats
    1 byte represents 2^8
    4 bytes represent 2^32

    float is 2^32
     */
    public static byte[] encodeFloat(float f) {
        int intBits =  Float.floatToIntBits(f);
        return new byte[] {
                (byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
    }

    public static float byteArrayToFloat(byte[] bytes, int seek) {
        int intBits =
                bytes[seek] << 24 | (bytes[seek + 1] & 0xFF) << 16 | (bytes[seek + 2] & 0xFF) << 8 | (bytes[seek + 3] & 0xFF);
        return Float.intBitsToFloat(intBits);
    }

    public static byte[] encodeInt(int intBits) {
        return new byte[] {
                (byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
    }

    public static int byteArrayToInt(byte[] bytes, int seek) {
        return bytes[seek] << 24 + bytes[seek + 1] << 16 + bytes[seek + 2] << 8 + bytes[seek + 3];
    }

    /*
        To encode a string, you write the length of the string, then in each byte represents
        ascii code
     */
    public static byte[] encodeString(String string) {
        int length = string.length();
        char[] charArr = string.toCharArray();

        byte[] bytes = new byte[charArr.length + 4];

        byte[] lengthRepresentation = encodeInt(length);

        bytes[0] = lengthRepresentation[0];
        bytes[1] = lengthRepresentation[1];
        bytes[2] = lengthRepresentation[2];
        bytes[3] = lengthRepresentation[3];

        for(int i = 0; i < charArr.length; i++) {
            bytes[i + 4] = (byte) charArr[i];
        }

        return bytes;
    }

    public static String byteArrayToString(byte[] bytes, int seek) {
        byte[] length = new byte[] { bytes[seek], bytes[seek + 1], bytes[seek + 2], bytes[seek + 3] };

        int stringLength = byteArrayToInt(length, 0);

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < stringLength; i++) {
            char c = (char) bytes[seek + i + 4];

            sb.append(c);
        }

        return sb.toString();
    }

}
