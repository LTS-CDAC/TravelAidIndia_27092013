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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;

public class TravelPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     // Load the XML preferences file
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences()
		.registerOnSharedPreferenceChangeListener(this);
        
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        
        if(!sp.getBoolean("key_preference_read_search_results", false)){
        	getPreferenceScreen().findPreference("Key_preference_stop_tts_on_shake").setEnabled(false);
        }else{
        	getPreferenceScreen().findPreference("Key_preference_stop_tts_on_shake").setEnabled(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_preference, menu);
        return true;
    }

    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
    	    String key) {
    	System.out.println("dfhgsd");
    	if (key.equals("key_preference_read_search_results")) {
    	   
    	    if(sharedPreferences.getBoolean(key, false)){
    	    	getPreferenceScreen().findPreference("Key_preference_stop_tts_on_shake").setEnabled(true);
    	    }
    	    else{
    	    	getPreferenceScreen().findPreference("Key_preference_stop_tts_on_shake").setEnabled(false);
    	    }
    	}
    	
    }
    
}
