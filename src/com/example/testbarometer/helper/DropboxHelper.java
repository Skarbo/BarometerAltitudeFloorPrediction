package com.example.testbarometer.helper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public class DropboxHelper {

	private static final String TAG = DropboxHelper.class.getSimpleName();

	final static private String DROPBOX_APP_KEY = "3fhnb94bjg1uk2d";
	final static private String DROPBOX_APP_SECRET = "m1z7uzqni9g322u";

	final static private String DROPBOX_ACCOUNT_PREFS_NAME = "prefs";
	final static private String DROPBOX_ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String DROPBOX_ACCESS_SECRET_NAME = "ACCESS_SECRET";
	final static private AccessType DROPBOX_ACCESS_TYPE = AccessType.APP_FOLDER;

	private Context context;
	private DropboxAPI<AndroidAuthSession> dropboxApi;

	private boolean loggedIn;

	public DropboxHelper(Context context) {
		this.context = context;
		AndroidAuthSession session = createSession();
		this.dropboxApi = new DropboxAPI<AndroidAuthSession>(session);

		this.setLoggedIn(this.dropboxApi.getSession().isLinked());
	}

	// ... GET

	public DropboxAPI<AndroidAuthSession> getDropboxApi() {
		return this.dropboxApi;
	}

	private String[] getKeys() {
		SharedPreferences prefs = this.context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(DROPBOX_ACCESS_KEY_NAME, null);
		String secret = prefs.getString(DROPBOX_ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	// ... /GET

	// ... SET

	private void setLoggedIn(boolean isLoggedIn) {
		this.loggedIn = isLoggedIn;
	}

	// ... /SET

	// ... IS

	public boolean isLoggedIn() {
		return this.loggedIn;
	}

	// ... /IS

	// ... CREATE

	private AndroidAuthSession createSession() {
		AppKeyPair appKeyPair = new AppKeyPair(DROPBOX_APP_KEY, DROPBOX_APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
			session = new AndroidAuthSession(appKeyPair, DROPBOX_ACCESS_TYPE, accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, DROPBOX_ACCESS_TYPE);
		}

		return session;
	}

	// ... /CREATE

	// ... DO

	public void doLogin() {
		AndroidAuthSession session = this.dropboxApi.getSession();

		if (session.authenticationSuccessful()) {
			try {
				session.finishAuthentication();

				TokenPair tokens = session.getAccessTokenPair();
				doStoreKeys(tokens.key, tokens.secret);
				setLoggedIn(true);
			} catch (IllegalStateException e) {
				Log.e(TAG, "Error authenticating", e);
			}
		}
	}

	public void doLogout() {
		dropboxApi.getSession().unlink();

		doClearKeys();
		setLoggedIn(false);
	}

	private void doStoreKeys(String key, String secret) {
		// Save the access key for later
		SharedPreferences prefs = this.context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.putString(DROPBOX_ACCESS_KEY_NAME, key);
		edit.putString(DROPBOX_ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	private void doClearKeys() {
		SharedPreferences prefs = this.context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	public void doUploadText(String filename, String filecontent, ProgressListener progressListener) {
		// Uploading content.
		InputStream inputStream = null;
		try {
			List<ByteArrayInputStream> streams = Arrays.asList(new ByteArrayInputStream(filecontent.getBytes()));
			inputStream = new SequenceInputStream(Collections.enumeration(streams));

			Entry newEntry = dropboxApi.putFileOverwrite("/" + filename, inputStream, filecontent.getBytes().length,
					progressListener);
			Log.d(TAG, "The uploaded file's rev is: " + newEntry.rev);
		} catch (DropboxUnlinkedException e) {
			// User has unlinked, ask them to link again here.
			// Toast.makeText(this.context,
			// "Unlinked from Dropbox, please link again",
			// Toast.LENGTH_SHORT).show();
			Log.e(TAG, "User has unlinked.");
		} catch (Exception e) {
			// Toast.makeText(this.context,
			// "Something went wrong while uploading Dropbox file",
			// Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Something went wrong while uploading.");
		}
	}

	// ... /DO

}
