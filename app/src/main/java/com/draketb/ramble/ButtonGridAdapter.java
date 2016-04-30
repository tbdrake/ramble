package com.draketb.ramble;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

class ButtonGridAdapter extends BaseAdapter {

    private enum ButtonState {
        Enabled,
        Disabled,
        Clicked,
    };

    private final int mRows;
    private final int mCols;
    private final int COLOR_ENABLED;
    private final int COLOR_DISABLED;
    private final int COLOR_CLICKED = Color.BLUE;
    private final List<Button> mButtons = new ArrayList<>();
    private final Map<Button, ButtonState> mButtonStates = new HashMap<>();
    private final ButtonClickListener mListener;

    public interface ButtonClickListener {
        void OnButtonClicked(String buttonText);
    }

    public ButtonGridAdapter(LayoutInflater inflater, int rows, int cols, ButtonClickListener listener) {
        mRows = rows;
        mCols = cols;
        mListener = listener;

        final Button testButton = (Button) inflater.inflate(R.layout.cell, null);
        testButton.setEnabled(true);
        COLOR_ENABLED = testButton.getCurrentTextColor();
        testButton.setEnabled(false);
        COLOR_DISABLED = testButton.getCurrentTextColor();

        for (int i = 0; i < (mRows * mCols); ++i) {
            final Button button = (Button) inflater.inflate(R.layout.cell, null);
            button.setEnabled(false);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonClicked((Button) v);
                }
            });

            mButtons.add(button);
            mButtonStates.put(button, ButtonState.Disabled);
        }
    }

    public void setAllButtonsEnabled(boolean enabled) {
        for (Button button : mButtons) {
            if (enabled) {
                setButtonEnabled(button);
            } else {
                setButtonDisabled(button);
            }
        }
    }

    public void updateButtonTexts(String[] buttonTexts) {
        for (int i = 0; i < mButtons.size(); ++i) {
            final String text = i < buttonTexts.length ? buttonTexts[i] : "";
            final Button button = mButtons.get(i);
            button.setText(text);

            startRollAnimation(button);
        }
    }

    private void startRollAnimation(Button button) {
        final Random random = new Random();
        final int pivotX = random.nextInt(button.getWidth());
        final int pivotY = random.nextInt(button.getHeight());
        final int numRotations = 5;
        final int direction = random.nextBoolean() ? 1 : -1;
        final RotateAnimation animation = new RotateAnimation(0, direction * numRotations * 360, pivotX, pivotY);
        animation.setDuration(1000);
        button.startAnimation(animation);
    }

    @Override
    public int getCount() {
        return mButtons.size();
    }

    @Override
    public Object getItem(int position) {
        return getButton(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getButton(position);
    }

    private Button getButton(int index) {
        return index < mButtons.size() ? mButtons.get(index) : null;
    }

    private void setButtonEnabled(Button button) {
        button.setEnabled(true);
        button.setTextColor(COLOR_ENABLED);
        mButtonStates.put(button, ButtonState.Enabled);
    }

    private void setButtonDisabled(Button button) {
        button.setEnabled(false);
        button.setTextColor(COLOR_DISABLED);
        mButtonStates.put(button, ButtonState.Disabled);
    }

    private void setButtonClicked(Button button) {
        button.setEnabled(false);
        button.setTextColor(COLOR_CLICKED);
        mButtonStates.put(button, ButtonState.Clicked);
    }

    private void onButtonClicked(Button button) {
        final int index = mButtons.indexOf(button);
        if (index == -1) {
            return;
        }

        setButtonClicked(button);

        final int row = index / mCols;
        final int col = index % mCols;

        for (int i = 0; i < mButtons.size(); ++i) {
            final Button b = mButtons.get(i);
            final ButtonState prevState = mButtonStates.get(b);
            if (prevState == ButtonState.Clicked) {
                continue;
            }

            final int r = i / mCols;
            final int c = i % mCols;

            if (((row - 1) <= r) && (r <= (row + 1)) && ((col - 1) <= c) && (c <= (col + 1))) {
                setButtonEnabled(b);
            } else {
                setButtonDisabled(b);
            }
        }

        if (mListener != null) {
            mListener.OnButtonClicked(button.getText().toString());
        }
    }
}