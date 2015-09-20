package com.fsck.k9.mail.transport;
/*
 * Copyright 2013 Michael Boyde Wallace (http://wallaceit.com.au)
 * This file is part of Visual Voicemail.
 *
 * Visual Voicemail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Visual Voicemail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Visual Voicemail (COPYING). If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by michael on 15/09/15.
 */

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HipriController {

    /**
     * Enable mobile connection for a specific address, the connection is closed after the app & services have exited.
     * Note: background sync with push causes it to be permanently enabled
     * @param connectivityManager a ConnectivityManager
     * @param address the address to enable
     * @return true for success, else false
     */
    public static boolean start(ConnectivityManager connectivityManager, String address) {

        String TAG_LOG = "ForceCellular";
        if (null == connectivityManager) {
            Log.d(TAG_LOG, "ConnectivityManager is null, cannot try to force a mobile connection");
            return false;
        }

        //check if mobile connection is available and connected
        NetworkInfo.State state = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
        Log.d(TAG_LOG, "TYPE_MOBILE_HIPRI network state: " + state);
        if (0 == state.compareTo(NetworkInfo.State.CONNECTED) || 0 == state.compareTo(NetworkInfo.State.CONNECTING)) {
            return true;
        }
        Log.d("ForceCellular", "Bind host: " + address);

        //activate mobile connection in addition to other connection already activated: this allows DNS requests through the mobile network
        int resultInt = connectivityManager.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
        Log.d(TAG_LOG, "startUsingNetworkFeature for enableHIPRI result: " + resultInt);

        //-1 means errors
        // 0 means already enabled
        // 1 means enabled
        // other values can be returned, because this method is vendor specific
        if (-1 == resultInt) {
            Log.e(TAG_LOG, "Error result of startUsingNetworkFeature, maybe problems");
            return false;
        }
        if (0 == resultInt) {
            Log.d(TAG_LOG, "No need to perform additional network settings");
            return true;
        }

        //find the host name to route
        String hostName = address; //StringUtil.extractAddressFromUrl(address);
        Log.d(TAG_LOG, "Source address: " + address);
        Log.d(TAG_LOG, "Destination host address to route: " + hostName);
        if (TextUtils.isEmpty(hostName)) hostName = address;

        //create a route for the specified address
        int hostAddress = lookupHost(hostName);
        if (-1 == hostAddress) {
            Log.e(TAG_LOG, "Wrong host address transformation, result was -1");
            return false;
        }
        //wait some time needed to connection manager for waking up
        try {
            for (int counter=0; counter<30; counter++) {
                NetworkInfo.State checkState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
                if (0 == checkState.compareTo(NetworkInfo.State.CONNECTED))
                    break;
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            //nothing to do
        }
        boolean resultBool = connectivityManager.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_HIPRI, hostAddress);
        Log.d(TAG_LOG, "requestRouteToHost result: " + resultBool);
        if (!resultBool)
            Log.e(TAG_LOG, "Wrong requestRouteToHost result: expected true, but was false");

        return resultBool;
    }

    /**
     * Transform host name in int value used by ConnectivityManager.requestRouteToHost
     * method
     *
     * @param hostname
     * @return -1 if the host doesn't exists, elsewhere its translation
     * to an integer
     */
    private static int lookupHost(String hostname) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return -1;
        }
        byte[] addrBytes;
        int addr;
        addrBytes = inetAddress.getAddress();
        addr = ((addrBytes[3] & 0xff) << 24)
                | ((addrBytes[2] & 0xff) << 16)
                | ((addrBytes[1] & 0xff) << 8 )
                |  (addrBytes[0] & 0xff);
        return addr;
    }
}
