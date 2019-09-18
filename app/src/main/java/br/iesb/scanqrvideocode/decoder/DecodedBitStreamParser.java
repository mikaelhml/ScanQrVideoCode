package br.iesb.scanqrvideocode.decoder;
/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.common.BitSource;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.decoder.Mode;
import com.google.zxing.qrcode.decoder.Version;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>QR Codes byte encoded.</p>
 *
 * @author Sean Owen
 * 
 */
public final class DecodedBitStreamParser {

	private DecodedBitStreamParser() {
	}

	public static DecoderResult decode(byte[] bytes, Version version) throws FormatException {
		return decode(bytes, version, null, null);
	}

	public static DecoderResult decode(byte[] bytes, Version version, ErrorCorrectionLevel ecLevel, Map<DecodeHintType, ?> hints) throws FormatException {
		BitSource bits = new BitSource(bytes);
		
		List<byte[]> byteSegments = new ArrayList<byte[]>(1);
		Mode mode;
		do {
			// While still another segment to read...
			if (bits.available() < 4) {
				// OK, assume we're done. Really, a TERMINATOR mode should have been recorded here
				mode = Mode.TERMINATOR;
			} else {
					mode = Mode.BYTE; // mode is encoded by 4 bits
			}
			if (mode != Mode.TERMINATOR) {
				// "Normal" QR code modes:
				// How many characters will follow, encoded in this mode?
				int count = bits.readBits(mode.getCharacterCountBits(version));
				if (mode == Mode.BYTE) {
					byteSegments.add(decodeByteSegment(bits, count));
				} else {
					throw FormatException.getFormatInstance();
				}
			}
		} while (mode != Mode.TERMINATOR);

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		for(byte[] bs : byteSegments) {
			try {
				bout.write(bs);
			} catch (IOException e) {
				throw new RuntimeException();
			}
		}

		DecoderResult result = new DecoderResult(bout.toByteArray(), "", null, null);
		
		return result;
	}

	private static byte[] decodeByteSegment(BitSource bits, int count) throws FormatException {
		// Don't crash trying to read more bits than we have available.
		if (count << 3 > bits.available()) {
			throw FormatException.getFormatInstance();
		}

		byte[] readBytes = new byte[count];
		for (int i = 0; i < count; i++) {
			readBytes[i] = (byte) bits.readBits(8);
		}

		return readBytes;
	}

}
