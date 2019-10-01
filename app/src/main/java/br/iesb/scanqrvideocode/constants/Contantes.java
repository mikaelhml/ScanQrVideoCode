package br.iesb.scanqrvideocode.constants;

import android.os.Environment;

public class Contantes {

    public static final int CAPACIDADE_POR_NIVEL_QR = 53;

    public static final int HEADER = 6;

    public static final int TAM_NOME_ARQUIVO = 30;

    public static final int CAPACIDADEQR = CAPACIDADE_POR_NIVEL_QR - HEADER ;

    public static final String PATHFILE = "/storage/emulated/0/Download/";

    public static final String FOLDER_APP = Environment.getExternalStorageDirectory().getAbsolutePath() +PATHFILE;




}
