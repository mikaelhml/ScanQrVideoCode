/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.iesb.scanqrvideocode.encoder;

final class BlockPair {

  private final byte[] dataBytes;
  private final byte[] errorCorrectionBytes;

  BlockPair(byte[] data, byte[] errorCorrection) {
    dataBytes = data;
    errorCorrectionBytes = errorCorrection;
  }

  public byte[] getDataBytes() {
    return dataBytes;
  }

  public byte[] getErrorCorrectionBytes() {
    return errorCorrectionBytes;
  }

}
