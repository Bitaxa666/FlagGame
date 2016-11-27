package edu.example.flaggame;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsActivityFragment extends PreferenceFragment
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{	
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	

}
