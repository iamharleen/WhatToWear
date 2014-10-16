/**
 * This file handles main activities by fetching weather condition according to location
 * 
 * @author Harleen Kaur
 * @version 1.0
 */

package com.example.whattowear;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import android.support.v7.app.ActionBarActivity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.location.LocationManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

/**
 * Main Activity fetches weather data from yahoo weather by using Yahoo api's.
 * Google api's are being used to fetch the current location which will be
 * passed to yahoo weather to fetch weather. Location search functionality also
 * been provided by yahoo api's.
 * 
 */
public class MainActivity extends ActionBarActivity implements LocationListener {

	// defining global variables to be used in activity
	private Double latituteValue;
	private Double longitudeValue;
	private LocationManager locationManager;
	private String provider;
	SharedPreferences pref;
	boolean errorMessage = false;
	BitmapDrawable bdrawable;
	String date, condition, humid, wind, temp, link;
	Bitmap icon = null;
	TextView title, dateText, windText, condText, humidText, tempText;
	ImageView image;
	ArrayList<String> weather = new ArrayList<String>();
	ProgressDialog dialog;
	String woeid = "", citySearch = "";
	String code, cityName, city;
	EditText cityEntry;
	ParseQuery<ParseObject> query;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // removing the title bar
		setContentView(R.layout.activity_main);

		// get the searched city value from Intent
		Intent intent = getIntent();
		citySearch = intent.getStringExtra("city");

		// get the preferences in pref variable
		pref = PreferenceManager.getDefaultSharedPreferences(this);

		MainActivity.this.overridePendingTransition(R.layout.slide2_left,
				R.layout.slide1_right);

		// define search button and assign listener on it
		ImageButton search_button = (ImageButton) findViewById(R.id.search_button);
		search_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// call function to handle onClick event
				clickHandler();
				// add code to hide keypad
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(cityEntry.getWindowToken(), 0);
			}
		});

		cityEntry = (EditText) findViewById(R.id.cityEntry);
		cityEntry.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					clickHandler();
					// add code to hide keypad
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(cityEntry.getWindowToken(), 0);
				}
				return true;
			}
		});

		// define right button and assign listener on it
		ImageButton btnopen = (ImageButton) findViewById(R.id.rightButton);
		btnopen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// set intent for main and parse activity
				Intent slideactivity = new Intent(MainActivity.this,
						ParseActivity.class);
				// set sliding window animated on left button click
				Bundle bndlanimation = ActivityOptions.makeCustomAnimation(
						getApplicationContext(), R.layout.slide1_left,
						R.layout.slide1_right).toBundle();
				startActivity(slideactivity, bndlanimation);

			}
		});

		// Use Google API's to get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// if GPS not enabled send message to user
		if (!enabled) {
			System.out.println("GPS not available");
			Context context = getApplicationContext();
			// Toast message for user when he clicks Show button
			Toast saveToast = Toast.makeText(context, "Please enable GPS",
					Toast.LENGTH_LONG);
			saveToast.show();
		}

		// initialize the location fields and set provider
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);

		// handle activities as per location
		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			onLocationChanged(location);
		} else {
			latituteValue = 0.0;
			longitudeValue = 0.0;
		}

		// assign variables to Layout view
		image = (ImageView) findViewById(R.id.icon);
		title = (TextView) findViewById(R.id.title);
		tempText = (TextView) findViewById(R.id.tempText);
		dateText = (TextView) findViewById(R.id.dateText);
		windText = (TextView) findViewById(R.id.windText);
		condText = (TextView) findViewById(R.id.conditionText);
		humidText = (TextView) findViewById(R.id.humidityText);
		cityEntry = (EditText) findViewById(R.id.cityEntry);
		cityEntry.setVisibility(View.GONE);

		Typeface typeFace = Typeface.createFromAsset(getAssets(),
				"Fonts/HelveticaNeue-Light.otf");
		// set font present in asset
		tempText.setTypeface(typeFace);
		condText.setTypeface(typeFace);
		dateText.setTypeface(typeFace);
		humidText.setTypeface(typeFace);
		windText.setTypeface(typeFace);
		title.setTypeface(typeFace);

		// call background task to fetch weather data
		new retrieve_weatherTask().execute();

		// initializing Parse database
		Parse.initialize(this, "U9sodpBsFvn3ZQxMPidBu4M3ClCj6jEt36WtuWg4",
				"zGzVKLkMkENW4o9xzJZk0pbexj5cKO7izM20nVQ3");
		ParseUser.enableAutomaticUser();
		// set ACL to Parse and set the public access
		ParseACL defaultACL = new ParseACL();
		defaultACL.setPublicReadAccess(true);
		ParseACL.setDefaultACL(defaultACL, true);

		// locate class table "BackgroundImage" from Parse database
		query = new ParseQuery<ParseObject>("BackgroundImage");

	}

	/**
	 * This function handles toast message when city searched is not present
	 * 
	 * @param nothing
	 * 
	 * @return nothing
	 */
	public void handleToast() {
		if (errorMessage) {
			Context context = getApplicationContext();
			// Toast message for user when city not present
			Toast saveToast = Toast.makeText(context, "City not found",
					Toast.LENGTH_LONG);
			saveToast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
			saveToast.show();
		}
	}

	/**
	 * This function handles onClick event for search button
	 * 
	 * @param nothing
	 * 
	 * @return nothing
	 */
	public void clickHandler() {
		cityEntry = (EditText) findViewById(R.id.cityEntry);
		city = "";
		if (!(cityEntry.getText().toString().matches(""))) {
			// get the city entered by user into local variable
			city = cityEntry.getText().toString();
			Intent intent = getIntent();
			// saving into intent
			intent.putExtra("city", city);
			finish();
			// start activity by passing intent
			startActivity(intent);
		} else {
			// if cityText is blank and visible, make the text box invisible
			if (cityEntry.getVisibility() == View.VISIBLE) {
				cityEntry.setVisibility(View.GONE);
			} else {
				// if cityText is blank and invisible, make the text box visible
				cityEntry.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			citySearch = data.getStringExtra("cityName");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 400, 1, this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		latituteValue = (double) (location.getLatitude());
		longitudeValue = (double) (location.getLongitude());
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * This function fetches weather data from yahoo weather in background async
	 * task
	 * 
	 * @param async
	 *            task
	 * 
	 * @return String result of background activity
	 */
	protected class retrieve_weatherTask extends
			AsyncTask<Void, String, String> {

		protected void onPreExecute() {
			// before executing the async task display the progress dialog
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage("Loading…");
			dialog.setCancelable(false);
			// show dialog to user
			dialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			String qResult = "";
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			try {
				String bUrl = "http://query.yahooapis.com/v1/public/yql?q=";
				// url with searched city variable
				String yQuery = "select woeid from geo.placefinder where text='"
						+ citySearch + "' and gflags=\"R\"";
				// encode the base url
				String fullUrlStr = bUrl + URLEncoder.encode(yQuery, "UTF-8")
						+ "&format=json";
				// url when latitude and longitude present
				String woeidUrl = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.placefinder%20where%20text%3D%22"
						+ latituteValue
						+ "%2C"
						+ longitudeValue
						+ "%22%20and%20gflags%3D%22R%22&format=json";

				URL fullUrl;
				if (citySearch == null) {
					// if city is not searched then take url from GPS location
					fullUrl = new URL(woeidUrl);
				} else {
					// if city has been searched then take yahoo query with city
					// name
					fullUrl = new URL(fullUrlStr);
				}

				JSONObject json = getJson(fullUrl);

				// if results is not present with the searched city then take
				// current location
				if (json.getJSONObject("query").getString("results") == "null") {
					errorMessage = true;
					fullUrl = new URL(woeidUrl);
					json = getJson(fullUrl);
				}
				// checking whether the JSON is objet or array
				if (json.getJSONObject("query").getJSONObject("results")
						.optJSONArray("Result") != null) {
					woeid = json.getJSONObject("query")
							.getJSONObject("results").getJSONArray("Result")
							.getJSONObject(0).getString("woeid");
				} else {
					// get the woeid from json
					woeid = json.getJSONObject("query")
							.getJSONObject("results").getJSONObject("Result")
							.getString("woeid");
				}
				System.out.println(json.toString());
			} catch (Exception e) {
				System.out.println("Exception: " + e);
			}

			// create http with woeid defined above
			HttpGet httpGet = new HttpGet(
					"http://weather.yahooapis.com/forecastrss?w=" + woeid);

			try {
				HttpResponse response = httpClient.execute(httpGet,
						localContext);
				HttpEntity entity = response.getEntity();
				// get the results in buffered reader
				if (entity != null) {
					InputStream is = entity.getContent();
					Reader rd = new InputStreamReader(is);
					BufferedReader bufferedreader = new BufferedReader(rd);
					StringBuilder sb = new StringBuilder();
					String stringReadLine = null;
					while ((stringReadLine = bufferedreader.readLine()) != null) {
						sb.append(stringReadLine + "\n");
					}
					qResult = sb.toString();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				Toast.makeText(MainActivity.this, e.toString(),
						Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(MainActivity.this, e.toString(),
						Toast.LENGTH_LONG).show();
			}

			// Document parser is created to read the data from xml file
			// generated from yahoo query
			Document doc = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder parser;
			try {
				parser = factory.newDocumentBuilder();
				doc = parser
						.parse(new ByteArrayInputStream(qResult.getBytes()));
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
				Toast.makeText(MainActivity.this, e1.toString(),
						Toast.LENGTH_LONG).show();
			} catch (SAXException e) {
				e.printStackTrace();
				Toast.makeText(MainActivity.this, e.toString(),
						Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(MainActivity.this, e.toString(),
						Toast.LENGTH_LONG).show();
			}

			// create node from location and condition
			Node descCity = doc.getElementsByTagName("yweather:location").item(
					0);
			Node codeValue = doc.getElementsByTagName("yweather:condition")
					.item(0);
			cityName = descCity.getAttributes().getNamedItem("city")
					.getNodeValue().toString();
			code = codeValue.getAttributes().getNamedItem("code")
					.getNodeValue().toString();

			// saving weather code in Shared Pref
			SharedPreferences.Editor prefEditor = pref.edit();
			// putting code in shared pref
			prefEditor.putString("code", code.toString());
			prefEditor.putBoolean("saved", true);
			prefEditor.commit(); // commit the changes

			Node temperatureNode = doc.getElementsByTagName(
					"yweather:condition").item(0);
			temp = temperatureNode.getAttributes().getNamedItem("temp")
					.getNodeValue().toString();
			Node tempNode = doc.getElementsByTagName("yweather:units").item(0);
			temp = temp
					+ "°"
					+ tempNode.getAttributes().getNamedItem("temperature")
							.getNodeValue().toString();

			Node humidNode = doc.getElementsByTagName("yweather:atmosphere")
					.item(0);
			humid = humidNode.getAttributes().getNamedItem("humidity")
					.getNodeValue().toString()
					+ "%";

			Node dateNode = doc.getElementsByTagName("yweather:forecast").item(
					0);
			date = dateNode.getAttributes().getNamedItem("date").getNodeValue()
					.toString();

			Node windNode = doc.getElementsByTagName("yweather:wind").item(0);
			wind = windNode.getAttributes().getNamedItem("speed")
					.getNodeValue().toString();
			Node windUnitNode = doc.getElementsByTagName("yweather:units")
					.item(0);
			wind = wind
					+ " "
					+ windUnitNode.getAttributes().getNamedItem("speed")
							.getNodeValue().toString();

			Node conditionNode = doc.getElementsByTagName("yweather:condition")
					.item(0);
			condition = conditionNode.getAttributes().getNamedItem("text")
					.getNodeValue().toString();

			// use tokenizer to extract the weather icon url
			String desc = doc.getElementsByTagName("item").item(0)
					.getChildNodes().item(13).getTextContent().toString();
			StringTokenizer str = new StringTokenizer(desc, "<=>");
			System.out.println("Tokens: " + str.nextToken("=>"));
			String src = str.nextToken();
			System.out.println("src: " + src);
			String urlIcon = src.substring(1, src.length() - 2);
			Pattern TAG_REGEX = Pattern.compile("(.+?)<br />");
			Matcher matcher = TAG_REGEX.matcher(desc);
			while (matcher.find()) {
				weather.add(matcher.group(1));
			}

			Pattern links = Pattern.compile("(.+?)<BR/>");
			matcher = links.matcher(desc);
			while (matcher.find()) {
				System.out.println("Match Links: " + (matcher.group(1)));
				link = matcher.group(1);
			}

			// execute query on the weather code value
			query.whereEqualTo("code", Integer.parseInt(code));
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> imageList, ParseException e) {
					if (e == null) {
						// locate column and create Object
						ParseFile fileObject = (ParseFile) imageList.get(0)
								.get("bgImage");
						// get the image in background
						fileObject.getDataInBackground(new GetDataCallback() {
							public void done(byte[] data, ParseException e) {
								if (e == null) {
									Log.d("error", "Image is present");

									// decoding to bitmap
									Bitmap bitmap = BitmapFactory
											.decodeByteArray(data, 0,
													data.length);
									ImageView imageView = (ImageView) findViewById(R.id.image);
									if (bitmap != null) {
										RelativeLayout relLay = (RelativeLayout) findViewById(R.id.main_layout);
										bdrawable = new BitmapDrawable(bitmap);
										relLay.setBackgroundDrawable(bdrawable);
									} else {
										// if bitmap cudn't created, display
										// loading image
										imageView.setImageDrawable(imageView
												.getContext()
												.getResources()
												.getDrawable(
														R.drawable.loading1));
									}
								} else {
									Log.d("error",
											"Image could not be downloaded.");
								}
							}
						});
						Log.d("Image is ", "retrieved " + imageList.size()
								+ " scores");
					} else {
						Log.d("Fetching image ", "Error: " + e.getMessage());
					}
				}
			});

			InputStream input = null;
			try {
				int response = -1;
				URL url = new URL(urlIcon);
				URLConnection conn = url.openConnection();

				// set the weather icon on screen
				if (!(conn instanceof HttpURLConnection))
					throw new IOException("Error in HTTP connection");
				HttpURLConnection connection = (HttpURLConnection) conn;
				connection.setAllowUserInteraction(false);
				connection.setInstanceFollowRedirects(true);
				connection.setRequestMethod("GET");
				connection.connect();

				response = connection.getResponseCode();
				if (response == HttpURLConnection.HTTP_OK) {
					System.out.println("*********************");
					input = connection.getInputStream();
				}
				icon = BitmapFactory.decodeStream(input);
				input.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return qResult;
		}

		/**
		 * This function gets the JSONObject value from URL
		 * 
		 * @param URL
		 * 
		 * @return JSONObject
		 */
		protected JSONObject getJson(URL url) {
			InputStream is;
			JSONObject json = null;
			try {
				is = url.openStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						is, Charset.forName("UTF-8")));
				String jsonText = "";

				StringBuilder sb = new StringBuilder();
				int cp;
				while ((cp = rd.read()) != -1) {
					sb.append((char) cp);
				}
				jsonText = sb.toString();
				json = new JSONObject(jsonText);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json;
		}

		/**
		 * This function set the values on Activity screen once async task is
		 * done
		 * 
		 * @param String
		 *            result value
		 * 
		 * @return nothing
		 */
		protected void onPostExecute(String result) {
			System.out.println("POST EXECUTE");
			if (dialog.isShowing())
				dialog.dismiss();// dismiss loading dialog
			// set the weather variables on activity screen
			tempText.setText(temp);
			condText.setText(condition);
			dateText.setText(date);
			humidText.setText("Humidity: " + humid);
			windText.setText("Wind: " + wind);
			image.setImageBitmap(icon);
			title.setText(cityName);
			// check if error present then show toast
			handleToast();
		}

	}
}
