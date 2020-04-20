package com.draketb.ramble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {
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
    private GridView mWordList = null;
    private DieViewGridAdapter mDieViewGridAdapter = null;
    private TextView mTimerText = null;
    private int mTimerSecondsRemaining = 0;
    private TextView mCurrentWordText = null;
    private GameCountDownTimer mGameCountDownTimer;
    private SynchronizedTreeSetWordList mDictionary = new SynchronizedTreeSetWordList();
    private List<String> mWordsFound = new ArrayList<>();
    private ArrayAdapter<String> mWordsFoundAdapter;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_S) {
            startGame(false);
        }
        return false;
    }

    private void startGame(boolean showProposal) {
        if (mBoardGrid == null) {
            return;
        }

        mVibrator.vibrate(100);

        final String[] board = showProposal ? getProposalBoard() : getBoggleBoard(3);
        updateBoard(board, true);
        startTimer();
    }

    private void registerShakeDetector() {
        mSensorManager.registerListener(mShakeDetector, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                Log.d(TAG, String.format("shake count: %d", count));
                boolean showProposal = count > 3;
                startGame(showProposal);
            }
        });
    }

    private void unregisterShakeDetector() {
        mShakeDetector.setOnShakeListener(null);
        mSensorManager.unregisterListener(mShakeDetector);
    }

    private void clearWordsFound() {
        mWordsFound.clear();
        mWordsFoundAdapter.clear();
        mWordsFoundAdapter.notifyDataSetChanged();
    }

    private int getScore() {
        int score = 0;
        for (String s : mWordsFound) {
            score += s.length() - 2;
        }
        return score;
    }

    private void checkCurrentWord() {
        final String word = mCurrentWordText.getText().toString().toLowerCase();
        if (word.length() >= 3 && mDictionary.containsWord(word)) {
            if (mWordsFound.contains(word)) {
                // Indicate duplicate word in UI
                mDieViewGridAdapter.duplicateWordFound();
            } else {
                mWordsFound.add(word);
                mWordsFoundAdapter.notifyDataSetChanged();

                // Indicate new word in UI
                mDieViewGridAdapter.newWordFound();
            }
        } else {
            // Indicate not a word in UI
            mDieViewGridAdapter.invalidWordFound();
        }

        mDieViewGridAdapter.resetButtons();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Set up the to receive shake events
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        setContentView(R.layout.main_activity);
        mBoardGrid = (GridView) findViewById(R.id.boardGrid);
        mBoardGrid.setNumColumns(BOARD_SIZE);
        mDieViewGridAdapter = new DieViewGridAdapter(this, BOARD_SIZE, BOARD_SIZE, new DieViewGridAdapter.WordListener() {
            @Override
            public void OnWordChanged(String word) {
                mCurrentWordText.setText(word);
            }
        });
        mBoardGrid.setAdapter(mDieViewGridAdapter);
        mBoardGrid.setOnItemClickListener(mDieViewGridAdapter);
        mWordList = (GridView) findViewById(R.id.wordList);
        mWordList.setNumColumns(4);
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

        final String[] titleBoard = new String[]{
                "E", "P", "W", "T",
                "R", "A", "M", "Y",
                "G", "B", "L", "E",
                "V", "D", "R", "A",
        };
        updateBoard(titleBoard, false);
        mDieViewGridAdapter.setButtonColorClicked(1, 0);
        mDieViewGridAdapter.setButtonColorClicked(1, 1);
        mDieViewGridAdapter.setButtonColorClicked(1, 2);
        mDieViewGridAdapter.setButtonColorClicked(2, 1);
        mDieViewGridAdapter.setButtonColorClicked(2, 2);
        mDieViewGridAdapter.setButtonColorClicked(2, 3);

        // Load dictionary in background
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mDictionary.load(getAssets());
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
            }
        }.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerShakeDetector();
        resumeTimer();
    }

    @Override
    protected void onPause() {
        pauseTimer();
        unregisterShakeDetector();
        super.onPause();
    }

    private void startTimer() {
        if (mGameCountDownTimer != null) {
            mGameCountDownTimer.cancel();
        }
        mGameCountDownTimer = new GameCountDownTimer(TIMER_SECONDS);
        mGameCountDownTimer.start();

        clearWordsFound();
        mDieViewGridAdapter.resetButtons();
        mDieViewGridAdapter.setClickEnabled(true);
    }

    private void pauseTimer() {
        if (mGameCountDownTimer != null) {
            mGameCountDownTimer.cancel();
            mGameCountDownTimer = null;
        }
    }

    private void resumeTimer() {
        if (mGameCountDownTimer == null && mTimerSecondsRemaining > 0) {
            mGameCountDownTimer = new GameCountDownTimer(mTimerSecondsRemaining);
            mGameCountDownTimer.start();
        }
    }

    private void updateBoard(String[] board, boolean animate) {
        mDieViewGridAdapter.updateButtonTexts(board, animate);
    }

    private static String[] getBoggleBoard(int minVowelCount) {
        final List<Die> dice = Arrays.asList(DICE);
        Collections.shuffle(dice);

        // Loop to create a board with at least minVowelCount vowels
        final List<String> board = new ArrayList<>();
        int vowelCount = 0;
        while (vowelCount < minVowelCount) {
            board.clear();
            vowelCount = 0;
            for (Die die : dice) {
                final String c = die.Roll();
                if (isVowel(c)) {
                    vowelCount += 1;
                }
                board.add(die.Roll());
            }
        }

        return board.toArray(new String[board.size()]);
    }

    static boolean isVowel(String dieFace) {
        switch (dieFace.toUpperCase()) {
            case "A":
            case "E":
            case "I":
            case "O":
            case "U":
                return true;
        }
        return false;
    }

    private static String[] getProposalBoard() {
        return new String[]{
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

    void setTimerSecondsRemaining(int secondsRemaining) {
        mTimerSecondsRemaining = secondsRemaining;
        mTimerText.setText(formatSeconds(mTimerSecondsRemaining));
    }

    private class GameCountDownTimer extends CountDownTimer {
        public GameCountDownTimer(int seconds) {
            super(seconds * 1000, 100);
            setTimerSecondsRemaining(seconds);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            setTimerSecondsRemaining(Math.round(millisUntilFinished / 1000f));
        }

        @Override
        public void onFinish() {
            setTimerSecondsRemaining(0);
            mGameCountDownTimer = null;
            mDieViewGridAdapter.resetButtons();
            mDieViewGridAdapter.setClickEnabled(false);
            mGameCountDownTimer = null; // Set to null to indicate the the timer is not running

            unregisterShakeDetector();
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder
                    .setTitle("Time's Up!")
                    .setMessage(String.format("Score: %d", getScore()))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            registerShakeDetector();
                        }
                    });
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
}
