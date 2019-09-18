package br.iesb.scanqrvideocode.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.iesb.scanqrvideocode.R;
import br.iesb.scanqrvideocode.binary.BinaryQRCodeWriter;

public class MainActivity extends AppCompatActivity {

    Button btnScanQRCode, btnCriarQRCode;
    private byte[] byteArray;
    private Boolean b;
    private ImageView imageView;
    private Activity context;
    private List<String> codigos;
    private String binario;
    public static final int TOTALQR = 1996;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciaVariaveis();
        eventoClicks();
    }

    private void eventoClicks() {
        btnCriarQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = new byte[0];
                criarQRCode(bytes,false);
            }
        });

        btnScanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciaScan();
            }
        });
    }

    private void iniciaScan() {
        new IntentIntegrator(this).initiateScan();

    }


    private void criarQRCode(byte[] byteArray, Boolean b) {
        if (!b) {
            carregarImg();
        } else {
            gerarQR(byteArray);
        }

    }

    private void gerarQR(byte[] byteArray) {

        BinaryQRCodeWriter qrCodeWriter = new BinaryQRCodeWriter();
        int width = 512;
        int height = 512;
        try {
            //1998 tamanho MAXIMO de string que ta dando certo
            BitMatrix byteMatrix = qrCodeWriter.encode(byteArray, BarcodeFormat.QR_CODE, width, height);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (!byteMatrix.get(x, y))
                        bmp.setPixel(x, y, Color.WHITE);
                    else
                        bmp.setPixel(x, y, Color.BLACK);
                }
            }
            imageView.setImageBitmap(bmp);
        } catch (WriterException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
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
        imageView = findViewById(R.id.img_QRCode);
    }

    @SuppressLint("StaticFieldLeak")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 111) && (resultCode == RESULT_OK)) {
            Uri filePath = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(filePath);
                final Bitmap yourSelectedImage = BitmapFactory.decodeStream(inputStream);
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... voids) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        yourSelectedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] b = baos.toByteArray();

                        String encodeImage = Base64.encodeToString(b, Base64.DEFAULT);
                        String stringretorno = "0000000000111222333" + encodeImage;
                        return stringretorno;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        String codificacao = s.substring(0,19);
                        s = s.substring(19);
                        byte[] decoedString = Base64.decode(s,Base64.DEFAULT);
                        Bitmap decoded = BitmapFactory.decodeByteArray(decoedString,0,decoedString.length);
                        imageView.setImageBitmap(decoded);

                    }
                }.execute();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Toast.makeText(context, "Serão gerados " + codigos.size() + "QR Codes", Toast.LENGTH_LONG).show();
        }
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Está vazio", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
            }

        }

    }


}
