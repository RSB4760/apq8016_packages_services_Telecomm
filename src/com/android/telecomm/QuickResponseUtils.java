/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.telecomm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;


/**
 * Utils class that exposes some helper routines to used to manage the QuickResponses
 */
public class QuickResponseUtils {
    public static final String LOG_TAG = "QuickResponseUtils";

    // SharedPreferences file name for our persistent settings.
    public static final String SHARED_PREFERENCES_NAME = "respond_via_sms_prefs";
    private static final String PACKAGE_NAME_TELEPHONY = "com.android.phone";

    // Preference keys for the 4 "canned responses"; see RespondViaSmsManager$Settings.
    // Since (for now at least) the number of messages is fixed at 4, and since
    // SharedPreferences can't deal with arrays anyway, just store the messages
    // as 4 separate strings.
    public static final int NUM_CANNED_RESPONSES = 4;
    public static final String KEY_CANNED_RESPONSE_PREF_1 = "canned_response_pref_1";
    public static final String KEY_CANNED_RESPONSE_PREF_2 = "canned_response_pref_2";
    public static final String KEY_CANNED_RESPONSE_PREF_3 = "canned_response_pref_3";
    public static final String KEY_CANNED_RESPONSE_PREF_4 = "canned_response_pref_4";

    /**
     * As of L, QuickResponses were moved from Telephony to Telecomm. Because of
     * this, we need to make sure that we migrate any old QuickResponses to our
     * current SharedPreferences.  This is a lazy migration as it happens only when
     * the QuickResponse settings are viewed or if they are queried via RespondViaSmsManager.
     */
    public static void maybeMigrateLegacyQuickResponses() {
        // The algorithm will go as such:
        // If Telecomm QuickResponses exist, we will skip migration because this implies
        // that a user has already specified their desired QuickResponses and have abandoned any
        // older QuickResponses.
        // Then, if Telephony QuickResponses exist, we will move those to Telecomm.
        // If neither exist, we'll populate Telecomm with the default QuickResponses.
        // This guarantees the caller that QuickResponses exist in SharedPreferences after this
        // function is called.

        Log.d(LOG_TAG, "maybeMigrateLegacyQuickResponses() - Starting");

        final Context telecommContext = TelecommApp.getInstance();
        final SharedPreferences prefs = telecommContext.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final Resources res = telecommContext.getResources();

        final boolean responsesExist = prefs.contains(KEY_CANNED_RESPONSE_PREF_1);
        if (responsesExist) {
            // If one QuickResponse exists, they all exist.
            Log.d(LOG_TAG, "maybeMigrateLegacyQuickResponses() - Telecomm QuickResponses exist");
            return;
        }

        // Grab the all the default QuickResponses from our resources.
        String cannedResponse1 = res.getString(R.string.respond_via_sms_canned_response_1);
        String cannedResponse2 = res.getString(R.string.respond_via_sms_canned_response_2);
        String cannedResponse3 = res.getString(R.string.respond_via_sms_canned_response_3);
        String cannedResponse4 = res.getString(R.string.respond_via_sms_canned_response_4);

        Log.d(LOG_TAG, "maybeMigrateLegacyQuickResponses() - No local QuickResponses");

        // We don't have local QuickResponses, let's see if they live in
        // the Telephony package and we'll fall back on using our default values.
        Context telephonyContext = null;
        try {
            telephonyContext = telecommContext.createPackageContext(PACKAGE_NAME_TELEPHONY, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, e, "maybeMigrateLegacyQuickResponses() - Can't find Telephony package.");
        }

        // Read the old canned responses from the Telephony SharedPreference if possible.
        if (telephonyContext != null) {
            // Note that if any one QuickResponse does not exist, we'll use the default
            // value to populate it.
            Log.d(LOG_TAG, "maybeMigrateLegacyQuickResponses() - Using Telephony QuickResponses.");
            final SharedPreferences oldPrefs = telephonyContext.getSharedPreferences(
                    SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            cannedResponse1 = oldPrefs.getString(KEY_CANNED_RESPONSE_PREF_1, cannedResponse1);
            cannedResponse2 = oldPrefs.getString(KEY_CANNED_RESPONSE_PREF_2, cannedResponse2);
            cannedResponse3 = oldPrefs.getString(KEY_CANNED_RESPONSE_PREF_3, cannedResponse3);
            cannedResponse4 = oldPrefs.getString(KEY_CANNED_RESPONSE_PREF_4, cannedResponse4);
        }

        // Either way, write them back into Telecomm SharedPreferences.
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CANNED_RESPONSE_PREF_1, cannedResponse1);
        editor.putString(KEY_CANNED_RESPONSE_PREF_2, cannedResponse2);
        editor.putString(KEY_CANNED_RESPONSE_PREF_3, cannedResponse3);
        editor.putString(KEY_CANNED_RESPONSE_PREF_4, cannedResponse4);
        editor.commit();

        Log.d(LOG_TAG, "maybeMigrateLegacyQuickResponses() - Done.");
        return;
    }
}