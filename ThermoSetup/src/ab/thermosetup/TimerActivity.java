package ab.thermosetup;

import java.util.Timer;
import java.util.TimerTask;

import ab.thermosetup.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

public class TimerActivity extends Activity implements OnClickListener,
		SeekBar.OnSeekBarChangeListener {

	boolean bBtnStatus[] = new boolean[8];
	TextView twOnOff[] = new TextView[8];
	SeekBar sbOnOff[] = new SeekBar[8];
	final static int maxslider = 96;

	Blinker bl = null;

	/*
	 * MediaPlayer mPlayer = null;
	 * 
	 * Timer timer;// timer used to run the task TimerTask timerTask;// timer
	 * task Handler mHandlerLight;// handler used by task to talk to main thread
	 * 
	 * Timer timerCountDown;// timer used to run the task TimerTask
	 * timerTaskCountDown;// timer task Handler mHandlerCountDown;// handler
	 * used by task to talk to main thread
	 * 
	 * boolean bBlack = true;// present state of the screen String strMsg =
	 * null;// message to be sent int iStrIndex = 0;// char in process byte
	 * byteMsg;// char in process int iStatus = 0;// 0=stop,1=begin of tx,2=tx
	 * ,3=eot // bot=10 bit 0 + 1 // eof=9 bit 0 int iCounter = 0;// int iBit =
	 * -1;// -1=do nothing.0=0,1=1 first step,2=1second step int iBitPerChar =
	 * 8;
	 * 
	 * // 0 bit is sent with a short deltaT, 1 bit is sent with a long // deltaT
	 * 
	 * void showLamp() { if (bBlack == false) {
	 * viewBackground.setBackgroundColor(getResources().getColor(
	 * android.R.color.holo_orange_light));
	 * viewLamp.setImageDrawable(getResources().getDrawable(
	 * R.drawable.light_bulb_white)); } else {
	 * viewBackground.setBackgroundColor(getResources().getColor(
	 * android.R.color.darker_gray));
	 * viewLamp.setImageDrawable(getResources().getDrawable(
	 * R.drawable.light_bulb_black)); } }
	 */
	String testoSlider(int i, int pos) {
		String onoff = ((i & 1) == 0) ? "ON__" : "OFF_";
		String timerCnt = new Integer(1 + (i / 2)).toString();
		String timerTime;
		if (pos == 0) {
			timerTime = "OFF";
		} else {
			pos--;
			// 96 steps for 15 minutes step=4 steps for hour
			int h = pos / 4;
			int m = 15 * (pos % 4);
			timerTime = String.format("%02d", h) + ":"
					+ String.format("%02d", m);

		}
		return onoff + timerCnt + " " + timerTime;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bl = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);

		View viewBackground = null;
		ImageView viewLamp = null;
		TextView textViewCounter = null;
		// LinearLayout llForeground=null;

		viewBackground = this.findViewById(R.id.linearLayout1);
		viewLamp = (ImageView) this.findViewById(R.id.imagelampTimer);
		textViewCounter = (TextView) this.findViewById(R.id.textViewCounter1);
		viewLamp.bringToFront();

		bl = new Blinker();
		bl.initblinker(8, 2, this, viewBackground, viewLamp, textViewCounter);

		/*
		 * mPlayer = MediaPlayer.create(this, R.raw.tick);
		 * 
		 * mHandlerCountDown = new Handler() { public void handleMessage(Message
		 * msg) { iCounter--; if (iCounter != 0) { mPlayer.start();// sound
		 * textViewCounter.setText((new Integer(iCounter)).toString()); } else {
		 * textViewCounter.setText("");
		 * 
		 * timerCountDown.cancel(); timerTaskCountDown.cancel();
		 * timerTaskCountDown = null; timerCountDown = null;
		 * startTimerTransmission(); }
		 * 
		 * } };
		 * 
		 * mHandlerLight = new Handler() { public void handleMessage(Message
		 * msg) { // will land here 2 times per DeltaTime. if the bit // we are
		 * sending is zero then toggle, otherwise // wait for the next switch
		 * (iBit) { case -1:// do nothing and go to check next bit break; case
		 * 0:// toggle and go to check next bit // Log.w("LightTalk", "0");
		 * bBlack = !bBlack; showLamp(); break; case 2:// toggle and go to check
		 * next bit bBlack = !bBlack; showLamp(); break; case 1:// do nothing
		 * and exit!!! // Log.w("LightTalk", "1"); iBit = 2; return;// n }
		 * 
		 * // now decide next bit switch (iStatus) { case 0:// start trasmission
		 * bBlack = true; showLamp(); iStrIndex = 0; iStatus = 1; iCounter = 0;
		 * iBit = 0; return; case 1:// send start of transmission iBit = 0;
		 * iCounter++; // usually keep 0. last tick send 1 if (iCounter == 15) {
		 * iBit = 1; iStatus = 2; iCounter = 0; return; } return; case 2:// send
		 * data if (iCounter == iBitPerChar) { iCounter = 0; iBit = 1; return;//
		 * send end of byte } if (iCounter == 0) { // first bit of the new byte
		 * 
		 * // eostring? if (iStrIndex == strMsg.length()) { iBit = 0; iStatus =
		 * 3; iCounter = 0; return; }
		 * 
		 * char charMsg = strMsg.charAt(iStrIndex); byteMsg = (byte) (charMsg &
		 * 0x00FF); //only numeric string?
		 * 
		 * byteMsg -= 48;// ascii for '0'
		 * 
		 * 
		 * // Log.w("LightTalk", new // Character(charMsg).toString());
		 * iStrIndex++; }
		 * 
		 * if ((byteMsg & (1 << iCounter)) > 0) { iBit = 1; } else { iBit = 0; }
		 * 
		 * // Log.w("LightTalk", "Sent " + new // Integer(iBit).toString());
		 * 
		 * iCounter++; return; case 3:// send eot iBit = 0; iCounter++; if
		 * (iCounter == 11) {
		 * 
		 * iBit = -1; bBlack = true; showLamp();
		 * viewLamp.setImageDrawable(getResources().getDrawable(
		 * R.drawable.light_bulb_transparent));
		 * 
		 * getWindow().clearFlags(
		 * WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		 * 
		 * timer.cancel(); timerTask.cancel(); timerTask = null; timer = null;
		 * return; } return; }
		 * 
		 * } };
		 */

		final LinearLayout lm = (LinearLayout) findViewById(R.id.linearLayout1);
		// llForeground = lm;
		// lm.bringToFront();
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		int dxButton = width / 7;
		final float scale = this.getResources().getDisplayMetrics().density;
		int dyTitlebar = (int) (48 * scale + 0.5f);
		int dyButton = dyTitlebar;
		int dyFont = (dyButton * 7) / 10;

		for (int iSlider = 0; iSlider < 8; iSlider++) {
			LinearLayout lls = new LinearLayout(this);
			lls.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams paramsLls = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			TextView tw = new TextView(this);
			tw.setId(iSlider + 100);
			tw.setWidth(width / 3);
			tw.setTextColor(0xff000000);
			tw.setText(testoSlider(iSlider, 0));
			tw.setTextSize(18);
			twOnOff[iSlider] = tw;

			SeekBar sb = new SeekBar(this);
			sb.setId(iSlider + 1000);
			sb.setMax(maxslider);
			sb.setProgress(0);
			sb.setLayoutParams(new LinearLayout.LayoutParams((width / 3) * 2,
					LayoutParams.WRAP_CONTENT, 1f));
			sb.setOnSeekBarChangeListener(this);
			sbOnOff[iSlider] = sb;

			lls.addView(tw);
			lls.addView(sb);
			lm.addView(lls, paramsLls);

		}

		TextView tw = new TextView(this);
		lm.addView(tw);

		// add a ll with 1 text and 7 buttons inside

		// int pixelSize = (int) (8 * scale + 0.5f);
		// int pixelSizeBtn = (int) (18 * scale + 0.5f);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 0);

		LinearLayout.LayoutParams paramsBtn = new LinearLayout.LayoutParams(
				dxButton, dyButton);
		paramsBtn.setMargins(0, 0, 0, 0);

		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.HORIZONTAL);
 
		String sDayOfWeek=this.getString(R.string.day_of_week);
		for (int i = 0; i < 7; i++) {
			final Button btnDay = new Button(this);
			btnDay.setId(1 + i);
			btnDay.setText(sDayOfWeek.substring(i, i + 1));
			btnDay.setTextSize(TypedValue.COMPLEX_UNIT_PX, dyFont);
			if (i >= 5) {
				btnDay.setTextColor(0xffff0000);
			} else {
				btnDay.setTextColor(0xff000000);
			}

			btnDay.setBackground(getResources().getDrawable(
					android.R.drawable.button_onoff_indicator_off));
			btnDay.setOnClickListener(this);
			bBtnStatus[i] = false;
			btnDay.setPadding(0, 0, 0, 0);
			ll.addView(btnDay, paramsBtn);
		}
		lm.addView(ll);
	}

	/*
	 * void startTimerTransmission() { strMsg = "123"; iStrIndex = 0; iStatus =
	 * 0;// 0=stop,1=begin of tx,2=tx 0,3=eot viewLamp.bringToFront(); if (timer
	 * != null) { timer.cancel(); timer = null; } if (timerTask != null) {
	 * timerTask.cancel(); timerTask = null; } timerTask = new TimerTask() {
	 * public void run() { mHandlerLight.obtainMessage(1).sendToTarget(); } };
	 * 
	 * timer = new Timer(); timer.schedule(timerTask, 100, 100); //
	 * getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); }
	 * 
	 * 
	 * void startTimerCountDown() { iCounter = 4;
	 * 
	 * if (timerCountDown != null) { timerCountDown.cancel(); timerCountDown =
	 * null; } if (timerTaskCountDown != null) { timerTaskCountDown.cancel();
	 * timerTaskCountDown = null; } timerTaskCountDown = new TimerTask() {
	 * public void run() { mHandlerCountDown.obtainMessage(1).sendToTarget(); }
	 * };
	 * 
	 * timerCountDown = new Timer(); timerCountDown.schedule(timerTaskCountDown,
	 * 1000, 1000); // }
	 */

	// 1bit 7bit 8bit 8bit 8bit... fino a 4 coppie di start/stop
	// 0 lmmgvsd start stop

	public void buttonsend(View view) {

		String output = String.format("%02x", (bBtnStatus[0] ? 64 : 0)
				+ (bBtnStatus[1] ? 32 : 0) + (bBtnStatus[2] ? 16 : 0)
				+ (bBtnStatus[3] ? 8 : 0) + (bBtnStatus[4] ? 4 : 0)
				+ (bBtnStatus[5] ? 2 : 0) + (bBtnStatus[6] ? 1 : 0));
		// 128+sb.getProgress());

		if (sbOnOff[0].getProgress() > 0) {
			output = output + String.format("%02x", sbOnOff[0].getProgress())
					+ String.format("%02x", sbOnOff[1].getProgress());
		}

		if (sbOnOff[2].getProgress() > 0) {
			output = output + String.format("%02x", sbOnOff[2].getProgress())
					+ String.format("%02x", sbOnOff[3].getProgress());
		}

		if (sbOnOff[4].getProgress() > 0) {
			output = output + String.format("%02x", sbOnOff[4].getProgress())
					+ String.format("%02x", sbOnOff[5].getProgress());
		}
		if (sbOnOff[6].getProgress() > 0) {
			output = output + String.format("%02x", sbOnOff[6].getProgress())
					+ String.format("%02x", sbOnOff[7].getProgress());
		}

		bl.startTimerCountDown(output);
		// if(sbOnOff[0].getProgress())

		// startTimerCountDown();
		/*
		 * View iview = this.getCurrentFocus(); if (iview != null) {
		 * InputMethodManager imm = (InputMethodManager)
		 * getSystemService(Context.INPUT_METHOD_SERVICE);
		 * imm.hideSoftInputFromWindow(iview.getWindowToken(), 0); }
		 */
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// se sto spostando un ON o OFF devo controllare che
		// il rispettivo controllo sia congruente e che il testo
		// sia riadattato
		int id = seekBar.getId() - 1000;

		// per pari devo controllare il dispari
		if ((id & 1) == 0) // pari
		{
			if (progress == maxslider) {
				sbOnOff[id].setProgress(maxslider - 1);
				progress = maxslider - 1;
			}

			if (sbOnOff[id + 1].getProgress() <= progress) {
				sbOnOff[id + 1].setProgress(progress + 1);
			}
			twOnOff[id].setText(testoSlider(id, progress));
			twOnOff[id + 1].setText(testoSlider(id + 1,
					sbOnOff[id + 1].getProgress()));
		}

		
		if ((id & 1) != 0) // dispari
		{
			if (progress == 0)
			{
				sbOnOff[id-1].setProgress(0);
				
			}
			if (progress == 1) {
				sbOnOff[id].setProgress(2);
				progress = 2;
			}

			if (sbOnOff[id - 1].getProgress() >= progress) {
				sbOnOff[id - 1].setProgress(progress - 1);
			}
			
			if (progress != 0&&sbOnOff[id - 1].getProgress()==0) {
				sbOnOff[id - 1].setProgress(1);
			}
			
			
			twOnOff[id].setText(testoSlider(id, progress));
			twOnOff[id - 1].setText(testoSlider(id - 1,
					sbOnOff[id - 1].getProgress()));
		}

		
		
		
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	public void onClick(View view) {
		int id = view.getId() - 1;
		if (id < 7 && id >= 0) {
			Drawable drwOff = getResources().getDrawable(
					android.R.drawable.button_onoff_indicator_off);
			Drawable drwOn = getResources().getDrawable(
					android.R.drawable.button_onoff_indicator_on);

			if (bBtnStatus[id] == false) {
				bBtnStatus[id] = true;
				view.setBackground(drwOn);
			} else {
				bBtnStatus[id] = false;
				view.setBackground(drwOff);
			}

		}

	}

}
