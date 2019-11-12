package br.iesb.scanqrvideocode.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import br.iesb.scanqrvideocode.R;
import br.iesb.scanqrvideocode.encoder.URIUtil;

import static br.iesb.scanqrvideocode.constants.Contantes.PATHFILE;

public class MainActivity extends AppCompatActivity {

    private static final char[] TABELA_CORRECAO = {
            'X', 'ﾉ', 'P', '�', 'ￇ','¾','ÿ'
    };
    private static final char[] TABELA_DECODE = {
            '0', '1', '2', '3', '4','5','6'
    };
    private static final int[] ALPHANUMERIC_TABLE = {
            -119, 80
    };
    private final ArrayList<Character> correcaoTabela = new ArrayList<>();
    private final ArrayList<Character> decodeTabela = new ArrayList<>();

    Button btnScanQRCode, btnCriarQRCode;
    private ImageView imageView;
    private TextView textQRLidos;
    private Integer quantidadeQR;
    private Chronometer simpleChronometer;
    private long inicio =0,fim=0;
    private Date date;
    private ArrayList<String> listaString = new ArrayList<>();
    IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);


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
                simpleChronometer.start();

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
        simpleChronometer = (Chronometer) findViewById(R.id.simpleChronometer);
        for (char c : TABELA_CORRECAO) {
            correcaoTabela.add(c);
        }
        for (char c : TABELA_DECODE) {
            decodeTabela.add(c);
        }
    }

    @SuppressLint("StaticFieldLeak")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 111) && (resultCode == RESULT_OK)) {
            Uri uri = data.getData();
            Uri uri2 = Uri.parse(getRealPathFromUri(uri));

            File file = new File(uri2.getPath());
            String arquivoCodificado = "";

            try {

                FileInputStream inputStream = new FileInputStream(file);
                String nomeArquivo = getFileName(uri2);
                String extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1);
                if (extensao.equalsIgnoreCase("JPEG") || extensao.equalsIgnoreCase("JPG") || extensao.equalsIgnoreCase("PNG")) {
                    arquivoCodificado = codificaImagem(inputStream);
                    nomeArquivo = nomeArquivo + arquivoCodificado.substring(0, 8);
                    arquivoCodificado = arquivoCodificado.substring(8);
                }
                if (extensao.equalsIgnoreCase("TXT")) {
                    int size = (int) file.length();
                    byte[] bytes = new byte[size];
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                    arquivoCodificado = byteArrayToString(bytes);

                }
                //montarImg(arquivoCodificado);
                Intent i = new Intent(MainActivity.this, GerarQrs.class);
                i.putExtra("string64", arquivoCodificado);
                i.putExtra("nomeArquivo", nomeArquivo);
                startActivity(i);
                finish();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Está vazio", Toast.LENGTH_LONG).show();
            }
            if (!Objects.equals(result.getContents(), "")) {
                if (listaString.contains(result.getContents())) {
                    integrator.initiateScan();
                    Toast.makeText(this, "Voce leu: "+ listaString.size() + "De: " + quantidadeQR, Toast.LENGTH_LONG).show();
                } else {
                    if(listaString.size()<1){
                        inicio = System.nanoTime();
                    }
                    listaString.add(result.getContents());
                    Toast.makeText(this, "Valor do QR adicionado a lista", Toast.LENGTH_LONG).show();
                    quantidadeQR = Integer.valueOf(listaString.get(0).substring(3, 6));
                    textQRLidos.setText("Voce leu: " + listaString.size() + "      De: " + quantidadeQR);



                    if (listaString.size() == quantidadeQR) {
                        fim = System.nanoTime();
                        long duration = fim - inicio;
                        Toast.makeText(MainActivity.this,"Tempo: "+duration/1000000,Toast.LENGTH_LONG).show();
                        simpleChronometer.stop();
                        Collections.sort(listaString);
                        try {
                            montarImg();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        integrator.initiateScan();
                    }
                }

            } else {
                integrator.initiateScan();
                Toast.makeText(this, "Falha na Leitura", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String codificaImagem(FileInputStream inputStream) {
        Bitmap b = BitmapFactory.decodeStream(inputStream);
        int[] pixels = new int[b.getHeight() * b.getWidth()];
        b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        String largura = "000" + b.getWidth();
        largura = largura.substring(largura.length() - 4);
        String altura = "000" + b.getHeight();
        altura = altura.substring(altura.length() - 4);
        byte[] bytes = recuperarBytes(pixels);
        return largura + altura + getArrayByte(pixels);
    }

    private byte[] recuperarBytes(int[] pixels) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for(int i:pixels){
            byte[] bytes = ByteBuffer.allocate(4).putInt(i).array();
            try {
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return output.toByteArray();
    }

    private String getArrayByte(int[] pixels) {
        StringBuilder pixeis = new StringBuilder();
        StringBuilder correcoes = new StringBuilder();
        int contador = 0, index = 0;
        for (int pixel : pixels) {

            if(pixel == -1){
                pixeis.append("XB");
            }
            else if(pixel == -3584){
                pixeis.append("XY");
            }
            else {
                char alpha = (char) Color.alpha(pixel);
                char red = (char) Color.red(pixel);
                char green = (char) Color.green(pixel);
                char blue = (char) Color.blue(pixel);
                pixeis.append(alpha);
                pixeis.append(red);
                pixeis.append(green);
                pixeis.append(blue);
            }
        }
        //String contadorS = "00" + contador;
        //contadorS = contadorS.substring(contadorS.length() - 3);
        //recuperarArrayByte(pixeis.toString());
        //return contadorS + correcoes + pixeis.toString();
        return pixeis.toString();
    }

    private int[] getPixelsFromByteArray(String encoded,int tamanho) {
        int[] pixels = new int[tamanho];
        if(encoded.length()!=tamanho*4){
            int diferenca = (tamanho*4) - encoded.length();
            StringBuilder encodedBuilder = new StringBuilder(encoded);
            for(int i = 0; i<diferenca; i++){
                encodedBuilder.append("ÿ");
            }
            encoded = encodedBuilder.toString();
        }
        int i=0;
        //int result = ByteBuffer.wrap(bytes).getInt();
        for (int index = 0; index < pixels.length; index++) {

            int alpha = encoded.charAt(i);
            int red = encoded.charAt(i + 1);
            int green = encoded.charAt(i + 2);
            int blue = encoded.charAt(i + 3);
            if(alpha>255){
                alpha = 255;
            }
            if(red>255){
                red = 255;
            }
            if(green>255){
                green = 255;
            }
            if(blue>255){
                blue = 255;
            }
            pixels[index]  = (alpha & 0xff) << 24 | (red & 0xff) << 16 | (green & 0xff) << 8 | (blue & 0xff);
            i +=4;
        }

        return pixels;
    }

    private String byteArrayToString(byte[] bytes) {
        StringBuilder retorno = new StringBuilder();
        StringBuilder correcoes = new StringBuilder();
        char c;
        //int contador = 0, index = 0;
        for (byte b : bytes) {
            /*if (correcaoTabela.contains((char) b)) {
                retorno.append('X');
                index = correcaoTabela.indexOf((char) b);
                correcoes.append(decodeTabela.get(index));
                contador++;
            } else {
             */
                c = (char) b;
                retorno.append(c);
            //}
        }

        //String contadorS = "00" + contador;
        //contadorS = contadorS.substring(contadorS.length() - 3);
        //return contadorS + correcoes + retorno.toString();


       /* String k ="";
        for(int i=0;i<255;i++){
            char b = (char) ((byte) i &0xff);
            k = i + "- "+b ;
            retorno.append(k);
        }
        */
        return retorno.toString();
    }


    private void montarImg() throws IOException, ClassNotFoundException {
        String string64Remontada = "";
        for (int i = 0; i < quantidadeQR; i++) {
            string64Remontada = string64Remontada + listaString.get(i).substring(6);
        }

        int tamanhoNomeArquivo = Integer.parseInt(string64Remontada.substring(0, 3));
        String fotoname = string64Remontada.substring(3, tamanhoNomeArquivo + 3);
        String extensao = fotoname.substring(fotoname.lastIndexOf('.') + 1,fotoname.lastIndexOf('.') + 4);
        string64Remontada = string64Remontada.substring(tamanhoNomeArquivo + 3);
        //string64Remontada = aplicaFiltroCorrecao(string64Remontada);
        if (extensao.equalsIgnoreCase("JPEG") || extensao.equalsIgnoreCase("JPG") || extensao.equalsIgnoreCase("PNG")) {
            int largura = Integer.parseInt(fotoname.substring(fotoname.length()-8,fotoname.length()-4));
            int altura = Integer.parseInt(fotoname.substring(fotoname.length()-4));
            fotoname = fotoname.substring(0,fotoname.length()-8);
            Bitmap bitmap =decodificaImg(string64Remontada,largura,altura);
            salvarImg(bitmap,fotoname);
        }

        if(extensao.equalsIgnoreCase("TXT")){
            File newDir = new File(PATHFILE);
            newDir.mkdirs();
            File file = new File(newDir, fotoname);
            file.getParentFile().mkdirs();
            file.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            byte[] bytes = recuperarArrayByte(string64Remontada);
            bos.write(bytes);
            bos.flush();
            bos.close();
        }
    }

    private void salvarImg(Bitmap bitmap, String fotoname) {

        try {
            File newDir = new File(PATHFILE);
            newDir.mkdirs();
            File file = new File(newDir, fotoname);
            file.getParentFile().mkdirs();
            file.createNewFile();
            BufferedOutputStream bos = null;
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String aplicaFiltroCorrecao(String string64Remontada) {
        //StringBuilder retorno = new StringBuilder();
        String stringCorrigida = string64Remontada.replaceAll("XY","ÿ\u0000òÿ");
        stringCorrigida = stringCorrigida.replaceAll("XB", "ÿÿÿÿ");
        /*int quantidadeCorrecoes = Integer.parseInt(stringCorrigida.substring(0, 3)), index = 0;
        String correcoes = stringCorrigida.substring(3, 3 + quantidadeCorrecoes);
        stringCorrigida = stringCorrigida.substring(3 + correcoes.length());
        for (int i = 0; i < stringCorrigida.length(); i++) {
            if (stringCorrigida.charAt(i) == 'X') {
                index = decodeTabela.indexOf(correcoes.charAt(0));
                correcoes = correcoes.substring(1);
                retorno.append(correcaoTabela.get(index));
            } else {
                retorno.append(stringCorrigida.charAt(i));
            }
        }
        */

        //recuperarArrayByte(retorno.toString());
        /*recuperarArrayByte(string64Remontada);
        String stringCorrigida = string64Remontada.replaceAll("XY","ÿ\u0000òÿ");
        stringCorrigida = stringCorrigida.replaceAll("XB", "ÿÿÿÿ");
        recuperarArrayByte(stringCorrigida);*/
        return stringCorrigida;
    }

    private byte[] recuperarArrayByte(String g) {
        byte[] bytesRecuperados = new byte[g.length()];

        for (int i = 0; i < g.length(); i++) {
            bytesRecuperados[i] = (byte) g.charAt(i) ;
        }
        return bytesRecuperados;
    }

    public String getRealPathFromUri(Uri contentUri) {
        return URIUtil.getPath(MainActivity.this, contentUri);

    }

    public String getFileName(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public Bitmap decodificaImg(String encoded, int largura, int altura) {
        int[] pixels = getPixelsFromByteArray(encoded,largura*altura);
        Bitmap bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, largura, 0, 0, largura, altura);
        return bitmap;

    }
}
