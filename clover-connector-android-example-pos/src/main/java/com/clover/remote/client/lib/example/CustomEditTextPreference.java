package com.clover.remote.client.lib.example;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by blakewilliams on 8/22/15.
 *
 * created this subclass so I could programmatically open the dialog
 */

public class CustomEditTextPreference extends EditTextPreference {

    public CustomEditTextPreference(Context ctx) {
        super(ctx);
    }

    public CustomEditTextPreference(Context ctx, AttributeSet attributeSet) {
        super(ctx, attributeSet);
    }
    public CustomEditTextPreference(Context ctx, AttributeSet attributeSet, int defStyle) {
        super(ctx, attributeSet, defStyle);
    }

    public void show(Bundle state) {
        super.showDialog(state);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
    }
}
