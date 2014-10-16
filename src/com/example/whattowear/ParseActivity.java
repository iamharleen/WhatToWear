/**
 * This file handle activity to display clothes images according to weather
 * 
 * @author Harleen Kaur
 * @version 1.0
 */

package com.example.whattowear;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Parse Activity fetches dress image from Parse database according weather code
 * got from shared preference
 * 
 */
public class ParseActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // removing the title bar
		setContentView(R.layout.activity_parse);

		this.overridePendingTransition(R.layout.slide2_left,
				R.layout.slide2_right);

		// define left button and assign listener on it
		ImageButton btnopen = (ImageButton) findViewById(R.id.leftButton);
		btnopen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// set intent for main and parse activity
				Intent slideactivity = new Intent(ParseActivity.this,
						MainActivity.class);

				// set sliding window animated on left button click
				Bundle bndlanimation = ActivityOptions.makeCustomAnimation(
						getApplicationContext(), R.layout.slide2_left,
						R.layout.slide2_right).toBundle();
				startActivity(slideactivity, bndlanimation);
			}
		});

		// fetch and store data from shared pref
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String savedPref = pref.getString("code", null);
		int code = Integer.parseInt(savedPref);

		// initialize Parse according to package name
		Parse.initialize(this, "U9sodpBsFvn3ZQxMPidBu4M3ClCj6jEt36WtuWg4",
				"zGzVKLkMkENW4o9xzJZk0pbexj5cKO7izM20nVQ3");
		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		defaultACL.setPublicReadAccess(true);
		// assign access to Parse
		ParseACL.setDefaultACL(defaultACL, true);

		// call dresscode() to get the dresscode by passing weather code as
		// arguement
		int dressCode = getDressCode(code);

		// Locate the class table "OutfitImage" from Parse.com
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				"OutfitImage");
		// get the dresscode
		query.whereEqualTo("newcode", dressCode);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> imageList, ParseException e) {
				if (e == null) {
					Random generator = new Random();
					// random selection of dress from list
					int random = generator.nextInt(imageList.size());

					// locate column and create Object
					ParseFile fileObject = (ParseFile) imageList.get(random)
							.get("outfit");
					// get the image in background
					fileObject.getDataInBackground(new GetDataCallback() {
						public void done(byte[] data, ParseException e) {
							if (e == null) {
								Log.d("no error", "Image is present");

								Bitmap bitmap = BitmapFactory.decodeByteArray(
										data, 0, data.length);
								bitmap = Bitmap.createScaledBitmap(bitmap,
										1080, 1800, true);
								ImageView imageView = (ImageView) findViewById(R.id.image);
								if (bitmap != null) {
									// if bitmap created, set image with bitmap
									imageView.setImageBitmap(bitmap);
								} else {
									// if bitmap cudn't ceated display loading
									// image
									imageView.setImageDrawable(imageView
											.getContext().getResources()
											.getDrawable(R.drawable.loading1));
								}
							} else {
								Log.d("error", "Image could not be downloaded.");
							}
						}
					});

					Log.d("Image is ", "retrieved " + imageList.size()
							+ " bitmap");
				} else {
					Log.d("Fetching image ", "Error: " + e.getMessage());
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.parse, menu);
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

	/**
	 * This function gets the dresscode according to the weather code
	 * 
	 * @param code
	 *            int
	 * 
	 * @return dresscode int
	 */
	public int getDressCode(int code) {

		// create list of different dresscode
		Integer[] list1 = { 19, 32, 36 };
		Integer[] list2 = { 30, 34, 44 };
		Integer[] list3 = { 0, 2, 20, 21, 22, 26, 29, 37, 38, 39 };
		Integer[] list4 = { 8, 9, 23, 24, 27, 28, 33, 35, 47 };
		Integer[] list5 = { 5, 7, 15, 16, 17, 40, 41, 42, 43, 46 };
		Integer[] list6 = { 3, 4, 13, 14, 18, 45 };
		Integer[] list7 = { 1, 6, 7, 10, 11, 12, 25, 3200 };
		Integer[] list8 = { 31 };

		int dressCode;

		// check code if its present in the list and assign dresscode
		if (Arrays.asList(list1).contains(code)) {
			dressCode = 1;
		} else if (Arrays.asList(list2).contains(code)) {
			dressCode = 2;
		} else if (Arrays.asList(list3).contains(code)) {
			dressCode = 3;
		} else if (Arrays.asList(list4).contains(code)) {
			dressCode = 4;
		} else if (Arrays.asList(list5).contains(code)) {
			dressCode = 5;
		} else if (Arrays.asList(list6).contains(code)) {
			dressCode = 6;
		} else if (Arrays.asList(list7).contains(code)) {
			dressCode = 7;
		} else if (Arrays.asList(list8).contains(code)) {
			dressCode = 8;
		} else {
			dressCode = 7;
		}

		return dressCode;
	}
}
