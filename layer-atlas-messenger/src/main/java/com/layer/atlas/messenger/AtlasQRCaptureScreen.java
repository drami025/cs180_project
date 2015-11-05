/*
 * Copyright (c) 2015 Layer. All rights reserved.
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
package com.layer.atlas.messenger;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.CompoundBarcodeView;


public class AtlasQRCaptureScreen extends Activity {
    private CaptureManager capture;
    private CompoundBarcodeView qrView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.atlas_screen_qr);

        qrView = new CompoundBarcodeView(this, R.layout.atlas_qr_scanner, R.id.zxing_barcode_surface, R.id.zxing_viewfinder_view, R.id.zxing_status_view);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        qrView.setLayoutParams(params);

        Resources r = getResources();
        qrView.setColors(
                r.getColor(R.color.atlas_qr_mask),
                r.getColor(R.color.atlas_qr_result),
                r.getColor(R.color.atlas_qr_laser),
                r.getColor(R.color.atlas_qr_result));

        ((LinearLayout) findViewById(R.id.qr)).addView(qrView);

        this.capture = new CaptureManager(this, this.qrView);
        this.capture.initializeFromIntent(this.getIntent(), savedInstanceState);
        this.capture.decode();
    }

    protected void onResume() {
        super.onResume();
        this.capture.onResume();
    }

    protected void onPause() {
        super.onPause();
        this.capture.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.capture.onDestroy();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.capture.onSaveInstanceState(outState);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.qrView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
