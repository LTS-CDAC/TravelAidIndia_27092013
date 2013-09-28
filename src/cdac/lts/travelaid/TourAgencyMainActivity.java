/* 
 * Copyright (C) 2013 by the Centre for Development of Advanced Computing Trivandrum
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cdac.lts.travelaid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import cdac.lts.govdatabase.TourAgencyDatabase;
import cdac.lts.shakedetection.ShakeDetection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.util.Linkify;

public class TourAgencyMainActivity extends Activity implements OnInitListener {

	final String LOGTAG = "TourAgencyMainActivity";
	ImageButton buttonSpeakState, buttonSpeakCity, buttonSpeakRegion;
	boolean buttonSpeakStateFlag = false;
	boolean buttonSpeakCityFlag = false;
	boolean buttonSpeakRegionFlag = false;

	ProgressDialog loadingFileProgress;

	AutoCompleteTextView autoCompleteTextviewState;

	AutoCompleteTextView autoCompleteTextviewCity;

	// AutoCompleteTextView autoCompleteTextviewRegion;

	short FOR_QUERY_CLICK_FLAG;
	

	// Default TTS engine
	public TextToSpeech textSpeech;
	boolean ttsFlag = false;
	
	final int ALERT_DIALOG_OPTIONS_ID_NOQUERYRESULT = 1;
	final int TTS_DATA_CHECK_CODE = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tour_agency_main);
		buttonSpeakState = (ImageButton) findViewById(R.id.button_speak_state);
		buttonSpeakState.setOnClickListener(speakStateContentListener);
		buttonSpeakCity = (ImageButton) findViewById(R.id.button_speak_city);
		buttonSpeakCity.setOnClickListener(speakCityContentListener);
		/*
		 * buttonSpeakRegion = (ImageButton)
		 * findViewById(R.id.button_speak_region);
		 * buttonSpeakRegion.setOnClickListener(speakRegionContentListener);
		 */
		checkVoiceRecognition();

		SharedPreferences SP = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (SP.getBoolean("key_preference_read_search_results", false)) {
			// TODO should be activated based on shared preferences
			ttsFlag = true;
			initializeTTS();
		}

		if (SP.getBoolean("Key_preference_stop_tts_on_shake", false)) {
			ShakeDetection sDetection = new ShakeDetection(textSpeech);
			sDetection.registerShakeSensor(getApplicationContext());
		}

		if (!getSharedPreferences(TOURAGENCY_DATABASE_PREF, 0).getBoolean(
				"first_start_flag", false)) {
			new SynchronizeDatabase().execute();
			activateFirstStart();
		} else {
			initialize();
		}

	}

	private void initializeTTS() {
		textSpeech = new TextToSpeech(getApplicationContext(), this);
		Intent findIntent = new Intent();
		findIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(findIntent, TTS_DATA_CHECK_CODE);
		
	}
		
	private void talk(String text) {
		textSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
	}

	/**
	 * Synchronize app database with website XML
	 */
	private class SynchronizeDatabase extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			loadingFileProgress.cancel();
			initialize();
			super.onPostExecute(result);
			try {
				this.finalize();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			loadingFileProgress = ProgressDialog
					.show(TourAgencyMainActivity.this, "",
							"Please wait..\nDatabase update on first start under progress..");
			super.onPreExecute();
		}

		void downloadXmlFromWeb() {

			String url = "https://datacms.nic.in/datatool/?url=http://data.gov.in//sites/default/files/TravelTourData.xls&format=xml";

			Utility.saveUrlAsFile(url, "TravelTourData.xml");
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO downloaad XML file
			// TODO check change is there in new download by hashing and then if
			// necessary update

			// TODO which table to synchronize
			final TourAgencyDatabase db = new TourAgencyDatabase(
					getApplicationContext());
			db.open();

			database2flag = true;

			try {

				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();

				DefaultHandler handler = new DefaultHandler() {

					public void startElement(String uri, String localName,
							String qName, Attributes attributes)
							throws SAXException {

						System.out.println("Start Element :" + qName);

						if (qName.contains("ROW")) {

							TravelToursEntity.rowid = qName;

						}
						/*
						 * if (database1flag) { if
						 * (qName.equalsIgnoreCase("HOTEL_NAME")) {
						 * database1.bhotelname = true; }
						 * 
						 * if (qName.equalsIgnoreCase("ADDRESS")) {
						 * database1.baddress = true; }
						 * 
						 * if (qName.equalsIgnoreCase("STATE")) {
						 * database1.bstate = true; }
						 * 
						 * if (qName.equalsIgnoreCase("PHONE")) {
						 * database1.bphone = true; }
						 * 
						 * if (qName.equalsIgnoreCase("FAX")) { database1.bfax =
						 * true; }
						 * 
						 * if (qName.equalsIgnoreCase("EMAIL_ID")) {
						 * database1.bemail_id = true; }
						 * 
						 * if (qName.equalsIgnoreCase("WEBSITE")) {
						 * database1.bwebsite = true; }
						 * 
						 * if (qName.equalsIgnoreCase("TYPE")) { database1.btype
						 * = true; }
						 * 
						 * if (qName.equalsIgnoreCase("ROOMS")) {
						 * database1.brooms = true; } }
						 */
						if (database2flag) {

							if (qName.equalsIgnoreCase("NAME_OF_AGENCY")) {
								database2.bnameofagency = true;
							}

							if (qName.equalsIgnoreCase("ADDRESS")) {
								database2.baddress = true;
							}

							if (qName.equalsIgnoreCase("PHONE")) {
								database2.bphone = true;
							}

							if (qName.equalsIgnoreCase("FAX")) {
								database2.bfax = true;
							}

							if (qName.equalsIgnoreCase("EMAIL")) {
								database2.bemail = true;
							}

							if (qName.equalsIgnoreCase("REGION")) {
								database2.bregion = true;
							}

							if (qName.equalsIgnoreCase("CITY")) {
								database2.bcity = true;
							}

							if (qName.equalsIgnoreCase("STATE")) {
								database2.bstate = true;
							}

							if (qName.equalsIgnoreCase("CONTACT_PERSON")) {
								database2.bcontactperson = true;
							}

							if (qName.equalsIgnoreCase("TYPE")) {
								database2.btype = true;
							}

						}

					}

					public void endElement(String uri, String localName,
							String qName) throws SAXException {

						System.out.println("End Element :" + qName);

						if (TravelToursEntity.rowid.equals(qName)) {

							if (BuildConfig.DEBUG) {
								Log.v(LOGTAG, "Its time to update...");
							}

							long id = db.insertTouristAgencyDetails(
									TravelToursEntity.rowid,
									TravelToursEntity.nameofagency,
									TravelToursEntity.address,
									TravelToursEntity.phone,
									TravelToursEntity.fax,
									TravelToursEntity.email,
									TravelToursEntity.region,
									TravelToursEntity.city.trim().toUpperCase(),
									TravelToursEntity.state.trim().toUpperCase(),
									TravelToursEntity.contactperson,
									TravelToursEntity.type);

							if (BuildConfig.DEBUG) {
								Log.v(LOGTAG, new Long(id).toString());
							}
						}

					}

					public void characters(char ch[], int start, int length)
							throws SAXException {

						/*
						 * if (database1flag) { if (database1.bhotelname) {
						 * 
						 * HotelToursEntity.hotelname = new String(ch, start,
						 * length); System.out.println("HOTEL_NAME : " +
						 * HotelToursEntity.hotelname); database1.bhotelname =
						 * false; }
						 * 
						 * if (database1.baddress) {
						 * 
						 * HotelToursEntity.address = new String(ch, start,
						 * length); System.out.println("ADDRESS : " +
						 * HotelToursEntity.address); database1.baddress =
						 * false; }
						 * 
						 * if (database1.bstate) { HotelToursEntity.state = new
						 * String(ch, start, length);
						 * System.out.println("STATE : " +
						 * HotelToursEntity.state); database1.bstate = false; }
						 * 
						 * if (database1.bphone) { HotelToursEntity.phone = new
						 * String(ch, start, length);
						 * System.out.println("PHONE : " +
						 * HotelToursEntity.phone); database1.bphone = false; }
						 * 
						 * if (database1.bfax) {
						 * 
						 * HotelToursEntity.fax = new String(ch, start, length);
						 * System.out.println("FAX : " + HotelToursEntity.fax);
						 * database1.bfax = false; }
						 * 
						 * if (database1.bwebsite) {
						 * 
						 * HotelToursEntity.website = new String(ch, start,
						 * length); System.out.println("WEBSITE : " +
						 * HotelToursEntity.website); database1.bwebsite =
						 * false; }
						 * 
						 * if (database1.bemail_id) {
						 * 
						 * HotelToursEntity.email_id = new String(ch, start,
						 * length); System.out.println("EMAIL_ID : " +
						 * HotelToursEntity.email_id); database1.bemail_id =
						 * false; }
						 * 
						 * if (database1.btype) { HotelToursEntity.type = new
						 * String(ch, start, length);
						 * System.out.println("TYPE : " +
						 * HotelToursEntity.type); database1.btype = false; }
						 * 
						 * if (database1.brooms) { HotelToursEntity.rooms = new
						 * String(ch, start, length);
						 * System.out.println("ROOMS : " +
						 * HotelToursEntity.rooms); database1.brooms = false; }
						 * 
						 * }
						 */

						if (database2flag) {
							if (database2.bnameofagency) {

								TravelToursEntity.nameofagency = new String(ch,
										start, length);

								System.out.println("NAME_OF_AGENCY : "
										+ TravelToursEntity.nameofagency);
								database2.bnameofagency = false;
							}

							if (database2.baddress) {

								TravelToursEntity.address = new String(ch,
										start, length);

								System.out.println("ADDRESS : "
										+ TravelToursEntity.address);
								database2.baddress = false;
							}

							if (database2.bphone) {

								TravelToursEntity.phone = new String(ch, start,
										length);

								System.out.println("PHONE : "
										+ TravelToursEntity.phone);
								database2.bphone = false;
							}

							if (database2.bfax) {

								TravelToursEntity.fax = new String(ch, start,
										length);

								System.out.println("FAX : "
										+ TravelToursEntity.fax);
								database2.bfax = false;
							}

							if (database2.bemail) {

								TravelToursEntity.email = new String(ch, start,
										length);

								System.out.println("EMAIL : "
										+ TravelToursEntity.email);
								database2.bemail = false;
							}

							if (database2.bregion) {

								TravelToursEntity.region = new String(ch,
										start, length);

								System.out.println("REGION : "
										+ TravelToursEntity.region);
								database2.bregion = false;
							}

							if (database2.bcity) {

								TravelToursEntity.city = new String(ch, start,
										length);

								System.out.println("CITY : "
										+ TravelToursEntity.city);
								database2.bcity = false;
							}

							if (database2.bstate) {

								TravelToursEntity.state = new String(ch, start,
										length);

								System.out.println("STATE : "
										+ TravelToursEntity.state);
								database2.bstate = false;
							}

							if (database2.bcontactperson) {

								TravelToursEntity.contactperson = new String(
										ch, start, length);

								System.out.println("CONTACT_PERSON : "
										+ TravelToursEntity.contactperson);
								database2.bcontactperson = false;
							}

							if (database2.btype) {

								TravelToursEntity.type = new String(ch, start,
										length);

								System.out.println("TYPE : "
										+ TravelToursEntity.type);
								database2.btype = false;
							}

						}

					}

				};

				// final String SDCARDLOC = Environment
				// .getExternalStorageDirectory().getAbsolutePath() + "/";

				/*
				 * if (database1flag) saxParser.parse(new InputSource(new
				 * BufferedReader( new FileReader(SDCARDLOC +
				 * "HotelTourismData.xml"))), handler);
				 */

				File fileTravelTourData = getApplicationContext()
						.getFileStreamPath("travel_tour_data.xml");

				// if database.bin doesn't exist then
				// copy raw/database.bin to data directory
				if (!fileTravelTourData.exists()) {
					Utility.resourceRestore(getApplicationContext(),
							"travel_tour_data.xml", R.raw.travel_tour_data);
				}
				if (database2flag)
					saxParser.parse(new InputSource(new BufferedReader(
							new FileReader(fileTravelTourData))), handler);

				db.close();
				Toast.makeText(getApplicationContext(),
						"Database Synchronization complete", Toast.LENGTH_SHORT)
						.show();

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

	}

	final String TOURAGENCY_DATABASE_PREF = "Tour_agency_database_pref";

	private void activateFirstStart() {
		SharedPreferences settings = getSharedPreferences(
				TOURAGENCY_DATABASE_PREF, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("first_start_flag", true);
		editor.commit();
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_tour_agency_main, menu);
		return true;
	}
*/
	private OnClickListener speakStateContentListener = new OnClickListener() {

		public void onClick(View v) {
			buttonSpeakStateFlag = true;
			speak();
		}

	};

	private OnClickListener speakCityContentListener = new OnClickListener() {

		public void onClick(View v) {
			buttonSpeakCityFlag = true;
			speak();
		}

	};

	private OnClickListener speakRegionContentListener = new OnClickListener() {

		public void onClick(View v) {
			buttonSpeakRegionFlag = true;
			speak();
		}

	};

	public void checkVoiceRecognition() {
		// Check if voice recognition is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			buttonSpeakState.setEnabled(false);
			buttonSpeakCity.setEnabled(false);
			//buttonSpeakRegion.setEnabled(false);

			// buttonSpeak.setText("Voice recognizer not present");
			Toast.makeText(this, "Voice recognizer not present",
					Toast.LENGTH_SHORT).show();
		}
	}

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

	public void speak() {

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Specify the calling package to identify your application
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
				.getPackage().getName());

		// Display an hint to the user about what he should say.
		// intent.putExtra(RecognizerIntent.EXTRA_PROMPT, metTextHint.getText()
		// .toString());

		// Given an hint to the recognizer about what the user is going to say
		// There are two form of language model available
		// 1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
		// 2.LANGUAGE_MODEL_FREE_FORM : If not sure about the words or phrases
		// and its domain.
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

		// If number of Matches is not selected then return show toast message
		/*
		 * if (msTextMatches.getSelectedItemPosition() ==
		 * AdapterView.INVALID_POSITION) { Toast.makeText(this,
		 * "Please select No. of Matches from spinner",
		 * Toast.LENGTH_SHORT).show(); return; }
		 */

		/*
		 * int noOfMatches = Integer.parseInt(msTextMatches.getSelectedItem()
		 * .toString());
		 */
		int noOfMatches = 1;

		// Specify how many results you want to receive. The results will be
		// sorted where the first result is the one with higher confidence.
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);
		// Start the Voice recognizer activity for the result.
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/**
	 * Description :
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case VOICE_RECOGNITION_REQUEST_CODE: {

			// If Voice recognition is successful then it returns RESULT_OK
			if (resultCode == RESULT_OK && data != null) {

				ArrayList<String> textMatchList = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				if (!textMatchList.isEmpty()) {
					// If first Match contains the 'search' word
					// Then start web search.
					if (textMatchList.get(0).contains("search")) {

						String searchQuery = textMatchList.get(0);
						searchQuery = searchQuery.replace("search", "");
						Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
						search.putExtra(SearchManager.QUERY, searchQuery);
						startActivity(search);
					} else {
						// populate the Matches
						/*
						 * mlvTextMatches.setAdapter(new ArrayAdapter<String>(
						 * this, android.R.layout.simple_list_item_1,
						 * textMatchList));
						 */

						if (buttonSpeakStateFlag) {
							AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.autocompletetextview_state);
							editText.setText(textMatchList.get(0));
							buttonSpeakStateFlag = false;
						} else if (buttonSpeakCityFlag) {
							AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.autocompletetextview_city);
							editText.setText(textMatchList.get(0));
							buttonSpeakCityFlag = false;
						} /*
						 * else if (buttonSpeakRegionFlag) {
						 * AutoCompleteTextView editText =
						 * (AutoCompleteTextView)
						 * findViewById(R.id.autocompletetextview_region);
						 * editText.setText(textMatchList.get(0));
						 * buttonSpeakRegionFlag = false; }
						 */

					}

				}
				// Result code for various error.
			} else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
				showToastMessage("Audio Error");
			} else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
				showToastMessage("Client Error");
			} else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
				showToastMessage("Network Error");
			} else if (resultCode == RecognizerIntent.RESULT_NO_MATCH) {
				showToastMessage("No Match");
			} else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
				showToastMessage("Server Error");
			}

			break;
		}
		
		
		case TTS_DATA_CHECK_CODE:{
			
			if (requestCode == TTS_DATA_CHECK_CODE) {
				if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {

					textSpeech = new TextToSpeech(getApplicationContext(), this);

				} else {

					Intent installIntent = new Intent();
					installIntent
							.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
					startActivity(installIntent);
				}
			}
			
			
		}
		
		
		
		
		}
	}

	/**
	 * Helper method to show the toast message
	 **/
	void showToastMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	static Boolean database2flag = false;

	/*
	 * static class database1 { static boolean bhotelname = false; static
	 * boolean baddress = false; static boolean bstate = false; static boolean
	 * bphone = false; static boolean bfax = false; static boolean bemail_id =
	 * false; static boolean bwebsite = false; static boolean btype = false;
	 * static boolean brooms = false; }
	 */

	static class database2 {
		static boolean bnameofagency = false;
		static boolean baddress = false;
		static boolean bphone = false;
		static boolean bfax = false;
		static boolean bemail = false;
		static boolean bregion = false;
		static boolean bcity = false;
		static boolean bstate = false;
		static boolean bcontactperson = false;
		static boolean btype = false;

	}

	/*
	 * static class HotelToursEntity { public static String rowid; public static
	 * String hotelname; public static String address; public static String
	 * state; public static String phone; public static String fax; public
	 * static String email_id; public static String website; public static
	 * String type; public static String rooms; }
	 */
	static class TravelToursEntity {
		public static String rowid;
		public static String nameofagency;
		public static String address;
		public static String phone;
		public static String fax;
		public static String email;
		public static String region;
		public static String city;
		public static String state;
		public static String contactperson;
		public static String type;
	}

	/**
	 * Initializes autosuggestion list
	 */
	private void initialize() {

		TourAgencyDatabase db = new TourAgencyDatabase(getApplicationContext());
		db.open();

		Cursor c = db.getTouristAgencyState();
		String[] STATES = new String[c.getCount()];

		ArrayAdapter<String> adapterState = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, STATES);

		if (c.moveToFirst()) {
			int i = 0;
			do {

				STATES[i++] = c.getString(0);

			} while (c.moveToNext());
		} else
			Toast.makeText(this, "No State info found", Toast.LENGTH_LONG)
					.show();

		c = db.getTouristAgencyCity();
		String[] CITY = new String[c.getCount()];

		ArrayAdapter<String> adapterCity = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, CITY);

		if (c.moveToFirst()) {
			int i = 0;
			do {

				CITY[i++] = c.getString(0);

			} while (c.moveToNext());
		} else
			Toast.makeText(this, "No City info found", Toast.LENGTH_LONG)
					.show();

		c = db.getTouristAgencyRegion();
		String[] REGION = new String[c.getCount()];

		ArrayAdapter<String> adapterRegion = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, REGION);

		if (c.moveToFirst()) {
			int i = 0;
			do {

				REGION[i++] = c.getString(0);

			} while (c.moveToNext());
		} else
			Toast.makeText(this, "No Region info found", Toast.LENGTH_LONG)
					.show();

		db.close();

		autoCompleteTextviewState = (AutoCompleteTextView) findViewById(R.id.autocompletetextview_state);
		autoCompleteTextviewState.setAdapter(adapterState);

		autoCompleteTextviewCity = (AutoCompleteTextView) findViewById(R.id.autocompletetextview_city);
		autoCompleteTextviewCity.setAdapter(adapterCity);
		/*
		 * autoCompleteTextviewRegion = (AutoCompleteTextView)
		 * findViewById(R.id.autocompletetextview_region);
		 * autoCompleteTextviewRegion.setAdapter(adapterRegion);
		 */

		autoCompleteTextviewState.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub

				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:

						FOR_QUERY_CLICK_FLAG = 1;
						if (textSpeech != null) {
							if (-1 == textSpeech.stop()) {
								Log.i(LOGTAG, "stopping failed");
							}

						}
						new GetFromDatabase().execute();

						return true;
					}
				}
				return false;
			}
		});

		autoCompleteTextviewCity.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub

				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						FOR_QUERY_CLICK_FLAG = 2;
						if (textSpeech != null) {
							if (-1 == textSpeech.stop()) {
								Log.i(LOGTAG, "stopping failed");
							}

						}
						new GetFromDatabase().execute();

						return true;
					}
				}
				return false;
			}
		});
		/*
		 * autoCompleteTextviewRegion.setOnKeyListener(new View.OnKeyListener()
		 * {
		 * 
		 * @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
		 * // TODO Auto-generated method stub
		 * 
		 * if (event.getAction() == KeyEvent.ACTION_DOWN) { switch (keyCode) {
		 * case KeyEvent.KEYCODE_DPAD_CENTER: case KeyEvent.KEYCODE_ENTER:
		 * 
		 * TourAgencyDatabase db = new TourAgencyDatabase(
		 * getApplicationContext()); db.open();
		 * 
		 * String state = autoCompleteTextviewState.getText() .toString();
		 * String city = autoCompleteTextviewCity.getText() .toString(); String
		 * region = autoCompleteTextviewRegion.getText() .toString();
		 * 
		 * if ((!region.equals("")) && (!state.equals("")) &&
		 * (!city.equals(""))) { Cursor c = db
		 * .getTouristAgencyDetailByStateCityRegion( state, city, region);
		 * formatDataInWebview(c); } else if ((!region.equals("")) &&
		 * (!state.equals("")) && (city.equals(""))) { Cursor c =
		 * db.getTouristAgencyDetailByStateRegion( state, region);
		 * formatDataInWebview(c); }
		 * 
		 * db.close();
		 * 
		 * return true; } } return false; } });
		 */

	}



	private class GetFromDatabase extends AsyncTask<Void, Void, Void> {

		Cursor c;
		
		
		private void queryOnCityClick() {
			TourAgencyDatabase db = new TourAgencyDatabase(getApplicationContext());
			db.open();

			String state = autoCompleteTextviewState.getText().toString().trim().toUpperCase();
			String city = autoCompleteTextviewCity.getText().toString().trim().toUpperCase();
			// String region = autoCompleteTextviewRegion.getText()
			// .toString();

			if ((!city.equals("")) && (!state.equals(""))) {
				// && region.equals("")) {
				c = db.getTouristAgencyDetailByStateCity(state, city);
				//formatDataInWebview(c);
			} else if ((!city.equals("")) && (state.equals(""))) {
				// && region.equals("")) {
				c = db.getTouristAgencyDetailByCity(city);
				//formatDataInWebview(c);
			} else if(city.equals("") && (!state.equals(""))){
				c = db.getTouristAgencyDetailByState(state);
			}
			
			
			
			/*
			 * else if ((!city.equals("")) && (!state.equals(""))){ //&&
			 * (!region.equals(""))) { Cursor c = db
			 * .getTouristAgencyDetailByStateCity( state, city);
			 * formatDataInWebview(c); }
			 */
			db.close();
		}

		private void queryOnStateClick() {
			TourAgencyDatabase db = new TourAgencyDatabase(getApplicationContext());
			db.open();
			String state = autoCompleteTextviewState.getText().toString().trim().toUpperCase();
			String city = autoCompleteTextviewCity.getText().toString().trim().toUpperCase();
			// String region = autoCompleteTextviewRegion.getText()
			// .toString();

			if ((!state.equals("")) && city.equals("")) {
				// && region.equals("")) {
				c = db.getTouristAgencyDetailByState(state);
				//formatDataInWebview(c);

			} else if ((!state.equals("")) && (!city.equals(""))) {
				// && region.equals("")) {
				c = db.getTouristAgencyDetailByStateCity(state, city);
				//formatDataInWebview(c);
			}else if(state.equals("") && (!city.equals(""))){
				c = db.getTouristAgencyDetailByCity(city);
			}
			
			
			
			/*
			 * else if ((!state.equals("")) && (!city.equals("")) &&
			 * (!region.equals(""))) { Cursor c = db
			 * .getTouristAgencyDetailByStateCity( state, city, region);
			 * formatDataInWebview(c); }
			 */

			db.close();
		}
		
		
		
		@Override
		protected Void doInBackground(Void... params) {

			if (FOR_QUERY_CLICK_FLAG == 1) {
				queryOnStateClick();
			} else {
				queryOnCityClick();
			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			loadingFileProgress.cancel();
			if(c!=null){
			 formatDataInWebview(c);
			}
			super.onPostExecute(result);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			loadingFileProgress = ProgressDialog.show(
					TourAgencyMainActivity.this, "", "Please wait..");
			super.onPreExecute();
		}

	}

	private void formatDataInWebview(Cursor c) {

		WebView webview = (WebView) findViewById(R.id.webView1);

		String HEADER_OPEN = "<html><body>";
		String HEADER_CLOSE = "</body></html>";
		String BR = "<br>";
		String BOLD_OPEN = "<b>";
		String BOLD_CLOSE = "</b>";
		/*
		 * String A_TAG = "<a"; String TAG_CLOSE = ">"; String A_CLOSE_TAG =
		 * "</a>"; String HREF_OPEN = "href=\""; String HTTP = "http://"; String
		 * MAIL_TO = "mailto:"; String TEL_OPEN = "tel://"; String FAX_OPEN =
		 * "fax://"; String DOUBLE_QUOTES = "\"";
		 */

		// <a href="mailto:webmaster@example.com">Jon Doe</a>.<br>
		// <a href="tel://555-5555"><img src="phone.png" alt="Call Now!" /></a>
		// <a href="http://www.w3schools.com">Visit W3Schools</a>

		StringBuilder BODY = new StringBuilder();

		// ... although note that there are restrictions on what this HTML can
		// do.
		// See the JavaDocs for loadData() and loadDataWithBaseURL() for more
		// info.

		if ( c.moveToFirst()) {

			do {

				BODY.append(BOLD_OPEN + "Name of Agency " + BOLD_CLOSE
						+ c.getString(1) + BR + BOLD_OPEN + "Address "
						+ BOLD_CLOSE + c.getString(2) + BR + BOLD_OPEN
						+ "Phone " + BOLD_CLOSE + c.getString(3) + BR
						+ BOLD_OPEN + "Fax " + BOLD_CLOSE + c.getString(4) + BR
						+ BOLD_OPEN + "Email " + BOLD_CLOSE + c.getString(5)
						+ BR + BOLD_OPEN + "Region " + BOLD_CLOSE
						+ c.getString(6) + BR + BOLD_OPEN + "City "
						+ BOLD_CLOSE + c.getString(7) + BR + BOLD_OPEN
						+ "State " + BOLD_CLOSE + c.getString(8) + BR
						+ BOLD_OPEN + "Contact person " + BOLD_CLOSE
						+ c.getString(9) + BR + BOLD_OPEN + "Type "
						+ BOLD_CLOSE + c.getString(10));
				BODY.append(BR);
				BODY.append(BR);

				if (ttsFlag) {
					talk("Name of Agency " + c.getString(1));
					talk("Address " + c.getString(2));
					talk("Phone " + c.getString(3));
					talk("Fax " + c.getString(4));
					talk("Email " + c.getString(5));
					talk("Region " + c.getString(6));
					talk("City " + c.getString(7));
					talk("State " + c.getString(8));
					talk("Contact person " + c.getString(9));
					talk("Type " + c.getString(10));
				}

				/*
				 * BODY.append(BOLD_OPEN + "Hotel Name " +BOLD_CLOSE +
				 * c.getString(1) + BR + BOLD_OPEN + "Address " + BOLD_CLOSE +
				 * c.getString(2) + BR + BOLD_OPEN + "STATE " + BOLD_CLOSE +
				 * c.getString(3) + BR + BOLD_OPEN + A_TAG + HREF_OPEN +
				 * TEL_OPEN + c.getString(4) + DOUBLE_QUOTES + TAG_CLOSE +
				 * "Phone " + c.getString(4) + A_CLOSE_TAG + BOLD_CLOSE + BR +
				 * BOLD_OPEN + A_TAG + HREF_OPEN + FAX_OPEN + c.getString(5) +
				 * DOUBLE_QUOTES + TAG_CLOSE + "Fax " + c.getString(5) +
				 * A_CLOSE_TAG + BOLD_CLOSE + BR + BOLD_OPEN + A_TAG + HREF_OPEN
				 * + MAIL_TO + c.getString(6) + DOUBLE_QUOTES + TAG_CLOSE +
				 * "Email-id " + c.getString(6) + A_CLOSE_TAG + BOLD_CLOSE + BR
				 * + BOLD_OPEN + A_TAG + HREF_OPEN + HTTP + c.getString(7) +
				 * DOUBLE_QUOTES + TAG_CLOSE + "Website " + c.getString(7) +
				 * A_CLOSE_TAG + BOLD_CLOSE + BOLD_OPEN + "Type " + BOLD_CLOSE +
				 * c.getString(8) + BR + BOLD_OPEN + "Rooms " + BOLD_CLOSE +
				 * c.getString(9)); BODY.append(BR); BODY.append(BR);
				 * BODY.append(BOLD_OPEN+"<a href=\"tel://555-5555\">58956</a>"+
				 * BOLD_CLOSE);
				 */

			} while (c.moveToNext());

		} else{
			Toast.makeText(this, "No info found", Toast.LENGTH_LONG).show();
			openQueryStatusDialog();
		}
		String summary = HEADER_OPEN + BODY + HEADER_CLOSE;
		Spannable sp = new SpannableString(Html.fromHtml(summary));
		Linkify.addLinks(sp, Linkify.ALL);
		webview.loadData(Html.toHtml(sp), "text/html", null);
		// webview.loadData(summary, "text/html", null);
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			if (BuildConfig.DEBUG)
				Log.i(LOGTAG,
						"Sucessfull intialization of Text-To-Speech engine");

		} else if (status == TextToSpeech.ERROR) {
			if (BuildConfig.DEBUG)
				Log.i(LOGTAG, "Unable to initialize Text-To-Speech engine");
		}

	}

	@Override
	public void onDestroy() {

		super.onDestroy();

		if (textSpeech != null) {
			if (-1 == textSpeech.stop()) {
				Log.i(LOGTAG, "stopping failed");
			}
			textSpeech.shutdown();
		}
	}
	
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
	      case ALERT_DIALOG_OPTIONS_ID_NOQUERYRESULT:
			return QueryStatusDialog();

		default:
			return null;
		}
	}
	
	
	
	private void openAlertOptionsDialog(int id) {
		showDialog(id);
	}
	
	
	/**
	 * Description : Opens Create New File Dialog
	 */
	private void openQueryStatusDialog() {
		openAlertOptionsDialog(ALERT_DIALOG_OPTIONS_ID_NOQUERYRESULT);
	}
		
	
	/**
	 * Description : Displays Create new file dialog
	 * 
	 * @return AlertDialog
	 */

	private AlertDialog QueryStatusDialog() {

		return new AlertDialog.Builder(this).setTitle("Query Status").setMessage("No results found for this query..")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				dialog.cancel();
				//finish(); // finishing the activity
			}

		}).setCancelable(false).create();
	}

}
