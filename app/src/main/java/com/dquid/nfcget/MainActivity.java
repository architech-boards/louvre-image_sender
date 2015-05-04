package com.dquid.nfcget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dquid.nfcget.util.Screen;


public class MainActivity extends Activity implements NXPTagUtils.WriteEepromCallback,
                                                      SendImgRunnable.SendImgCallback
{
	String TAG = "myTag";
	byte[] pixels;
	int pixelsLen;
	NdefMessage msg;
	
	NfcAdapter adapter;
	PendingIntent pendingIntent;
	IntentFilter writeTagFilters[];
	boolean writeMode;
    boolean isActivityResumed;
	Context ctx;
	Tag tag;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		// Check for available NFC Adapter
        adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter == null) {
        	this.showLongToast("NFC is not available");
            finish();
            return;
        }

		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
		IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		ndefDetected.addCategory(Intent.CATEGORY_DEFAULT);
		writeTagFilters = new IntentFilter[] { tagDetected, ndefDetected };


        // Select one of the image as default
        getBytesFromMonochromeBitmap(R.drawable.logo_rsr);
        msg = new NdefMessage(
                new NdefRecord[] {
                        new NdefRecord( NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], pixels)
                }
        );
		
		
//		Button resetButton = (Button) findViewById(R.id.resetButton);
//		resetButton.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				NXPTagUtils ntu = null;
//				
//				if(tag !=null){
//					ntu = new NXPTagUtils(tag);
//					try {
//						ntu.write(new byte[]{ (byte)0x03, (byte)0x00, (byte)0xFE, (byte)0x00 }, (byte) 0x04);
//						ntu.write(new byte[]{ (byte)0xE1, (byte)0x10, (byte)0xFE, (byte)0x00 }, (byte) 0x03);
//						
//					} catch (IOException e) {
//						e.printStackTrace();
//					} catch (FormatException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		});

        final ImageButton rsrButton = (ImageButton) findViewById(R.id.imageButton1);
		final ImageButton nxpButton = (ImageButton) findViewById(R.id.imageButton2);
		final ImageButton silicaButton = (ImageButton) findViewById(R.id.imageButton3);
        rsrButton.setSelected(true);
        nxpButton.setSelected(false);
        silicaButton.setSelected(false);


        rsrButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                rsrButton.setSelected(true);
                nxpButton.setSelected(false);
                silicaButton.setSelected(false);

                getBytesFromMonochromeBitmap(R.drawable.logo_rsr);
                msg = new NdefMessage(
                        new NdefRecord[] {
                                new NdefRecord( NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], pixels)
                        }
                );

            }
        });

		nxpButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				rsrButton.setSelected(false);
                nxpButton.setSelected(true);
				silicaButton.setSelected(false);
				
				getBytesFromMonochromeBitmap(R.drawable.nxp);
				msg = new NdefMessage(
	        			new NdefRecord[] {
	        					new NdefRecord( NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], pixels)
	        			}
	        	);
	        	
			}
		});

		silicaButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				rsrButton.setSelected(false);
                nxpButton.setSelected(false);
				silicaButton.setSelected(true);
				
				getBytesFromMonochromeBitmap(R.drawable.silica);
				msg = new NdefMessage(
	        			new NdefRecord[] {
	        					new NdefRecord( NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], pixels)
	        			}
	        	);
				
			}
		});

        // Block with an alert dialog in case NFC not present or not enabled
        checkNfcEnabled();

//		startActivity(new Intent(this, SplashActivity.class));
	}

    private void checkNfcEnabled()
    {
        NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("NFC not present or not enabled")
                    .setTitle("NFC Warning");

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
    @Override
    public void onNewIntent(Intent intent) {
    	Log.d(TAG, "onNewIntent");
     
        tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        
//        if(tag!=null){
//        	this.showLongToast("NDef Tag Detected: " + tag);
//    	}
    	
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
        	Log.d(TAG, intent.getAction());
        	 
        	// Write tag - NXP Way
        	if(tag !=null)
				scriAlberto();
             
        } else  if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
        	Log.d(TAG, "ACTION_TECH_DISCOVERED");
        	
        }

    }
	
    
    private void showLongToast(final String msg){
    	Log.d(TAG, msg);
    	
    	runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
		    	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}
		});
    }
    
    private void showShortToast(final String msg){
    	Log.d(TAG, msg);
    	
    	runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
		    	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			}
		});
    }

    
    private void scriAlberto(){
//    	if(tag ==null){
//    		return;
//    	}
//
////    	try {
////			Thread.sleep(10000);
////		} catch (InterruptedException e) {
////			e.printStackTrace();
////		}
//
//
//    	NXPTagUtils ntag = new NXPTagUtils(tag, this);
//    	byte[] ndef_message_bytes = null;
//
//    	try {
//			ndef_message_bytes = ntag.createRawNdefMessage(pixels, pixelsLen);
//			ntag.write_EEPROM(ndef_message_bytes);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//            this.showShortToast("Error occurred: " + e.getMessage());
//			return;
//		}
//
//    	try {
//    		ntag.nfca_fast_read((byte) 0x10, (byte) 0x11);
//		} catch (IOException e) {
//			e.printStackTrace();
//			this.showShortToast("Error occurred: " + e.getMessage());
//		} catch (FormatException e) {
//			e.printStackTrace();
//			this.showShortToast("Error occurred: " + e.getMessage());
//		}
//
//
//		this.showShortToast("Tag Written");

        Screen.keepScreenOn(getWindow());
        new Thread(new SendImgRunnable(tag, pixels, pixelsLen, this, this)).start();
    }
    
    
	@Override
	public void onPause(){
		super.onPause();
    	Log.d(TAG, "onPause");
        isActivityResumed = false;

        WriteModeOff(false);

        Screen.releaseScreenOn(getWindow());
	}
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - action: " + getIntent().getAction());
        isActivityResumed = true;
		WriteModeOn(false);
    }


	byte[] getBytesFromMonochromeBitmap(int resid){
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), resid);
		StringBuffer str;
        int intVal;
		byte byteVal;
		int h = 0, w = 0, i=0;
		int imageWidth = bmp.getWidth();
		int adjustedImageWidth = imageWidth%8==0 ? imageWidth : (imageWidth + (8-(imageWidth%8)));
		int imageHeight = bmp.getHeight();
		pixels = new byte[(imageWidth * imageHeight)];
		
		int[] pixelBytes = new int[8];
		Log.d(TAG, "image width: " + imageWidth + " and height: " + imageHeight );
		Log.d(TAG, "adjusted image width: " + adjustedImageWidth );
		
		for(h=0;h<imageHeight;h++){
			for(w=0;w<adjustedImageWidth;){
				str = new StringBuffer();
				pixelBytes[0] = w<imageWidth ?  bmp.getPixel(w, h) : Color.WHITE; w++;
				pixelBytes[1] = w<imageWidth ?  bmp.getPixel(w, h) : Color.WHITE; w++;
				pixelBytes[2] = w<imageWidth ?  bmp.getPixel(w, h) : Color.WHITE; w++;
				pixelBytes[3] = w<imageWidth ?  bmp.getPixel(w, h) : Color.WHITE; w++;
				pixelBytes[4] = w<imageWidth ?  bmp.getPixel(w, h) : Color.WHITE; w++;
				pixelBytes[5] = w<imageWidth ?  bmp.getPixel(w, h) : Color.WHITE; w++;
				pixelBytes[6] = w<imageWidth ?  bmp.getPixel(w, h) : Color.WHITE; w++;
				pixelBytes[7] = w<imageWidth ?  bmp.getPixel(w, h) : Color.WHITE; w++;
				
				str.append( pixelBytes[0] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[1] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[2] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[3] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[4] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[5] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[6] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[7] == Color.BLACK ? '0':'1');
				
				intVal = Integer.parseInt(str.toString(), 2);
				byteVal = (byte) intVal;
				//Log.d(TAG, "i: " + i + " - " + Integer.toHexString(intVal)  + " - w: " + w + " - h: " + h);
				pixels[i++] = byteVal;
				
			}
		}
		
		pixelsLen = i;
//		Log.d(TAG, "h: " + h + " - w: " + w + " - i: " + i);

		
		return pixels;
	}
	
	String getStringFromMonochromeBitmap(int resid){
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), resid);
		StringBuffer str = new StringBuffer();
		StringBuffer out = new StringBuffer();
		int intVal;
//		byte byteVal;
		int h = 0, w = 0/*, i=0*/;
		int imageWidth = bmp.getWidth();
		int imageHeight = bmp.getHeight();
//		byte[] pixels = new byte[(bmp.getHeight() * bmp.getWidth())/8];
		
		int[] pixelBytes = new int[8];
				
		for(h=0;h<imageHeight;h++){
			for(w=0;w<imageWidth;){
				str = new StringBuffer();
				pixelBytes[0] = w<imageWidth ?  bmp.getPixel(w, h) : Color.BLACK; w++;
				pixelBytes[1] = w<imageWidth ?  bmp.getPixel(w, h) : Color.BLACK; w++;
				pixelBytes[2] = w<imageWidth ?  bmp.getPixel(w, h) : Color.BLACK; w++;
				pixelBytes[3] = w<imageWidth ?  bmp.getPixel(w, h) : Color.BLACK; w++;
				pixelBytes[4] = w<imageWidth ?  bmp.getPixel(w, h) : Color.BLACK; w++;
				pixelBytes[5] = w<imageWidth ?  bmp.getPixel(w, h) : Color.BLACK; w++;
				pixelBytes[6] = w<imageWidth ?  bmp.getPixel(w, h) : Color.BLACK; w++;
				pixelBytes[7] = w<imageWidth ?  bmp.getPixel(w, h) : Color.BLACK; w++;
				
				str.append( pixelBytes[0] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[1] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[2] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[3] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[4] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[5] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[6] == Color.BLACK ? '0':'1');
				str.append( pixelBytes[7] == Color.BLACK ? '0':'1');
				
				intVal = Integer.parseInt(str.toString(), 2);
//				byteVal = (byte) intVal;
//				out.append((char)intVal);
				out.append(Character.toChars(intVal));
//				Log.d(TAG, "i: " + i + " - " + Integer.toHexString(intVal) );
//				pixels[i++] = byteVal;
				
			}
		}
		
//		Log.d(TAG, "h: " + h + " - w: " + w + " - i: " + i);
		
		return out.toString();
	}


	private void WriteModeOn(boolean forceMainThread)
    {
        if(isActivityResumed) // Foreground dispatched can be activated only from resumed activity
        {
            if(forceMainThread)
            {
                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
                final Activity mainActivity = this;
                mainHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        adapter.enableForegroundDispatch(mainActivity, pendingIntent, writeTagFilters, null);
                    }
                });
            }
            else{
                adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
            }
            writeMode = true;
        }
	}


    private void WriteModeOff(boolean forceMainThread)
    {
        if(forceMainThread)
        {
            // Get a handler that can be used to post to the main thread
            Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
            final Activity mainActivity = this;
            mainHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    adapter.disableForegroundDispatch(mainActivity);
                }
            });
        }
        else{
            adapter.disableForegroundDispatch(this);
        }
        writeMode = false;
	}

    @Override
    public void onBlockWritten(final int blockIndex, final int blocksToWrite)
    {
        final LinearLayout comLayout = (LinearLayout)findViewById(R.id.comInfoLayout);

        comLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                ProgressBar comProgressBar = (ProgressBar)findViewById(R.id.comProgressBar);
                TextView comInfoText = (TextView)findViewById(R.id.comInfoText);

                if(comLayout.getVisibility() == View.GONE){
                    comProgressBar.setProgress(0);
                    comProgressBar.setIndeterminate(false);
//                    comProgressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.LightGreen),
//                            PorterDuff.Mode.DARKEN);

                    comLayout.setVisibility(View.VISIBLE);
                    TextView comInfoHeaderText = (TextView)findViewById(R.id.comInfoHeaderText);
                    comInfoHeaderText.setText(R.string.com_in_progress);
                    comInfoHeaderText.setVisibility(View.VISIBLE);
                }

                comProgressBar.setMax(blocksToWrite);
                if(blockIndex > blocksToWrite){
                    comProgressBar.setSecondaryProgress(blocksToWrite);
                }
                else{
                    comProgressBar.setSecondaryProgress(blockIndex);
                }
                comInfoText.setText("Block " + blockIndex + " of " + blocksToWrite);
            }
        });
    }

    @Override
    public void onImgSent()
    {
        WriteModeOff(true);

        final LinearLayout comLayout = (LinearLayout)findViewById(R.id.comInfoLayout);

        comLayout.post(new Runnable()
        {
            @Override
            public void run()
            {

                TextView comInfoHeaderText = (TextView)findViewById(R.id.comInfoHeaderText);
                TextView comInfoText = (TextView)findViewById(R.id.comInfoText);

                comInfoHeaderText.setText("Waiting for board's screen update ...");
                comInfoText.setText("Keep the phone on the tag");
            }
        });
    }

    @Override
    public void onBoardScreenUpdateProgress(final int currProgress, final int totProgress)
    {
        final ProgressBar comProgressBar = (ProgressBar)findViewById(R.id.comProgressBar);

        comProgressBar.post(new Runnable()
        {
            @Override
            public void run()
            {
                comProgressBar.setMax(totProgress);
                if(currProgress > totProgress){
                    comProgressBar.setProgress(totProgress);
                }
                else{
                    comProgressBar.setProgress(currProgress);
                }
            }
        });
    }

    @Override
    public void onBoardScreenUpdated()
    {
        WriteModeOn(true);

        this.showLongToast("The board's screen should be updated, please remove the phone");

        final LinearLayout comLayout = (LinearLayout)findViewById(R.id.comInfoLayout);

        comLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                comLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onSentErr(Exception exc)
    {
        WriteModeOn(true);

        this.showLongToast("Error occurred: " + exc.getMessage());

        final LinearLayout comLayout = (LinearLayout)findViewById(R.id.comInfoLayout);

        comLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                comLayout.setVisibility(View.GONE);
            }
        });
    }
}
