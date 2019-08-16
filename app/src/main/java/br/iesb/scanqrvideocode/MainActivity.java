package br.iesb.scanqrvideocode;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    Button btnScanQRCode, btnCriarQRCode;
    private byte[] byteArray;
    private Boolean b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciaVariaveis();
        eventoClicks();
        inicitFunction();
    }

    private void eventoClicks() {
        btnCriarQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int w = 15;
                int h = 15;

                Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                Bitmap bitmap = Bitmap.createBitmap(w, h, conf);
                byte[] byteArray = convert(bitmap);
                //String hexChar = bytesToHex(byteArray);
                criarQRCode(byteArray,false);
            }
        });
    }



    private void criarQRCode(byte[] byteArray, Boolean b) {
        if(!b){
            carregarImg();
        }
        else{
            gerarQR(byteArray);
        }

    }

    private void gerarQR(byte[] byteArray) {
        QRCodeWriter writer = new QRCodeWriter();
        String string = " teste teste teste teste teste teste teste teste  teste teste teste teste teste teste teste teste  teste teste teste teste teste teste teste teste  teste teste teste teste teste teste teste teste  teste teste teste teste teste teste teste teste  teste teste teste teste teste teste teste teste  teste teste teste t"
        img_QRCode as ImageView
        try {
            val bitMatrix = writer.encode(string, BarcodeFormat.QR_CODE, 512, 512)
            val width = 512
            val height = 512
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (!bitMatrix.get(x, y))
                        bmp.setPixel(x, y, Color.WHITE)
                    else
                        bmp.setPixel(x, y, Color.BLACK)
                }
            }
            img_QRCode.setImageBitmap(bmp)
        } catch (e: WriterException) {
            //Log.e("QR ERROR", ""+e);
            Toast.makeText(this,"Fuck",Toast.LENGTH_LONG)
        }
    }

    private void carregarImg() {
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 111);

    }


    private void iniciaVariaveis() {
        btnCriarQRCode = findViewById(R.id.btnCriarQRCode);
        btnScanQRCode = findViewById(R.id.btnScanQRCode);
    }

    private void inicitFunction() {

    }

    private byte[] convert(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        return stream.toByteArray();

    }
}
