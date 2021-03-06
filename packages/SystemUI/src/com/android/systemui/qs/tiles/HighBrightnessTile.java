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

package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.android.systemui.R;
import com.android.systemui.SysUIToast;
import com.android.systemui.qs.QSTile;

import com.android.internal.logging.MetricsProto.MetricsEvent;

public class HighBrightnessTile extends QSTile<QSTile.BooleanState> {

    public static final String HIGH_BRIGHTNESS_MODE = "high_brightness_mode";
    private static final int HIGH_BRIGHTNESS_MODE_OFF = 0;
    private static final int HIGH_BRIGHTNESS_MODE_ON = 1;
    private static final int DEFAULT_HIGH_BRIGHTNESS_MODE = 0;

    public HighBrightnessTile(Host host) {
        super(host);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    private ContentObserver mObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            refreshState();
        }
    };

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.HIGH_BRIGHTNESS_MODE),
                    false, mObserver);
        } else {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
        }
    }

    @Override
    public void handleClick() {
        toggleState();
        refreshState();
    }

    @Override
    public void handleLongClick() {

    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_high_brightness);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        boolean HighBrightness = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.HIGH_BRIGHTNESS_MODE, DEFAULT_HIGH_BRIGHTNESS_MODE,
                UserHandle.USER_CURRENT) != 0;

        state.label = mContext.getString(R.string.quick_settings_high_brightness);
        state.icon = HighBrightness
                ? ResourceIcon.get(R.drawable.ic_qs_brightness_high_on)
                : ResourceIcon.get(R.drawable.ic_qs_brightness_high_off);
    }

    protected void toggleState() {
        int mode = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.HIGH_BRIGHTNESS_MODE, DEFAULT_HIGH_BRIGHTNESS_MODE,
                UserHandle.USER_CURRENT);
        switch (mode) {
            case HIGH_BRIGHTNESS_MODE_OFF:
                Settings.Secure.putInt(mContext.getContentResolver(),
                        HIGH_BRIGHTNESS_MODE, HIGH_BRIGHTNESS_MODE_ON);
                mHost.collapsePanels();
                SysUIToast.makeText(mContext, mContext.getString(
                        R.string.high_brightness_warning),
                        Toast.LENGTH_LONG).show();
                break;
            case HIGH_BRIGHTNESS_MODE_ON:
                Settings.Secure.putInt(mContext.getContentResolver(),
                        HIGH_BRIGHTNESS_MODE, HIGH_BRIGHTNESS_MODE_OFF);
                break;
        }
    }
}