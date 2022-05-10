
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zpf.download;

import android.app.DownloadManager;
import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.security.NetworkSecurityPolicy;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

class RealSystemFacade implements SystemFacade {
    private Context mContext;

    public RealSystemFacade(Context context) {
        mContext = context;
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public Network getNetwork(JobParameters params) {
        return params.getNetwork();
    }

    @Override
    public NetworkInfo getNetworkInfo(Network network, int uid, boolean ignoreBlocked) {
        return mContext.getSystemService(ConnectivityManager.class)
                .getNetworkInfo(network);
    }

    @Override
    public Network getActiveNetwork(int uid, boolean ignoreBlocked) {
        return mContext.getSystemService(ConnectivityManager.class)
                .getActiveNetwork();
    }

    @Override
    public NetworkCapabilities getNetworkCapabilities(Network network) {
        return mContext.getSystemService(ConnectivityManager.class)
                .getNetworkCapabilities(network);
    }

    @Override
    public long getMaxBytesOverMobile() {
        final Long value = DownloadManager.getMaxBytesOverMobile(mContext);
        return (value == null) ? Long.MAX_VALUE : value;
    }

    @Override
    public long getRecommendedMaxBytesOverMobile() {
        final Long value = DownloadManager.getRecommendedMaxBytesOverMobile(mContext);
        return (value == null) ? Long.MAX_VALUE : value;
    }

    @Override
    public void sendBroadcast(Intent intent) {
        mContext.sendBroadcast(intent);
    }

    @Override
    public void sendBroadcast(Intent intent, String receiverPermission, Bundle options) {
        mContext.sendBroadcast(intent, receiverPermission);
    }

    @Override
    public boolean userOwnsPackage(int uid, String packageName) throws NameNotFoundException {
        return mContext.getPackageManager().getApplicationInfo(packageName, 0).uid == uid;
    }

    @Override
    public SSLContext getSSLContextForPackage(Context context, String packageName)
            throws GeneralSecurityException {
        return SSLContext.getDefault();
    }

    /**
     * Returns whether cleartext network traffic (HTTP) is permitted for the provided package to
     * {@code host}.
     */
    public boolean isCleartextTrafficPermitted(String packageName, String host) {
        return true;
    }
}