package com.android.fortunaattendancesystem.extras;

/**
 * Created by fortuna on 17/11/16.
 */
import android.location.Location;

public interface LocationTracker {
    interface LocationUpdateListener{
        void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime);
    }

    void start();
    void start(LocationUpdateListener update);

    void stop();

    boolean hasLocation();

    boolean hasPossiblyStaleLocation();

    Location getLocation();

    Location getPossiblyStaleLocation();

}
