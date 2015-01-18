package com._78737146.eece411.A1;

public class StringUtils {

    public static String byteArrayToHexString(byte[] bytes, int size) {
        StringBuffer buf=new StringBuffer();
        String       str;
        int val;

        for (int i=0; i<size; i++) {
            val = ByteOrder.ubyte2int(bytes[i]);
            str = Integer.toHexString(val);
            while ( str.length() < 2 )
                str = "0" + str;
            buf.append( str );
        }
        return buf.toString().toUpperCase();
    }
}
