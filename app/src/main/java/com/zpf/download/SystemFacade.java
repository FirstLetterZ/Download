package com.zpf.download;

import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

interface SystemFacade {
    /**
     * @see System#currentTimeMillis()
     */
    public long currentTimeMillis();
    public Network getActiveNetwork(int uid, boolean ignoreBlocked);

    public Network getNetwork(JobParameters params);
    public NetworkInfo getNetworkInfo(Network network, int uid, boolean ignoreBlocked);

    public NetworkCapabilities getNetworkCapabilities(Network network);

    /**
     * @return maximum size, in bytes, of downloads that may go over a mobile connection; or null if
     * there's no limit
     */
    public long getMaxBytesOverMobile();

    /**
     * @return recommended maximum size, in bytes, of downloads that may go over a mobile
     * connection; or null if there's no recommended limit.  The user will have the option to bypass
     * this limit.
     */
    public long getRecommendedMaxBytesOverMobile();

    /**
     * Send a broadcast intent.
     */
    public void sendBroadcast(Intent intent);

    /**
     * Send a broadcast intent with options.
     */
    public void sendBroadcast(Intent intent, String receiverPermission, Bundle options);

    /**
     * Returns true if the specified UID owns the specified package name.
     */
    public boolean userOwnsPackage(int uid, String pckg) throws NameNotFoundException;

    /**
     * Returns true if cleartext network traffic is permitted from {@code packageName} to
     * {@code host}.
     */
    public boolean isCleartextTrafficPermitted(String packageName, String host);

    /**
     * Return a {@link SSLContext} configured using the specified package's configuration.
     */
    public SSLContext getSSLContextForPackage(Context context, String pckg)
            throws GeneralSecurityException;
}