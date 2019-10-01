package br.iesb.scanqrvideocode.model;

import java.util.ArrayList;

import static br.iesb.scanqrvideocode.constants.Contantes.CAPACIDADEQR;
import static br.iesb.scanqrvideocode.constants.Contantes.HEADER;
import static br.iesb.scanqrvideocode.constants.Contantes.TAM_NOME_ARQUIVO;

public class ArquivoString {
    private ArrayList<String> arrayString = new ArrayList<String>();
    private int quantidadeQRGerado;
    private int quantidadeQRLido;
    private ArrayList<String> arrayLido = new ArrayList<>();

    public ArquivoString() {
    }

    public ArquivoString(String s) {
        dividirString(s);
    }

    private void dividirString(String s) {
        quantidadeQRGerado = (int) s.length() / CAPACIDADEQR;
        quantidadeQRGerado += ((s.length() % CAPACIDADEQR > 0) ? 1 : 0);
        String x;
        String y;
        int contador = 0;
        for (int i = 0; i < quantidadeQRGerado; i++) {
            x = "00" + i;
            x = x.substring(x.length() - 3);

            y = "00" + quantidadeQRGerado;
            y = y.substring(y.length() - 3);

            if (x.equals("000")) {
                if (contador + CAPACIDADEQR + TAM_NOME_ARQUIVO > s.length()) {
                    arrayString.add(x + y + "555555555555555555555555555555" + s.substring(contador));
                } else {
                    arrayString.add(x + y + "555555555555555555555555555555" + s.substring(contador, contador + CAPACIDADEQR - TAM_NOME_ARQUIVO));
                    contador += CAPACIDADEQR + TAM_NOME_ARQUIVO;
                }
            } else {
                if (contador + CAPACIDADEQR > s.length()) {
                    arrayString.add(x + y + s.substring(contador));
                } else {
                    arrayString.add(x + y + s.substring(contador, contador + CAPACIDADEQR));
                    contador += CAPACIDADEQR;
                }
            }
        }

    }


    public int getQuantidadeQRLido() {
        return quantidadeQRLido;
    }

    public void setQuantidadeQRLido(int quantidadeQRLido) {
        this.quantidadeQRLido = quantidadeQRLido;
    }

    public ArrayList<String> getArrayLido() {
        return arrayLido;
    }

    public void setArrayLido(ArrayList<String> arrayLido) {
        this.arrayLido = arrayLido;
    }

    public void setQuantidadeQRGerado(int quantidadeQRGerado) {
        this.quantidadeQRGerado = quantidadeQRGerado;
    }

    public int getQuantidadeQRGerado() {
        return quantidadeQRGerado;
    }

    public ArrayList<String> getArrayString() {
        return arrayString;
    }

    public void setArrayString(ArrayList<String> arrayString) {
        this.arrayString = arrayString;
    }

}
