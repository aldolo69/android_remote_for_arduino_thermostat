package ab.thermosetup;

import java.util.Timer;
import java.util.TimerTask;

import ab.thermosetup.R;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class Blinker {

	Activity act = null;
	View viewBackground = null;// used to show yellow back
	ImageView viewLamp = null;// used to show lamp
	TextView textViewCounter = null;// used to show counter

	MediaPlayer mPlayer = null;

	Timer timer;// timer used to run the task
	TimerTask timerTask;// timer task
	Handler mHandlerLight;// handler used by task to talk to main thread

	Timer timerCountDown;// timer used to run the task
	TimerTask timerTaskCountDown;// timer task
	Handler mHandlerCountDown;// handler used by task to talk to main thread

	boolean bBlack = true;// present state of the screen
	String strMsg = null;// message to be sent
	int iStrIndex = 0;// char in process
	int byteMsg;// char in process
	int iStatus = -1;// 0=stop,1=begin of tx,2=tx ,3=eot
	// bot=10 bit 0 + 1
	// eof=9 bit 0
	int iCounter = 0;//
	int iBit = -1;// -1=do nothing.0=0,1=1 first step,2=1second step
	int iBitPerChar = 8;
	int iBytePerChar = 1;// 2 to join 2 hex digit in a 8 bit number

	// 0 bit is sent with a short deltaT, 1 bit is sent with a long
	// deltaT

	Camera cam = null;
	Parameters p = null;

	void showLamp() {
		if (bBlack == false) {
			// viewBackground.setBackgroundColor(act.getResources().getColor(
			// android.R.color.holo_orange_light));
			viewLamp.setImageDrawable(act.getResources().getDrawable(
					R.drawable.light_bulb_white));
			// Settings.System.putInt(act.getContentResolver(),
			// Settings.System.SCREEN_BRIGHTNESS, 255);

			if (cam == null) {
				cam = Camera.open();
				p = cam.getParameters();
				cam.startPreview();
			}

			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			cam.setParameters(p);

		} else {
			// viewBackground.setBackgroundColor(act.getResources().getColor(
			// android.R.color.black));
			viewLamp.setImageDrawable(act.getResources().getDrawable(
					R.drawable.light_bulb_black));
			// Settings.System.putInt(act.getContentResolver(),
			// Settings.System.SCREEN_BRIGHTNESS, 1);

			if (cam != null) {
				p.setFlashMode(Parameters.FLASH_MODE_OFF);
				cam.setParameters(p);
			}

		}
	}

	int charToBin(char byteMsg) {
		if (byteMsg >= '0' && byteMsg <= '9') {
			byteMsg -= '0';
		}
		if (byteMsg >= 'A' && byteMsg <= 'F') {
			byteMsg -= 'A' - 10;
		}
		if (byteMsg >= 'a' && byteMsg <= 'f') {
			byteMsg -= 'a' - 10;
		}
		return byteMsg;
	}

	void initblinker(int bpc, // 4 or 8
			int Bpc, // 1 or 2 to join 2 hex digit
			Activity activit, View back, ImageView lamp, TextView count) {
		iBitPerChar = bpc;
		iBytePerChar = Bpc;

		act = activit;
		viewBackground = back;// used to show yellow back
		viewLamp = lamp;// used to show lamp
		textViewCounter = count;// used to show counter

		mPlayer = MediaPlayer.create(act, R.raw.tick);

		mHandlerCountDown = new Handler() {
			public void handleMessage(Message msg) {
				iCounter--;
				if (iCounter != 0) {
					mPlayer.start();// sound
					textViewCounter.setText((new Integer(iCounter)).toString());
				} else {
					textViewCounter.setText("");

					timerCountDown.cancel();
					timerTaskCountDown.cancel();
					timerTaskCountDown = null;
					timerCountDown = null;
					startTimerTransmission();
				}

			}
		};

		mHandlerLight = new Handler() {
			public void handleMessage(Message msg) {
				// will land here 2 times per DeltaTime. if the bit
				// we are sending is zero then toggle, otherwise
				// wait for the next
				switch (iBit) {
				case -1:// do nothing and go to check next bit
					break;
				case 0:// toggle and go to check next bit
						// Log.w("LightTalk", "0");
					bBlack = !bBlack;
					showLamp();
					break;
				case 2:// toggle and go to check next bit
					bBlack = !bBlack;
					showLamp();
					break;
				case 1:// do nothing and exit!!!
						// Log.w("LightTalk", "1");
					iBit = 2;
					return;// n
				}

				// now decide next bit
				switch (iStatus) {
				case 0:// start trasmission
					bBlack = true;
					showLamp();
					iStrIndex = 0;
					iStatus = 1;
					iCounter = 0;
					iBit = 0;
					return;
				case 1:// send start of transmission
					iBit = 0;
					iCounter++;
					// usually keep 0. last tick send 1
					if (iCounter == 15) {
						iBit = 1;
						iStatus = 2;
						iCounter = 0;
						return;
					}
					return;
				case 2:// send data
					if (iCounter == iBitPerChar) {
						iCounter = 0;
						iBit = 1;
						return;// send end of byte
					}
					if (iCounter == 0) {
						// first bit of the new byte

						// eostring?
						if (iStrIndex == strMsg.length()) {
							iBit = 0;
							iStatus = 3;
							iCounter = 0;
							return;
						}

						char charMsg = strMsg.charAt(iStrIndex);
						// only numeric string?

						if (iBitPerChar == 4) {
							byteMsg = (int) (charMsg);
							byteMsg -= 48;// ascii for '0'
							//you can only send 0..9 digits
						}
						if (iBitPerChar == 8) {
							if (iBytePerChar == 1) {
								byteMsg = (int) (charMsg);
								//never tested this option....
							} else {

								byteMsg = charToBin(charMsg);
								byteMsg<<=4;
								iStrIndex++;
								charMsg = strMsg.charAt(iStrIndex);
								byteMsg |= charToBin( charMsg );
							}
						}

						iStrIndex++;
					}

					if ((byteMsg & (1 << iCounter)) > 0) {
						iBit = 1;
					} else {
						iBit = 0;
					}

					// Log.w("LightTalk", "Sent " + new
					// Integer(iBit).toString());

					iCounter++;
					return;
				case 3:// send eot
					iBit = 0;
					iCounter++;
					if (iCounter == 11) {
						iBit = -1;
						iStatus = -1;//stop everything

						// viewBackground.setBackgroundColor(act.getResources().getColor(
						// android.R.color.darker_gray));
						viewLamp.setImageDrawable(act.getResources()
								.getDrawable(R.drawable.light_bulb_transparent));

						// Settings.System.putInt(act.getContentResolver(),
						// Settings.System.SCREEN_BRIGHTNESS, 255);

						if (cam != null) {
							p.setFlashMode(Parameters.FLASH_MODE_OFF);
							cam.setParameters(p);

							cam.stopPreview();
							cam.release();
							cam = null;
						}

						act.getWindow().clearFlags(
								WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
						timer.cancel();
						timerTask.cancel();
						timerTask = null;
						timer = null;
						return;
					}
					return;
				}

			}
		};

	}

	void startTimerTransmission() {
		// strMsg = "11111";//editText.getText().toString();
		iStrIndex = 0;

		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
		timerTask = new TimerTask() {
			public void run() {
				mHandlerLight.obtainMessage(1).sendToTarget();
			}
		};

		timer = new Timer();
		timer.schedule(timerTask, 100, 100); //
		act.getWindow()
				.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}


	
//call this method to start transmission. method exit if something already
	//running
	void startTimerCountDown(String imp) {

		if (iStatus != -1)
			return;


		iStatus = 0;// 0=stop,1=begin of tx,2=tx 0,3=eot

		
		
		// Settings.System.putInt(act.getContentResolver(),
		// Settings.System.SCREEN_BRIGHTNESS_MODE,
		// Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL); //this will set the
		// manual mode (set the automatic mode off)

		iCounter = 3;
		strMsg = imp;

		if (timerCountDown != null) {
			timerCountDown.cancel();
			timerCountDown = null;
		}
		if (timerTaskCountDown != null) {
			timerTaskCountDown.cancel();
			timerTaskCountDown = null;
		}
		timerTaskCountDown = new TimerTask() {
			public void run() {
				mHandlerCountDown.obtainMessage(1).sendToTarget();
			}
		};

		timerCountDown = new Timer();
		timerCountDown.schedule(timerTaskCountDown, 1000, 1000); //
	}

}
