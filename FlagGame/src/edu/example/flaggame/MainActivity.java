package edu.example.flaggame;

import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	public static final String CHOICES="pref_numberOfChoices";
	public static final String REGIONS="pref_regionsToInclude";
	private boolean phoneDevice=true;
	private boolean preferencesChanged=true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
        String selectChoise = "2";        
        SharedPreferences sharedPreferences=getSharedPreferences("edu.example.flaggame_preferences", MODE_PRIVATE);
		SharedPreferences.Editor editorCho = sharedPreferences.edit();
		editorCho.putString(CHOICES, selectChoise);
		editorCho.commit();      
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferencesChangedListener);
        //Determination of the parameters of the device screen
        int screenSize=getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if(screenSize==Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
        	phoneDevice=false;
        Log.d("MyGame", "the display size is LARGE");
        //layout äëÿ òàáëåòêè âçÿòü
        if(phoneDevice)
        	//åñëè òåëåôîí òî èñïîëüçóåòñÿ òîëüêî â ïîðòðåòíîé îðèåíòàöèè
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   
        Log.d("MyGame", "the display size is for PHONE");
    }
    //äëÿ ïðàâåëüíîãî îáíîâëåíèÿ êîíôèãóðàöèè âî âðåìÿ èãðû âûçûâàåì ÎíÑòàðò
	@Override
	protected void onStart()
	{		
		super.onStart();
		if(preferencesChanged)
		{
			MainActivityFragment quizFragment=(MainActivityFragment)getFragmentManager().findFragmentById(R.id.quizFrag);
			quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
			quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
			quizFragment.resetQuiz();
			preferencesChanged=false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{				
		Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();//????????????????
		Point screenSize = new Point();
		display.getRealSize(screenSize);
		
		if(screenSize.x < screenSize.y)
		{
			getMenuInflater().inflate(R.menu.main,menu);
			return true;
		}
		else
			return false;	
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent preferencesIntent=new Intent(this,SettingsActivity.class);
		startActivity(preferencesIntent);
		return super.onOptionsItemSelected(item);
	}
	
	private OnSharedPreferenceChangeListener preferencesChangedListener= new OnSharedPreferenceChangeListener()
	{		
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
				String key)
		{
			preferencesChanged=true;		
			
			MainActivityFragment quizFragment=(MainActivityFragment)getFragmentManager().findFragmentById(R.id.quizFrag);
			if(key.equals(CHOICES))
			{
				quizFragment.updateGuessRows(sharedPreferences);
				quizFragment.resetQuiz();
			}
			else if (key.equals(REGIONS))
			{
				
				Set <String> regions = sharedPreferences.getStringSet(REGIONS, null);
				if(regions!=null && regions.size()>0)
				{
					quizFragment.updateRegions(sharedPreferences);
					quizFragment.resetQuiz();
				} 
				else
				{
					//èçìèíåíèå íàñòðîåê â øàðåäÏðåô ñ ïîìîùüþ editor è óñòàíîâêà èõ ñommit()!
					SharedPreferences.Editor editor = sharedPreferences.edit();
					regions.add(getResources().getString(R.string.default_region));
					editor.putStringSet(REGIONS, regions);					
					editor.commit();
					Toast.makeText(MainActivity.this, R.string.default_region_message, Toast.LENGTH_SHORT).show();
				}
			}
			Toast.makeText(MainActivity.this, R.string.restarting_quiz, Toast.LENGTH_SHORT).show();
			
		}
	};  

}
