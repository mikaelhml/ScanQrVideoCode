package br.iesb.scanqrvideocode.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

import br.iesb.scanqrvideocode.R;
import br.iesb.scanqrvideocode.binary.BinaryQRCodeReader;
import br.iesb.scanqrvideocode.encoder.QRCode;
import br.iesb.scanqrvideocode.encoder.URIUtil;

import static br.iesb.scanqrvideocode.constants.Contantes.FOLDER_APP;
import static br.iesb.scanqrvideocode.constants.Contantes.PATHFILE;

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
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
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
            Uri uri = data.getData();
            Uri uri2 = Uri.parse(getRealPathFromUri(uri));


            //InputStream inputStream = getContentResolver().openInputStream(filePath);
            //final Bitmap yourSelectedImage = BitmapFactory.decodeStream(inputStream);
            File file = new File(uri2.getPath());
            try {

                FileInputStream inputStream = new FileInputStream(file);
                int size = (int) file.length();
                byte[] bytes = new byte[size];

                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();
                String encodedString = byteArrayToString(bytes);
                //String encodeImage = Base64.encodeToString(bytes, Base64.DEFAULT);
                Intent i = new Intent(MainActivity.this, GerarQrs.class);
                i.putExtra("string64", encodedString);
                startActivity(i);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }


            /*new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    // yourSelectedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();


                    return encodeImage;
                }

                @Override
                protected void onPostExecute(String s) {
                }
            }.execute();
            //Toast.makeText(context, "Serão gerados " + codigos.size() + "QR Codes", Toast.LENGTH_LONG).show();

             */
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Está vazio", Toast.LENGTH_LONG).show();
            }
            if (result.getContents().length() > 15) {
                if (listaString.contains(result.getContents())) {
                    Toast.makeText(this, "Este Qr Ja foi Lido", Toast.LENGTH_LONG).show();
                } else {
                    listaString.add(result.getContents());
                    Toast.makeText(this, "Valor do QR adicionado a lista", Toast.LENGTH_LONG).show();
                    quantidadeQR = Integer.valueOf(listaString.get(0).substring(3, 6));
                    textQRLidos.setText("Voce leu: " + listaString.size() + "      De: " + quantidadeQR);
                    if (listaString.size() == quantidadeQR) {
                        Collections.sort(listaString);
                        try {
                            montarImg();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {


                        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                        integrator.setDesiredBarcodeFormats();
                        integrator.initiateScan();
                    }
                }

            } else {
                Toast.makeText(this, "Falha na Leitura", Toast.LENGTH_SHORT).show();

                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats();
                integrator.initiateScan();
            }
        }
    }

    private String byteArrayToString(byte[] bytes) {
        StringBuilder retorno = new StringBuilder();
        char c;
        //String s = new String(bytes, StandardCharsets.ISO_8859_1);
        for(byte b : bytes){
                c = (char)b;
            retorno.append(c);
        }
        return retorno.toString();
    }

    private void montarImg() throws IOException, ClassNotFoundException {
        String string64Remontada = "";
        for (int i = 0; i < quantidadeQR; i++) {
            string64Remontada = string64Remontada + listaString.get(i).substring(6);
        }

        File newDir = new File(PATHFILE);
        newDir.mkdirs();
        String fotoname = "teste2.txt";
        File file = new File(newDir, fotoname);
        file.getParentFile().mkdirs();
        file.createNewFile();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bos.write(recuperarArrayByte(string64Remontada.substring(30)));
        bos.flush();
        bos.close();



    }
    private byte[] recuperarArrayByte(String g) {
        byte[] bytesRecuperados = new byte[g.length()];

        for(int i=0;i<g.length();i++){
            bytesRecuperados[i] = (byte)g.charAt(i);
        }

        //g.getBytes(StandardCharsets.ISO_8859_1);
        return bytesRecuperados;
    }

    public String getRealPathFromUri(Uri contentUri) {
        return URIUtil.getPath(MainActivity.this, contentUri);

    }

}
