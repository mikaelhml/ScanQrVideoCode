package br.iesb.scanqrvideocode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;

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
        String string = " teste teste teste teste teste teste teste teste  teste teste teste teste teste teste teste teste  teste teste teste teste teste teste teste teste  teste teste teste teste teste teste teste teste  teste teste teste teste teste teste teste teste  teste teste teste teste teste teste teste teste  teste teste teste t";

        try {
            BitMatrix bitMatrix = writer.encode(string, BarcodeFormat.QR_CODE, 512, 512);
            int width = 512;
            int height = 512;
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x=0; x < width; x++) {
                for (int y=0;y<height;y++){
                    if (!bitMatrix.get(x, y))
                        bmp.setPixel(x, y, Color.WHITE);
                    else
                        bmp.setPixel(x, y, Color.BLACK);
                }
            }

        } catch (WriterException e) {
            //Log.e("QR ERROR", ""+e);
            Toast.makeText(this,"Fuck",Toast.LENGTH_LONG);
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

    //



    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        // Check which request it is that we're responding to
        if (requestCode == 111) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = resultIntent.getData();
                // We only need the NUMBER column, because there will be only one row in the result
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using <code><a href="/reference/android/content/CursorLoader.html">CursorLoader</a></code> to perform the query.
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                // Retrieve the phone number from the NUMBER column
                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(column);

                // Do something with the phone number...
            }
        }
    }

*/



    //


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if ((requestCode == 111) && (resultCode == RESULT_OK)) {
            Uri selectedFile = data.getData(); //The uri with the location of the file
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedFile);
            byte[] string64 = convert(bitmap);
            criarQRCode(string64,true);
        }
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Está vazio", Toast.LENGTH_LONG).show();
            } else {


                val options = BitmapFactory.Options();
                val bitmap = BitmapFactory.decodeByteArray(result.contents.toByteArray(), 0, result.contents.toString().length, options);
                img_QRCode.setImageBitmap(bitmap);
                Toast.makeText(this, result.contents.toString(), Toast.LENGTH_LONG).show();


            }

        } else {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }





    //

    private void inicitFunction() {

    }

    private byte[] convert(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        return stream.toByteArray();

    }
}
