package com.autoxing.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.autoxing.controller.R;

public class SaveMapDialog extends Dialog {

    Context mContext;
    private EditText mMapName;
    private Button mSavePop;
    private View.OnClickListener mClickListener;

    public SaveMapDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public void setListener(View.OnClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.save_map_dialog);

        mMapName = (EditText) findViewById(R.id.et_map_name);

        Window dialogWindow = this.getWindow();
        WindowManager m = ((Activity)mContext).getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        // p.height = (int) (d.getHeight() * 0.6);
        p.width = (int) (d.getWidth() * 0.8);
        dialogWindow.setAttributes(p);

        mSavePop = (Button) findViewById(R.id.btn_save_pop);
        mSavePop.setOnClickListener(mClickListener);

        this.setCancelable(true);
    }

    public EditText getMapName() {
        return mMapName;
    }

    public void setMapName(EditText mapName) {
        this.mMapName = mapName;
    }
}
