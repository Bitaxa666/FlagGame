package edu.example.flaggame;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivityFragment extends Fragment 
{
	private static final String TAG = "FlagGame Activity";
	private static final int FLAGS_IN_QUIZ=10;
	private static final int ANIM_COUNT=3;
	
	private List <String> fileNameList;
	private List <String> quizCountriesList;
	private Set <String> regionsSet;
	
	private String correctAnswer;
	private int totalGueses;
	private int correctAnswers;
	private int guessesRows;
	private SecureRandom random;
	private Handler handler;
	private Animation shakeAnimation;
	private LinearLayout quizLinerLayout;
	private TextView questionNumberTextView;
	private ImageView flagImageView;
	private LinearLayout[] guessLinearLayouts;
	private TextView answerTextView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v=inflater.inflate(R.layout.fragment_main, container, false);
		fileNameList=new ArrayList<String>();
		quizCountriesList=new ArrayList<String>();
		random=new SecureRandom();
		handler=new Handler();
		shakeAnimation=AnimationUtils.loadAnimation(getActivity(), R.anim.incorrect);
		shakeAnimation.setRepeatCount(ANIM_COUNT);//repetitions of the anim
		
		quizLinerLayout=(LinearLayout) v.findViewById(R.id.quizLinearLayout);
		questionNumberTextView=(TextView) v.findViewById(R.id.questionNumberTextView);//dynamically chenge string
		flagImageView=(ImageView) v.findViewById(R.id.flagImageView);
		guessLinearLayouts=new LinearLayout[4];
		guessLinearLayouts[0]=(LinearLayout) v.findViewById(R.id.row1LinearLayout);
		guessLinearLayouts[1]=(LinearLayout) v.findViewById(R.id.row2LinearLayout);
		guessLinearLayouts[2]=(LinearLayout) v.findViewById(R.id.row3LinearLayout);
		guessLinearLayouts[3]=(LinearLayout) v.findViewById(R.id.row4LinearLayouts);
		answerTextView=(TextView) v.findViewById(R.id.answerTextView);
		
		for(LinearLayout row : guessLinearLayouts)
		{
			for(int column=0;column<row.getChildCount();column++)
			{
				Button button=(Button) row.getChildAt(column);
				button.setOnClickListener(guessButtonListener);
			}
		}
		questionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));//Chenge the String. Start game with 1!
		return v;
	}
	
	public void updateGuessRows (SharedPreferences sharedPreferences)
	{
		String choices=sharedPreferences.getString(MainActivity.CHOICES, null);		
		guessesRows=Integer.parseInt(choices)/2;
	
		
		for(LinearLayout layout:guessLinearLayouts)
		{
			//layout.setVisibility(View.GONE);
			layout.setVisibility(View.INVISIBLE);
		}
		for(int row = 0;row<guessesRows; row++)
		{
			guessLinearLayouts[row].setVisibility(View.VISIBLE);
		}
	}
	
	public void updateRegions(SharedPreferences sharedPreferences)
	{
		regionsSet=sharedPreferences.getStringSet(MainActivity.REGIONS, null);
	}
	
	public void resetQuiz()
	{
		//turning to the asset folder with command -  AssetManager
		AssetManager assets=getActivity().getAssets();
		fileNameList.clear();
		
		try{
			for(String region : regionsSet)
			{
				String[] paths = assets.list(region);
				for(String path : paths)
				{
					fileNameList.add(path.replace(".png", ""));
				}
			}
		} catch (IOException ex)
		{
			Log.e(TAG, "Error loading image file names", ex);
		}
		
		correctAnswers=0;
		totalGueses=0;
		quizCountriesList.clear();
		int flagCounter=1;
		int numberOfFlags = fileNameList.size();
		while(flagCounter<=FLAGS_IN_QUIZ)
		{
			int randomIndex=random.nextInt(numberOfFlags);
			String filename=fileNameList.get(randomIndex);
			if(!quizCountriesList.contains(filename))
			{
				quizCountriesList.add(filename);
				++flagCounter;
			}
		}
		loadNextFlags();
	  }
	
	private void loadNextFlags()
	{
		String nextImage=quizCountriesList.remove(0);
		correctAnswer=nextImage;
		answerTextView.setText("");
		//TODO
		questionNumberTextView.setText(getString(R.string.question, (correctAnswers+1), FLAGS_IN_QUIZ));//String with new count of correct answer!
		String region=nextImage.substring(0, nextImage.indexOf('-'));
		AssetManager assets=getActivity().getAssets();
		
		try(InputStream stream=assets.open(region + "/" + nextImage + ".png"))
		{
			Drawable flag=Drawable.createFromStream(stream, nextImage);
			flagImageView.setImageDrawable(flag);
			//animate(false);
		} catch(IOException ex)
		{
			Log.e(TAG, "Error loading" + nextImage, ex);
		}
		
		Collections.shuffle(fileNameList);
		int correct=fileNameList.indexOf(correctAnswer);
		fileNameList.add(fileNameList.remove(correct));
		
		for(int row=0; row<guessesRows;row++)
		{
			for(int column=0; column<guessLinearLayouts[row].getChildCount();column++)
			{
				Button newGuessButton=(Button) guessLinearLayouts[row].getChildAt(column);
				newGuessButton.setEnabled(true);
				String filename=fileNameList.get((row*2)+column);
				newGuessButton.setText(getCountryName(filename));
				newGuessButton.setBackgroundResource(R.drawable.flag_btn);
			}
		}
		
		int row=random.nextInt(guessesRows);
		int column=random.nextInt(2);
		LinearLayout randomRow=guessLinearLayouts[row];
		String countryName = getCountryName(correctAnswer);
		((Button)randomRow.getChildAt(column)).setText(countryName);
	}
	
	private String getCountryName(String name)
	{
		return name.substring(name.indexOf('-')+1).replace('_', ' ');
	}	

	private OnClickListener guessButtonListener = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{	
			Button guessButton = ((Button)v);
			String guess=guessButton.getText().toString();
			String answer=getCountryName(correctAnswer);
			++totalGueses;
			if(guess.equals(answer))
			{
				++correctAnswers;
				answerTextView.setText(answer + "!");
				answerTextView.setTextColor(getResources().getColor(R.color.correct_answer));
				disableButtons();
				
				if(correctAnswers==FLAGS_IN_QUIZ)
				{
					DialogFragment quizResults=new DialogFragment()
					{
						@Override
						public Dialog onCreateDialog(Bundle bundle)
						{
							AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
							builder.setMessage(getResources().getString(R.string.results,totalGueses,(1000/(double) totalGueses)));
							builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() 
							{								
								@Override
								public void onClick(DialogInterface dialog, int which) 
								{
									resetQuiz();
									
								}
							});
							return builder.create();
						}
					};
					quizResults.setCancelable(false);
					quizResults.show(getFragmentManager(), "quiz rezults");
				}
				else
				{
					handler.postDelayed(new Runnable()
					{
						@Override
						public void run()
						{					
							loadNextFlags();
						}
					}, 2000);
				}
			}
			else
			{
				flagImageView.startAnimation(shakeAnimation);
				answerTextView.setText(R.string.incorrect_answer);
				answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));
				guessButton.setBackgroundResource(R.drawable.btn_flag_false);//
				
				guessButton.setEnabled(false);
			}
			
		}
	};
	private void disableButtons()
	{
		for(int row=0;row<guessesRows;row++)
		{
			LinearLayout guessRow=guessLinearLayouts[row];
			for(int i=0;i<guessRow.getChildCount();i++)
			{
				guessRow.getChildAt(i).setEnabled(false);
			}
			
			
		}
		
	}	

}
