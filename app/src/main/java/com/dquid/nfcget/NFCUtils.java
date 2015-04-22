package com.dquid.nfcget;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.util.Log;

public class NFCUtils {
	static String TAG = "NFCUtils";
	
	public static List<String> parseNdefMsgs(Intent intent) {
    	Log.d(TAG, "processIntent");
    	NdefMessage[] msgs;
    	ArrayList<String> recordStrings = new ArrayList<String> ();
    	
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
                for(final NdefRecord ndr : msgs[i].getRecords()){
                	recordStrings.add(getStringFromNdefText(ndr.getPayload()));
                }
                
            }
        }
        
        return recordStrings;
    }
    
    
	public static void writeMifareUltralightTag(Tag tag, byte[] dataArray) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        byte[] dataPage;
        int i, j, k;
        try {
            ultralight.connect();
            
            for(i = 0, k=0; i<dataArray.length;k++){
            	dataPage = new byte[4];
            	for(j=0;j<4;j++){
            		if(i>=dataArray.length)
            			dataPage[j] = (byte)0x00;
            		else
            			dataPage[j] = dataArray[i++];
            	}
            		
            	Log.d(TAG, "k: " + k);
            	ultralight.writePage(k, dataPage);
            }
            
        } catch (IOException e) {
            Log.e(TAG, "IOException while closing MifareUltralight...", e);
        } finally {
            try {
                ultralight.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight...", e);
            }
        }
        return;
    }
	
	
	public static void writeMifareUltralightTag_Test(Tag tag) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            ultralight.writePage(4, "abcd".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(5, "efgh".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(6, "ijkl".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(7, "mnop".getBytes(Charset.forName("US-ASCII")));
        } catch (IOException e) {
            Log.e(TAG, "IOException while closing MifareUltralight...", e);
        } finally {
            try {
                ultralight.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight...", e);
            }
        }
        return;
    }

    
    public static String readMifareUltralightTag(Tag tag) {
    	MifareUltralight mifare = MifareUltralight.get(tag);
    	String value = new String();
    	
        try {
            mifare.connect();
            final byte[] payload = mifare.readPages(4);
            
            value = new String(payload, Charset.forName("US-ASCII"));
            
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing MifareUltralight message...", e);
        } finally {
            if (mifare != null) {
               try {
                   mifare.close(); 
               }
               catch (IOException e) {
                   Log.e(TAG, "Error closing tag...", e);
               }
            }
        }
        return value;
    }
    
    
    public static void formatTagAsNdef(Tag tag) {
    	NdefFormatable ndef = NdefFormatable.get(tag);
    	NdefMessage msg = new NdefMessage(new NdefRecord[] {createTextRecord("max_culo", true)});
    	
        try {
        	ndef.connect();
        	ndef.format(msg);
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing formatTagasNdef...", e);
        } catch (FormatException e) {
        	Log.e(TAG, "IOException while writing formatTagasNdef...", e);
		} finally {
            try {
            	ndef.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing formatTagasNdef...", e);
            }
        }
        return;
    }
    
    
    @SuppressLint("NewApi")
	public static Boolean writeNdefToTag(Tag tag, NdefMessage msg) {
        Ndef ndef = Ndef.get(tag);
        
//        int size = ndef.getMaxSize();
        try {
        	ndef.connect();
//        	if(msg.getByteArrayLength()>size){
//        		Log.e(TAG, "Cannot write ndef tag.. message is too big... [max size is " + size + " Bytes - message length is " + msg.getByteArrayLength() + " Bytes]");
//        		return;
//        	}

        	ndef.writeNdefMessage(msg);
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing Ndef...", e);
            return false;
        } catch (FormatException e) {
        	Log.e(TAG, "IOException while writing Ndef...", e);
            return false;
		} finally {
            try {
            	ndef.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing Ndef...", e);
                return false;
            }
        }
        
        return true;
    }
    
    @SuppressLint("NewApi")
	public static void writeNdefTag_Test(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        NdefMessage msg = new NdefMessage(new NdefRecord[] {createTextRecord("max_culo", true)});
        
        int size = ndef.getMaxSize();
        try {
        	ndef.connect();
        	if(msg.getByteArrayLength()>size){
        		Log.e(TAG, "Cannot write ndef tag.. message is too big...");
        		return;
        	}
        		
        	ndef.writeNdefMessage(msg);
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing Ndef...", e);
        } catch (FormatException e) {
        	Log.e(TAG, "IOException while writing Ndef...", e);
		} finally {
            try {
            	ndef.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing Ndef...", e);
            }
        }
        
        return;
    }
    
    
    @SuppressLint("NewApi")
	public static void writeNdefTagMultipleTimes_Test(Tag tag, int times) {
        Ndef ndef = Ndef.get(tag);
        
        for (int i = 0; i < times; i++) {
	        NdefMessage msg = new NdefMessage(new NdefRecord[] {createTextRecord("max_culo"+i, true)});
	        
	        int size = ndef.getMaxSize();
	        try {
	        	ndef.connect();
	        	if(msg.getByteArrayLength()>size){
	        		Log.e(TAG, "Cannot write ndef tag.. message is too big...");
	        		return;
	        	}
	        	
	        	ndef.writeNdefMessage(msg);
	        	
	        } catch (IOException e) {
	            Log.e(TAG, "IOException while writing Ndef...", e);
	        } catch (FormatException e) {
	        	Log.e(TAG, "IOException while writing Ndef...", e);
			} finally {
	            try {
	            	ndef.close();
	            	Log.e(TAG, "just wrote 'max_culo"+i+"' - sleeping 1000ms...");
	            	Thread.sleep(1000);
	            } catch (IOException e) {
	                Log.e(TAG, "IOException while closing Ndef...", e);
	            } catch (InterruptedException e) {
	            	Log.e(TAG, "IOException while writing Ndef...", e);
				}
	        }
        
        }
        return;
    }
    
    
    @SuppressLint("NewApi")
	public static void writeNdefTagMultipleTimes_noDisconection_Test(Tag tag, int times) {
        Ndef ndef = Ndef.get(tag);
        NdefMessage msg = new NdefMessage(new NdefRecord[] {createTextRecord("max_culo", true)});
        
        int size = ndef.getMaxSize();
        try {
        	ndef.connect();
        	if(msg.getByteArrayLength()>size){
        		Log.e(TAG, "Cannot write ndef tag.. message is too big...");
        		return;
        	}
        		
        	for (int i = 0; i < times; i++) {
        		msg = new NdefMessage(new NdefRecord[] {createTextRecord("max_culo"+i, true)});
        		ndef.writeNdefMessage(msg);
        		Log.e(TAG, "just wrote 'max_culo"+i+"' - sleeping 500ms...");
        		Thread.sleep(2000);
			}
        	ndef.writeNdefMessage(msg);
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing Ndef...", e);
        } catch (FormatException e) {
        	Log.e(TAG, "IOException while writing Ndef...", e);
		} catch (InterruptedException e) {
        	Log.e(TAG, "IOException while writing Ndef...", e);
		} finally {
            try {
            	ndef.close();
            	
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing Ndef...", e);
            }
		}
        return;
    }
    
    public static NdefMessage readNdefTag(Tag tag) {
    	Ndef ndef = Ndef.get(tag);
//    	ArrayList<String> recordStrings = new ArrayList<String> ();
    	NdefMessage ndefMsg = null;
    	
        try {
        	ndef.connect();
            ndefMsg = ndef.getNdefMessage();
           
//            for(final NdefRecord rec : ndefMsg.getRecords()){
//            	recordStrings.add(new String(getStringFromNdefText(rec.getPayload())));
//            }
            
            
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing Ndef message...", e);
        } catch (FormatException e) {
        	Log.e(TAG, "IOException while writing Ndef message...", e);
		} finally {
            if (ndef != null) {
               try {
            	   ndef.close(); 
               }
               catch (IOException e) {
                   Log.e(TAG, "Error closing tag...", e);
               }
            }
        }
        return ndefMsg;
    }
    
    
    public static NdefRecord createTextRecord(String payload,  boolean encodeInUtf8) {
        byte[] langBytes = Locale.getDefault().getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
        NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
        
//        <intent-filter>
//        <action android:name="android.nfc.action.NDEF_DISCOVERED" />
//        <category android:name="android.intent.category.DEFAULT" />
//        <data android:mimeType="text/plain" />
//    </intent-filter>
    }
    
    
    
    public static NdefRecord createTextRecordFromBytes(byte[] payload) {
        NdefRecord record = new NdefRecord(
        		NdefRecord.TNF_WELL_KNOWN,
        		NdefRecord.RTD_TEXT, 
        		new byte[0], 
        		payload);
        return record;
        
//        <intent-filter>
//        <action android:name="android.nfc.action.NDEF_DISCOVERED" />
//        <category android:name="android.intent.category.DEFAULT" />
//        <data android:mimeType="text/plain" />
//    </intent-filter>
    }
    
    
    public static NdefRecord createURIRecord(String absoluteUri) {
    	return new NdefRecord(
    		    NdefRecord.TNF_ABSOLUTE_URI ,
    		    absoluteUri.getBytes(Charset.forName("US-ASCII")),
    		    new byte[0], new byte[0]);
    	
    	
//    	<intent-filter>
//        <action android:name="android.nfc.action.NDEF_DISCOVERED" />
//        <category android:name="android.intent.category.DEFAULT" />
//        <data android:scheme="http"
//            android:host="developer.android.com"
//            android:pathPrefix="/index.html" />
//    </intent-filter>
    }
    
    
    public static NdefRecord createMIMERecord(String packageName) {
    	String s = "application/vnd."+packageName+"";
    	return new NdefRecord(
			    NdefRecord.TNF_MIME_MEDIA ,
			    s.getBytes(Charset.forName("US-ASCII")),
			    new byte[0], 
			    "hello there!".getBytes(Charset.forName("US-ASCII")));
    	
    	
//    	<intent-filter>
//        <action android:name="android.nfc.action.NDEF_DISCOVERED" />
//        <category android:name="android.intent.category.DEFAULT" />
//        <data android:mimeType="application/vnd.packageName" />
//    </intent-filter>
    }
    
    
    public static NdefRecord createAarRecord(String payload,  boolean encodeInUtf8) {
        return NdefRecord.createApplicationRecord("com.dquid.nfctest");
    }
    
    
    
    public static String getStringFromNdefText(byte[]payload){

        /*
		 * payload[0] contains the "Status Byte Encodings" field, per the NFC
		 * Forum "Text Record Type Definition" section 3.2.1.
		 * 
		 * bit7 is the Text Encoding Field.
		 * 
		 * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1): The
		 * text is encoded in UTF16
		 * 
		 * Bit_6 is reserved for future use and must be set to zero.
		 * 
		 * Bits 5 to 0 are the length of the IANA language code.
		 */

         //Get the Text Encoding
        String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";

        //Get the Language Code
        int languageCodeLength = payload[0] & 0077;
        try {
//			String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
			
			//Get the Text
	        String text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
	        
	        return text;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "ERROR";
		}

        
    }

}
