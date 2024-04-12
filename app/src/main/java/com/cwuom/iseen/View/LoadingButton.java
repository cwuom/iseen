package com.cwuom.iseen.View;

/*
 * This software is provided for educational purposes only and should not be used for commercial or illegal activities.
 * Please respect the original author's work by retaining their information intact.
 * If you make modifications to this code, you can repackage it accordingly.
 *
 * Original Author: cwuom
 * Date: 2024/4/12
 *
 * Instructions:
 * 1. Make necessary modifications.
 * 2. Rebuild the app.
 * 3. Retain this header.
 *
 * Thank you!
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.cwuom.iseen.R;

public class LoadingButton extends FrameLayout {

    private Button button;
    private ProgressBar progressBar;
    private String btn_text;

    public LoadingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.loading_button, this, true);
        button = findViewById(R.id.button);
        progressBar = findViewById(R.id.progress_bar);

        // Handle attributes
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.LoadingButton,
                    0, 0);

            try {
                String text = a.getString(R.styleable.LoadingButton_android_text);
                if (text != null) {
                    button.setText(text);
                    btn_text = text;
                }
            } finally {
                a.recycle();
            }
        }
    }

    public void setButtonText(CharSequence text) {
        button.setText(text);
        btn_text = String.valueOf(text);
    }

    public void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? VISIBLE : GONE);
        button.setEnabled(!loading);
        button.setText(loading ? "" : btn_text);
    }

    public void setOnClickListener(OnClickListener listener) {
        button.setOnClickListener(listener);
    }
}
