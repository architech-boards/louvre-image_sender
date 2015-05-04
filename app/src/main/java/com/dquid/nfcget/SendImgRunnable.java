package com.dquid.nfcget;

import android.nfc.FormatException;
import android.nfc.Tag;

import java.io.IOException;

/**
 * SendImgRunnable.java
 *
 * Purpose: wrap the logic of sending img via NFC inside a different thread with possibility
 * to track the progress of the operation
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class SendImgRunnable implements Runnable
{
    public interface SendImgCallback{
        void onImgSent();
        void onBoardScreenUpdateProgress(int currProgress, int totProgress);
        void onBoardScreenUpdated();
        void onSentErr(Exception exc);
    }


    Tag tag;
    byte[] pixels;
    int pixelsLen;
    SendImgCallback sendImgCallback;
    NXPTagUtils.WriteEepromCallback writeEepromCallback;

    final int BOARD_SCREEN_UPDATE_SEC = 26;


    public SendImgRunnable(Tag tag, byte[] pixels, int pixelsLen, SendImgCallback sendImgCallback,
                           NXPTagUtils.WriteEepromCallback writeEepromCallback)
    {
        this.tag = tag;
        this.pixels = pixels;
        this.pixelsLen = pixelsLen;
        this.sendImgCallback = sendImgCallback;
        this.writeEepromCallback = writeEepromCallback;
    }


    @Override
    public void run()
    {
        if(tag ==null){
            return;
        }

//    	try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

        NXPTagUtils ntag = new NXPTagUtils(tag, writeEepromCallback);
        byte[] ndef_message_bytes = null;

        try {
            ndef_message_bytes = ntag.createRawNdefMessage(pixels, pixelsLen);
            ntag.write_EEPROM(ndef_message_bytes);

        } catch (Exception e) {
            e.printStackTrace();
            sendImgCallback.onSentErr(e);
            return;
        }

        try {
            ntag.nfca_fast_read((byte) 0x10, (byte) 0x11);
        } catch (IOException e) {
            e.printStackTrace();
            sendImgCallback.onSentErr(e);
        } catch (FormatException e) {
            e.printStackTrace();
            sendImgCallback.onSentErr(e);
        }

        sendImgCallback.onImgSent();

        try
        {
            for(int i = 0; i < BOARD_SCREEN_UPDATE_SEC; i++){
                sendImgCallback.onBoardScreenUpdateProgress(i, BOARD_SCREEN_UPDATE_SEC);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
            sendImgCallback.onSentErr(e);
        }

        sendImgCallback.onBoardScreenUpdated();
    }
}
