
// Copyright (c) 2012-2013 Arvin Schnell

package com.example.helloremote;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class Preferences extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	addPreferencesFromResource(R.xml.preferences);
    }
}
