package com.draketb.boggleme;

import android.content.Context;
import android.database.DataSetObserver;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BoggleMeActivity extends AppCompatActivity {
    private static final String TAG = BoggleMeActivity.class.getSimpleName();
    private static final int BOARD_SIZE = 16;
    private static final int TIMER_SECONDS = 120;
    private static final Die[] DICE = new Die[] {
        new Die("RIFOBX"), new Die("IFEHEY"), new Die("DENOWS"), new Die("UTOKND"),
        new Die("HMSRAO"), new Die("LUPETS"), new Die("ACITOA"), new Die("YLGKUE"),
        new Die(new String[] {"Qu", "B", "M", "J", "O", "A"}), new Die("EHISPN"), new Die("VETIGN"), new Die("BALIYT"),
        new Die("EZAVND"), new Die("RALESC"), new Die("UWILRG"), new Die("PACEMD")
    };

    private SensorManager mSensorManager;
    private final ShakeDetector mShakeDetector = new ShakeDetector();
    private GridView mBoardGrid = null;
    private ButtonGridAdapter mButtonGridAdapter = null;
    private Button mStartButton = null;
    private TextView mTimerText = null;
    private TextViewCountDownTimer mTextViewCountDownTimer;
    private BoggleDictionary mDictionary = new BoggleDictionary();

    private static class ButtonGridAdapter extends BaseAdapter {

        private final Button[] _buttons = new Button[BOARD_SIZE];

        public ButtonGridAdapter(LayoutInflater inflater) {
            for (int i = 0; i < _buttons.length; ++i) {
                _buttons[i] = (Button) inflater.inflate(R.layout.cell, null);
            }
        }

        void updateButtonTexts(String[] buttonTexts) {

            final Random random = new Random();
            for (int i = 0; i < _buttons.length; ++i) {
                final String text = i < buttonTexts.length ? buttonTexts[i] : "";
                final Button button = _buttons[i];
                button.setText(text);

                final int pivotX = random.nextInt(button.getWidth());
                final int pivotY = random.nextInt(button.getHeight());
                final int numRotations = 5;
                final int direction = random.nextBoolean() ? 1 : -1;
                final RotateAnimation animation = new RotateAnimation(0, direction * numRotations * 360, pivotX, pivotY);
                animation.setDuration(1000);
                button.startAnimation(animation);
            }
        }

        @Override
        public int getCount() {
            return _buttons.length;
        }

        @Override
        public Object getItem(int position) {
            return _buttons[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return position < _buttons.length ? _buttons[position] : null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the to receive shake events
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                Log.d(TAG, String.format("shake count: %d", count));

                final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);

                if (count > 5) {
                    updateBoard(getProposalBoard());
                } else {
                    updateBoard(getBoggleBoard());
                }

            }
        });

        setContentView(R.layout.title_screen);

        final Button startButton = (Button)findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_boggle_me);
                mBoardGrid = (GridView) findViewById(R.id.boardGrid);
                mButtonGridAdapter = new ButtonGridAdapter(getLayoutInflater());
                mBoardGrid.setAdapter(mButtonGridAdapter);
                mTimerText = (TextView) findViewById(R.id.timerText);
                setTimerTextView(TIMER_SECONDS);
                mStartButton = (Button) findViewById(R.id.startButton);
                mStartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTimer();
                    }
                });
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
            "Y", "O", "U", "M",
            "A", "R", "R", "Y",
            "M", "E", "?", "☺",
        };
    }

    private static class Die {
        private final String[] _faces;

        public Die(String[] faces) {
            _faces = faces;
        }

        public Die(String faces) {
            _faces = new String[faces.length()];
            for (int i = 0; i < _faces.length; ++i) {
                _faces[i] = faces.substring(i, i + 1);
            }
        }

        public String Roll() {
            if (_faces == null || _faces.length == 0) {
                return "";
            }

            return _faces[new Random().nextInt(_faces.length)];
        }
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
        }
    }
}
