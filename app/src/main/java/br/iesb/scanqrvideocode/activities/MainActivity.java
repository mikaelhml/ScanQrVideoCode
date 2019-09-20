package br.iesb.scanqrvideocode.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import br.iesb.scanqrvideocode.R;

public class MainActivity extends AppCompatActivity {

    Button btnScanQRCode, btnCriarQRCode;
    private ImageView imageView;
    private TextView textQRLidos;
    private Integer quantidadeQR;
    private ArrayList<String> listaString = new ArrayList<>();


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
                criarQRCode();
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


    private void criarQRCode() {
            carregarImg();
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
        imageView = findViewById(R.id.img_Recuperada);
        textQRLidos = findViewById(R.id.textQRLidos);
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
                        Intent i = new Intent(MainActivity.this, GerarQrs.class);
                        i.putExtra("string64", encodeImage);
                        startActivity(i);
                        finish();
                        return encodeImage;
                    }

                    @Override
                    protected void onPostExecute(String s) {
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
            }
            if (result.getContents().length() > 50) {
                if (listaString.contains(result.getContents())) {
                    Toast.makeText(this, "Este Qr Ja foi Lido", Toast.LENGTH_LONG).show();
                } else {
                    listaString.add(result.getContents());
                    Toast.makeText(this, "Valor do QR adicionado a lista", Toast.LENGTH_LONG).show();
                    quantidadeQR = Integer.valueOf(listaString.get(0).substring(3, 6));
                    textQRLidos.setText("Voce leu: " + listaString.size() + "      De: " + quantidadeQR);
                    if (listaString.size() == quantidadeQR) {
                        Collections.sort(listaString);
                        montarImg();
                    }
                }

            } else {
                Toast.makeText(this, "Falha na Leitura", Toast.LENGTH_SHORT).show();
                new IntentIntegrator(this).initiateScan();
            }
        }
    }

    private void montarImg() {
        String string64Remontada = "";
        for (int i = 0; i < quantidadeQR; i++) {
            string64Remontada = string64Remontada + listaString.get(i).substring(6);
        }
        byte[] decoedString = Base64.decode(string64Remontada, Base64.DEFAULT);
        Bitmap decoded = BitmapFactory.decodeByteArray(decoedString, 0, decoedString.length);
        imageView.setImageBitmap(decoded);

    }

}
