package myandroid.app.hhobzic.a360pool.classes;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import myandroid.app.hhobzic.pool360.R;

public class LoadingDialog extends Dialog {
    private String message;

    public LoadingDialog(Context context, String message) {
        super(context);
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);
        TextView messageTextView = findViewById(R.id.message);
        messageTextView.setText(message);
    }
}