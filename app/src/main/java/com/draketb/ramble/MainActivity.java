package com.draketb.ramble;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity
        extends AppCompatActivity
        implements ButtonGridAdapter.ButtonClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int BOARD_SIZE = 4;
    private static final int TIMER_SECONDS = 120;
    private static final Die[] DICE = new Die[] {
        new Die("RIFOBX"), new Die("IFEHEY"), new Die("DENOWS"), new Die("UTOKND"),
        new Die("HMSRAO"), new Die("LUPETS"), new Die("ACITOA"), new Die("YLGKUE"),
        new Die(new String[] {"Qu", "B", "M", "J", "O", "A"}), new Die("EHISPN"), new Die("VETIGN"), new Die("BALIYT"),
        new Die("EZAVND"), new Die("RALESC"), new Die("UWILRG"), new Die("PACEMD")
    };

    private SensorManager mSensorManager;
    private final ShakeDetector mShakeDetector = new ShakeDetector();
    private Vibrator mVibrator = null;
    private GridView mBoardGrid = null;
    private ListView mWordList = null;
    private ButtonGridAdapter mButtonGridAdapter = null;
    private TextView mTimerText = null;
    private TextView mCurrentWordText = null;
    private TextViewCountDownTimer mTextViewCountDownTimer;
    private WordList mDictionary = new WordList();
    private List<String> mWordsFound = new ArrayList<>();
    private ArrayAdapter<String> mWordsFoundAdapter;

    @Override
    public void OnButtonClicked(String buttonText) {
        mCurrentWordText.setText(mCurrentWordText.getText().toString().concat(buttonText));
    }

    private void clearCurrentWord() {
        mCurrentWordText.setText("");
    }

    private void addWordFound(String word) {
        if (mWordsFound.contains(word)) {
            return; // Do not add the same word twice
        }

        mWordsFound.add(0, word);
        mWordsFoundAdapter.notifyDataSetChanged();
    }

    private void clearWordsFound() {
        mWordsFound.clear();
        mWordsFoundAdapter.clear();
        mWordsFoundAdapter.notifyDataSetChanged();
    }

    private void checkCurrentWord() {
        final String currentWord = mCurrentWordText.getText().toString().toLowerCase();
        if (currentWord.length() >= 3 && mDictionary.containsWord(currentWord)) {
            addWordFound(currentWord);
        } else {
            mVibrator.vibrate(50);
        }
        clearCurrentWord();
        mButtonGridAdapter.setAllButtonsEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Set up the to receive shake events
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                Log.d(TAG, String.format("shake count: %d", count));

                mVibrator.vibrate(100);

                if (count > 3) {
                    updateBoard(getProposalBoard());
                } else {
                    updateBoard(getBoggleBoard());
                }

                startTimer();
            }
        });

        setContentView(R.layout.title_screen);

        final Button startButton = (Button)findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.main_activity);
                mBoardGrid = (GridView) findViewById(R.id.boardGrid);
                mBoardGrid.setNumColumns(BOARD_SIZE);
                mButtonGridAdapter = new ButtonGridAdapter(getLayoutInflater(), BOARD_SIZE, BOARD_SIZE, MainActivity.this);
                mBoardGrid.setAdapter(mButtonGridAdapter);
                mWordList = (ListView) findViewById(R.id.wordList);
                mWordsFoundAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.found_word_text, mWordsFound);
                mWordList.setAdapter(mWordsFoundAdapter);
                mTimerText = (TextView) findViewById(R.id.timerText);
                mCurrentWordText = (TextView) findViewById(R.id.currentWordText);
                mCurrentWordText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkCurrentWord();
                    }
                });
                setTimerTextView(TIMER_SECONDS);
            }
        });

        // Load dictionary in background
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mDictionary.load(getAssets());
                return null;
            }
        }.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    private void startTimer() {
        if (mTextViewCountDownTimer != null) {
            mTextViewCountDownTimer.cancel();
        }
        mTextViewCountDownTimer = new TextViewCountDownTimer(TIMER_SECONDS);
        mTextViewCountDownTimer.start();

        clearCurrentWord();
        clearWordsFound();
        mButtonGridAdapter.setAllButtonsEnabled(true);
    }

    private void updateBoard(String[] board) {
        mButtonGridAdapter.updateButtonTexts(board);
    }

    private static String[] getBoggleBoard() {
        final List<Die> dice = Arrays.asList(DICE);
        Collections.shuffle(dice);

        final List<String> board = new ArrayList<>();
        for (Die die : dice) {
            board.add(die.Roll());
        }

        return board.toArray(new String[board.size()]);
    }


    private static String[] getProposalBoard() {
        return new String[] {
            "W", "I", "L", "L",
            "Y", "O", "U", "Y",
            "M", "A", "R", "R",
            "M", "E", "?", "☺",
        };
    }

    private static String formatSeconds(int seconds) {
        final int m = seconds / 60;
        final int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    void setTimerTextView(int secondsRemaining) {
        mTimerText.setText(formatSeconds(secondsRemaining));
    }

    private class TextViewCountDownTimer extends CountDownTimer {
        public TextViewCountDownTimer(int seconds) {
            super(seconds * 1000, 100);
            setTimerTextView(seconds);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            setTimerTextView(Math.round(millisUntilFinished / 1000f));
        }

        @Override
        public void onFinish() {
            setTimerTextView(0);
            mTextViewCountDownTimer = null;
            mButtonGridAdapter.setAllButtonsEnabled(false);
        }
    }
}
