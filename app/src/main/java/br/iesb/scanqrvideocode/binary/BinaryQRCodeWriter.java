package br.iesb.scanqrvideocode.binary;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;
import java.util.Map;
import br.iesb.scanqrvideocode.encoder.Encoder;

/**
 * This object renders a QR Code as a BitMatrix 2D array of greyscale values.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class BinaryQRCodeWriter {

  private static final int QUIET_ZONE_SIZE = 4;

  private int horizontalAligment = 0;
  private int verticalAligment = 0;
  
  public void setAligment(int horizontal, int vertical) {
	  this.horizontalAligment = horizontal;
	  this.verticalAligment = vertical;
  }
  
  public BinaryQRCodeWriter() {
  }

  public BinaryQRCodeWriter(int horizontal, int vertical) {
	  this.horizontalAligment = horizontal;
	  this.verticalAligment = vertical;
  }

  public BitMatrix encode(byte[] contents, BarcodeFormat format, int width, int height)
      throws WriterException {

    return encode(contents, format, width, height, null);
  }

  public BitMatrix encode(byte[] contents,
                          BarcodeFormat format,
                          int width,
                          int height,
                          Map<EncodeHintType,?> hints) throws WriterException {


    if (format != BarcodeFormat.QR_CODE) {
      throw new IllegalArgumentException("Can only encode QR_CODE, but got " + format);
    }

    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Requested dimensions are too small: " + width + 'x' +
          height);
    }

    ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.L;
    if (hints != null) {
      ErrorCorrectionLevel requestedECLevel = (ErrorCorrectionLevel) hints.get(EncodeHintType.ERROR_CORRECTION);
      if (requestedECLevel != null) {
        errorCorrectionLevel = requestedECLevel;
      }
    }

    QRCode code = Encoder.encode(contents, errorCorrectionLevel, hints);
    return renderResult(code, width, height);
  }

  // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
  // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
  private BitMatrix renderResult(QRCode code, int width, int height) {
    ByteMatrix input = code.getMatrix();
    if (input == null) {
      throw new IllegalStateException();
    }
    int inputWidth = input.getWidth();
    int inputHeight = input.getHeight();
    int qrWidth = inputWidth + (QUIET_ZONE_SIZE << 1);
    int qrHeight = inputHeight + (QUIET_ZONE_SIZE << 1);
    int outputWidth = Math.max(width, qrWidth);
    int outputHeight = Math.max(height, qrHeight);

    int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
    // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
    // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
    // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
    // handle all the padding from 100x100 (the actual QR) up to 200x160.
    int leftPadding;
    if(horizontalAligment == -1) { // left
        leftPadding = QUIET_ZONE_SIZE;
    } else if(horizontalAligment == 1) { // right
        leftPadding = (outputWidth - (inputWidth * multiple)) - QUIET_ZONE_SIZE;
    } else {
    	// center
        leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
    }
    int topPadding;
    if(verticalAligment == -1) { // up
        topPadding = QUIET_ZONE_SIZE;
    } else if(verticalAligment == 1) { // down
        topPadding = (outputHeight - (inputHeight * multiple)) - QUIET_ZONE_SIZE;
    } else {
    	// center
        topPadding = (outputHeight - (inputHeight * multiple)) / 2;
    }
    
    BitMatrix output = new BitMatrix(outputWidth, outputHeight);

    for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
      // Write the contents of this row of the barcode
      for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
        if (input.get(inputX, inputY) == 1) {
          output.setRegion(outputX, outputY, multiple, multiple);
        }
      }
    }

    return output;
  }

}
