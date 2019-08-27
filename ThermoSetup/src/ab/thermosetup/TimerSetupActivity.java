package ab.thermosetup;

//protocollo lighttalk
//
//1bit 	7bit	8bit	8bit	8bit... fino a 4 coppie di start/stop
//0     	lmmgvsd	start	stop
//
//
//1bit	2bit	5bit
//1	00	xxxxx	temperatura giorno
//1	01	xxxxx	temperatura notte
//1	10	xxxxx	temperatura viaggio
//

//1bit	2bit	5bit	8bit		8bit		8bit
//1	11	00000	MMyyyyyy	ddddhhhh 	MMmmmmmm

import java.util.Calendar;
import java.util.TimeZone;

import ab.thermosetup.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class TimerSetupActivity extends Activity implements
		SeekBar.OnSeekBarChangeListener {

	TextView textView = null;
	Blinker bl = null;
	SeekBar sb=null;
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		bl = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_timersetup);

		View viewBackground = null;
		ImageView viewLamp = null;
		TextView textViewCounter = null;

		viewBackground = this.findViewById(R.id.linearLayout1);
		viewLamp = (ImageView) this.findViewById(R.id.imagelampTimer);
		textView = (TextView) this.findViewById(R.id.editTexttemp);
		textViewCounter = (TextView) this.findViewById(R.id.textViewCounter1);

	  sb = (SeekBar) this.findViewById(R.id.seekBarTEMP);
		sb.setOnSeekBarChangeListener(this);

		bl = new Blinker();
		bl.initblinker(8, 2, this, viewBackground, viewLamp, textViewCounter);

	}


	
	

	public void buttonTDAY(View view) {
		//1bit	2bit	5bit
		//1	00	xxxxx	temperatura giorno
		//1	01	xxxxx	temperatura notte
		//1	10	xxxxx	temperatura viaggio
		String output = 
				 String.format(
						"%02x",128+sb.getProgress());

		bl.startTimerCountDown(output);
	}
	public void buttonTNIGHT(View view) {
		String output = 
				 String.format(
						"%02x",128+32+sb.getProgress());

		bl.startTimerCountDown(output);
	}
	public void buttonTLEAVE(View view) {
		String output = 
				 String.format(
						"%02x",128+64+sb.getProgress());

		bl.startTimerCountDown(output);
	}
	
	public void buttonYYMMDDHHMM(View view) {
		// 1bit 2bit 5bit 8bit 8bit 8bit
		// 1 11 000dh MMyyyyyy ddddhhhh MMmmmmmm

		Calendar cal = Calendar.getInstance(TimeZone.getDefault());


		String output = String.format("%02x", 128 + 64 + 32+
				((cal.get(Calendar.DAY_OF_MONTH)&16)!=0?2:0) +
				((cal.get(Calendar.HOUR_OF_DAY)&16)!=0?1:0)
				)
				+ String.format(
						"%02x",
						(((1 + cal.get(Calendar.MONTH))) & (8 + 4)) * 16
								+ (cal.get(Calendar.YEAR) - 2000))
				+ String.format("%02x", (cal.get(Calendar.DAY_OF_MONTH)&15) * 32
						+ (cal.get(Calendar.HOUR_OF_DAY))&15)
				+ String.format(
						"%02x",
						(((1 + cal.get(Calendar.MONTH))) & (2 + 1)) * 64
								+ cal.get(Calendar.MINUTE));

		bl.startTimerCountDown(output);
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		textView.setText(String.format("%d", progress));

	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}

}
