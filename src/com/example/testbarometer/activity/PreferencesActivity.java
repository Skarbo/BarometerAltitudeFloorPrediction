package com.example.testbarometer.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.example.testbarometer.helper.DropboxHelper;

public class PreferencesActivity extends PreferenceActivity {

	private static final String TAG = PreferencesActivity.class.getSimpleName();

	private DropboxHelper dropboxHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dropboxHelper = new DropboxHelper(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.dropboxHelper.doLogin();

		setPreferenceScreen(createPreferenceHierarchy());
	}

	private PreferenceScreen createPreferenceHierarchy() {
		// Root
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

		// Launch preferences
		PreferenceCategory syncPrefCat = new PreferenceCategory(this);
		syncPrefCat.setTitle("Sync accounts");
		root.addPreference(syncPrefCat);

		Preference syncDropboxSwitchPref = null;
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			syncDropboxSwitchPref = createSyncDropboxSwitchPref(syncPrefCat);
		} else {
			syncDropboxSwitchPref = new CheckBoxPreference(this);
		}
		syncDropboxSwitchPref.setKey("sync_dropbox");
		syncDropboxSwitchPref.setTitle("Dropbox");
		syncDropboxSwitchPref.setSummary(this.dropboxHelper.isLoggedIn() ? "Linked" : "Not linked");
		syncDropboxSwitchPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				doSyncDropbox((Boolean) newValue);
				return true;
			}
		});
		syncPrefCat.addPreference(syncDropboxSwitchPref);

		return root;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public Preference createSyncDropboxSwitchPref(PreferenceCategory syncPrefCat) {
		return new SwitchPreference(this);
	}

	protected void doSyncDropbox(Boolean isSync) {
		if (isSync) {
			this.dropboxHelper.getDropboxApi().getSession().startAuthentication(PreferencesActivity.this);
		} else {
			this.dropboxHelper.doLogout();
		}
	}

}
