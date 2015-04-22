package com.dquid.nfcget;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.util.Log;

public class NXPTagUtils {

	public enum Sector implements Byte_enum {
		Sector0((byte) 0x00), Sector1((byte) 0x01), Sector2((byte) 0x02), Sector3(
				(byte) 0x03);

		byte value;

		private Sector(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}
	}

    public interface WriteEepromCallback{
        void onBlockWritten(int blockIndex, int blocksToWrite);
    }

    WriteEepromCallback writeEepromCallback;

	Sector current_sec;

	NfcA nfca;
	byte[] command;
	byte[] answer;

	public NXPTagUtils(Tag tag, WriteEepromCallback writeEepromCallback) {
		nfca = NfcA.get(tag);
        this.writeEepromCallback = writeEepromCallback;
		try {
			nfca.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		current_sec = Sector.Sector0;
	}

	public void write_EEPROM(byte[] Data) throws IOException, FormatException {

		SectorSelect(Sector.Sector0);
		byte[] temp;
		int Index = 0;
		byte BlockNr = 4; // begin of user memory is page 0x04

		// write till all Data is written or 0xFF was written(BlockNr should be
		// 0 then, because of byte)
		for (Index = 0; Index < Data.length && BlockNr != 0; Index += 4) {
			temp = Arrays.copyOfRange(Data, Index, Index + 4);
			write(temp, BlockNr);
            writeEepromCallback.onBlockWritten(Index, Data.length);
			BlockNr++;
		}

		// If Data is left write to the 1 Sector
		if (Index < Data.length) {
			SectorSelect(Sector.Sector1);
			BlockNr = 0;

			for (; Index < Data.length; Index += 4) {
				temp = Arrays.copyOfRange(Data, Index, Index + 4);
				write(temp, BlockNr);
                writeEepromCallback.onBlockWritten(Index, Data.length);
				BlockNr++;
			}
		}
	}

	public void SectorSelect(Sector sector) throws IOException, FormatException {

		// When card is allredy in this sector do nothing
		if (current_sec == sector)
			return;

		// no answer
		answer = new byte[0];

		command = new byte[2];
		command[0] = (byte) 0xc2;
		command[1] = (byte) 0xff;

		nfca.transceive(command);

		command = new byte[4];
		command[0] = (byte) sector.getValue();
		command[1] = (byte) 0x00;
		command[2] = (byte) 0x00;
		command[3] = (byte) 0x00;

		nfca.setTimeout(1);

		// catch exception, passiv ack
		try {
			nfca.transceive(command);
		} catch (IOException e) {
		}
		nfca.setTimeout(600);
		current_sec = sector;

	}

	public void write(byte[] Data, byte BlockNr) throws IOException,
			FormatException {

		// no answer
		answer = new byte[0];

		command = new byte[6];
		command[0] = (byte) 0xA2;
		command[1] = BlockNr;
		command[2] = Data[0];
		command[3] = Data[1];
		command[4] = Data[2];
		command[5] = Data[3];
		/*byte[] res = */nfca.transceive(command);
//		Log.d("WRITE BLOCK", "Block Number: " + BlockNr);
	}

	public byte[] createRawNdefMessage(String text)
			throws UnsupportedEncodingException {
		// creating NDEF
		NdefMessage NDEFmessage = createNdefMessage(text);

		byte[] Ndef_message_byte = NDEFmessage.toByteArray();
		int ndef_message_size = Ndef_message_byte.length;
		byte[] message;

		if (ndef_message_size < 0xFF) {
			message = new byte[ndef_message_size + 3];
			byte TLV_size = 0;
			TLV_size = (byte) ndef_message_size;
			message[0] = (byte) 0x03;
			message[1] = (byte) TLV_size;
			message[message.length - 1] = (byte) 0xFE;
			System.arraycopy(Ndef_message_byte, 0, message, 2,
					Ndef_message_byte.length);
		} else {
			message = new byte[ndef_message_size + 5];
			int TLV_size = ndef_message_size;
			TLV_size |= 0xFF0000;
			message[0] = (byte) 0x03;
			message[1] = (byte) ((TLV_size >> 16) & 0xFF);
			message[2] = (byte) ((TLV_size >> 8) & 0xFF);
			message[3] = (byte) (TLV_size & 0xFF);
			message[message.length - 1] = (byte) 0xFE;
			System.arraycopy(Ndef_message_byte, 0, message, 4,
					Ndef_message_byte.length);
		}

		return message;
	}

	public byte[] createRawNdefMessage(byte[] textBytes, int len)
			throws UnsupportedEncodingException {
		// creating NDEF
		NdefMessage NDEFmessage = createNdefMessage(textBytes, len);

		byte[] Ndef_message_byte = NDEFmessage.toByteArray();
		int ndef_message_size = Ndef_message_byte.length;
		byte[] message;

		if (ndef_message_size < 0xFF) {
			message = new byte[ndef_message_size + 3];
			byte TLV_size = 0;
			TLV_size = (byte) ndef_message_size;
			message[0] = (byte) 0x03;
			message[1] = (byte) TLV_size;
			message[message.length - 1] = (byte) 0xFE;
			System.arraycopy(Ndef_message_byte, 0, message, 2,
					Ndef_message_byte.length);
		} else {
			message = new byte[ndef_message_size + 5];
			int TLV_size = ndef_message_size;
			TLV_size |= 0xFF0000;
			message[0] = (byte) 0x03;
			message[1] = (byte) ((TLV_size >> 16) & 0xFF);
			message[2] = (byte) ((TLV_size >> 8) & 0xFF);
			message[3] = (byte) (TLV_size & 0xFF);
			message[message.length - 1] = (byte) 0xFE;
			System.arraycopy(Ndef_message_byte, 0, message, 4,
					Ndef_message_byte.length);
		}

		return message;
	}

	public byte[] createRawNdefMessage(NdefMessage ndef)
			throws UnsupportedEncodingException {
		// creating NDEF
		NdefMessage NDEFmessage = ndef;
		byte[] Ndef_message_byte = NDEFmessage.toByteArray();
		int ndef_message_size = Ndef_message_byte.length;
		byte[] message;

		if (ndef_message_size < 0xFF) {
			message = new byte[ndef_message_size + 3];
			byte TLV_size = 0;
			TLV_size = (byte) ndef_message_size;
			message[0] = (byte) 0x03;
			message[1] = (byte) TLV_size;
			message[message.length - 1] = (byte) 0xFE;
			System.arraycopy(Ndef_message_byte, 0, message, 2,
					Ndef_message_byte.length);
		} else {
			message = new byte[ndef_message_size + 5];
			int TLV_size = ndef_message_size;
			TLV_size |= 0xFF0000;
			message[0] = (byte) 0x03;
			message[1] = (byte) ((TLV_size >> 16) & 0xFF);
			message[2] = (byte) ((TLV_size >> 8) & 0xFF);
			message[3] = (byte) (TLV_size & 0xFF);
			message[message.length - 1] = (byte) 0xFE;
			System.arraycopy(Ndef_message_byte, 0, message, 4,
					Ndef_message_byte.length);
		}

		return message;
	}

	private NdefMessage createNdefMessage(String text)
			throws UnsupportedEncodingException {
		String lang = "en";
		// String lang = "";
		byte[] textBytes = text.getBytes("US-ASCII");
		byte[] langBytes = lang.getBytes("US-ASCII");
		int langLength = langBytes.length;
		int textLength = textBytes.length;
		byte[] payload = new byte[1 + langLength + textLength];
		payload[0] = (byte) langLength;
		System.arraycopy(langBytes, 0, payload, 1, langLength);
		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_TEXT, new byte[0], payload);

		NdefRecord[] records = { record };
		NdefMessage message = new NdefMessage(records);

		return message;
	}

	private NdefMessage createNdefMessage(byte[] textBytes, int textLength)
			throws UnsupportedEncodingException {
		String lang = "en";
		byte[] langBytes = lang.getBytes("US-ASCII");
		int langLength = langBytes.length;
		byte[] payload = new byte[1 + langLength + textLength];
		payload[0] = (byte) langLength;
		System.arraycopy(langBytes, 0, payload, 1, langLength);
		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_TEXT, new byte[0], payload);

		NdefRecord[] records = { record };
		NdefMessage message = new NdefMessage(records);

		return message;
	}

	public void nfca_fast_read(byte StartAddr, byte EndAddr)
			throws IOException, FormatException {
		byte[] TxBuffer = new byte[3];
		byte[] RxBuffer = new byte[8];
		
		TxBuffer[0] = (byte) 0x3A;
		TxBuffer[1] = (byte) StartAddr;
		TxBuffer[2] = (byte) EndAddr;

		if (nfca != null) {
			RxBuffer = nfca.transceive(TxBuffer);

			for(byte b : RxBuffer)
				Log.d("nfca_fast_read", "" + b);
		}
	}

}
