package com.joesnason.blescanner;

/**
 * Created by joesnason on 2015/6/2.
 */
public class Util {



    /**
     * bytesToHex method
     * Found on the internet
     * http://stackoverflow.com/a/9855338
     */
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static double calucateDistance(int rssi, int TxPower){


        double ratio = ((float)rssi) / TxPower;
        // less than Txpower,  it mean the Beacon in  ONE Miles range. the distance and rssi are linear relationship.
        if(ratio < 1.0){
            return Math.pow(ratio, 10);
        }
        return 0.89976 * Math.pow(ratio, 7.7095) + 0.111;
    }

}
