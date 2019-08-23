package br.iesb.scanqrvideocode.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.FormatException;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.decoder.Version;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.iesb.scanqrvideocode.R;
import br.iesb.scanqrvideocode.binary.BinaryQRCodeWriter;

import static br.iesb.scanqrvideocode.decoder.DecodedBitStreamParser.decode;

public class MainActivity extends AppCompatActivity {

    Button btnScanQRCode, btnCriarQRCode;
    private byte[] byteArray;
    private Boolean b;
    private ImageView imageView;

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
                //int w = 15;
                // int h = 15;

                //Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                //Bitmap bitmap = Bitmap.createBitmap(w, h, conf);
                //String hexChar = bytesToHex(byteArray);
                // criarQRCode(byteArray, false);
                BinaryQRCodeWriter qrCodeWriter = new BinaryQRCodeWriter();
                byte[] bytes = hexStringToByteArray("BF05FF");//0123456789ABCDEF0123456789ABCDEF01");
                int width = 512;
                int height = 512;
                String fileType = "png";
                String filePath = "QRcode.png"; // Salva a imagem do QR Code nesse caminho!
                File qrFile = new File(filePath);
                try {
                    BitMatrix byteMatrix = qrCodeWriter.encode(bytes, BarcodeFormat.QR_CODE, width, height);
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
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
            //gerarQR(byteArray);
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

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if ((requestCode == 111) && (resultCode == RESULT_OK)) {
            Uri selectedFile = data.getData(); //The uri with the location of the file
        }
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Está vazio", Toast.LENGTH_LONG).show();
            } else {

                BitmapFactory.Options options = new BitmapFactory.Options();
                //DecoderResult bits = decode(result.getContents().getBytes());
                Bitmap bitmap = BitmapFactory.decodeByteArray(result.getContents().getBytes(), 0, result.getContents().length(), options);
                imageView.setImageBitmap(bitmap);
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();


            }

        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
