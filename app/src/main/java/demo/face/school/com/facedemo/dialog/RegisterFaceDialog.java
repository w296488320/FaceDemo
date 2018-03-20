package demo.face.school.com.facedemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import demo.face.school.com.facedemo.R;
import demo.face.school.com.facedemo.model.EventBusModel;


public class RegisterFaceDialog extends Dialog {

    private ImageView extimageview;
    private TextView tip_txt;
    private Button upload_btn, cancel_btn;

    public RegisterFaceDialog(Context context, final Bitmap mBitmap) {
        super(context);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.dialog_register);
        extimageview = (ImageView) findViewById(R.id.extimageview);
        extimageview.setImageBitmap(mBitmap);
        tip_txt = (TextView) findViewById(R.id.tip);
        cancel_btn = (Button) findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterFaceDialog.this.dismiss();
            }
        });
        upload_btn = (Button) findViewById(R.id.upload_btn);
        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new EventBusModel("UploadFaceData", "1"));
                RegisterFaceDialog.this.dismiss();

            }
        });

    }

}