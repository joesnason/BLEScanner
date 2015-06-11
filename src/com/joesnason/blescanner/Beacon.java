package com.joesnason.blescanner;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by joesnason on 2015/6/2.
 */
public class Beacon  implements Parcelable {

    private final String uuid;
    private final String name;
    private final int major;
    private final int minor;
    private final int rssi;

    public Beacon(String proximityUUID, String BeaconName, int major, int minor, int rssi){
        this.uuid = proximityUUID;
        this.name = BeaconName;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;

    }

    public String getName(){
        return name;
    }

    public String getUUID(){
        return uuid;
    }


    public int getMajor(){
        return major;
    }

    public int getMinor(){
        return minor;
    }

    public int getRssi(){
        return rssi;
    }

    public boolean equals(Object obj){
        if (this == obj)
            return true;

        if(obj != null){
            Beacon beacon = (Beacon)obj;
            if (this.uuid == beacon.uuid && this.major == beacon.major && this.minor == beacon.minor)
                return true;
        }


        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeString(this.uuid);
        parcel.writeString(this.name);
        parcel.writeInt(this.major);
        parcel.writeInt(this.minor);
        parcel.writeInt(this.rssi);
    }

}
