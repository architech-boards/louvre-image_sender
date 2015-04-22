package com.dquid.nfcget;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.dquid.nfcget.util.SystemUiHider;


//TODO sistemare immagini (ora sono molto sgranate)

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class SplashActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
//				startActivity(new Intent(SplashActivity.this, MainActivity.class));
				finish();
			}
		}, 2000);
	}
	
}
