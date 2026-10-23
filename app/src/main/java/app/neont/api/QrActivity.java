package app.neont.api;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QrActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        String link = getIntent().getStringExtra("link");
        if (link == null) {
            finish();
            return;
        }
        ImageView ivQr = findViewById(R.id.ivQrCode);
        TextView tvLink = findViewById(R.id.tvQrLink);

        tvLink.setText(link);

        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(link, BarcodeFormat.QR_CODE, 600, 600);
            ivQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        findViewById(R.id.btnCloseQr).setOnClickListener(v -> finish());
    }
}
