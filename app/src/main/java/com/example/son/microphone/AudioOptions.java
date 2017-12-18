package com.example.son.microphone;

/**
 * Created by son on 19.09.2017.
 */


        import android.annotation.SuppressLint;
        import android.content.Context;
        import android.graphics.Typeface;
        import android.util.AttributeSet;
        import android.widget.Button;


@SuppressLint("AppCompatCustomView")
public class AudioOptions extends Button {
    public AudioOptions(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public AudioOptions(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioOptions(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Roboto-Light.ttf");
        setTypeface(tf);
    }
}