package com.draketb.ramble;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

class ButtonGridAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private enum ButtonState {
        Enabled,
        Disabled,
        Clicked,
    };

    private final int mRows;
    private final int mCols;
    private final int COLOR_NORMAL = Color.BLACK;
    private final int COLOR_CLICKED = Color.BLUE;
    private final List<SquareTextView> mButtons = new ArrayList<>();
    private final Map<SquareTextView, ButtonState> mButtonStates = new HashMap<>();
    private final ButtonClickListener mListener;

    public interface ButtonClickListener {
        void OnButtonClicked(String buttonText);
    }

    public ButtonGridAdapter(LayoutInflater inflater, int rows, int cols, ButtonClickListener listener) {
        mRows = rows;
        mCols = cols;
        mListener = listener;

        for (int i = 0; i < (mRows * mCols); ++i) {
            final SquareTextView button = (SquareTextView) inflater.inflate(R.layout.die, null);

            mButtons.add(button);
            mButtonStates.put(button, ButtonState.Disabled);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onButtonClicked((SquareTextView) view);
    }

    public void setAllButtonsEnabled(boolean enabled) {
        for (SquareTextView button : mButtons) {
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
            final SquareTextView button = mButtons.get(i);
            button.setText(text);

            startRollAnimation(button);
        }
    }

    private void startRollAnimation(SquareTextView button) {
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
        return position + 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getButton(position);
    }

    private SquareTextView getButton(int index) {
        return index < mButtons.size() ? mButtons.get(index) : null;
    }

    private void setButtonEnabled(SquareTextView button) {
        button.setTextColor(COLOR_NORMAL);
        mButtonStates.put(button, ButtonState.Enabled);
    }

    private void setButtonDisabled(SquareTextView button) {
        button.setTextColor(COLOR_NORMAL);
        mButtonStates.put(button, ButtonState.Disabled);
    }

    private void setButtonClicked(SquareTextView button) {
        button.setTextColor(COLOR_CLICKED);
        mButtonStates.put(button, ButtonState.Clicked);
    }

    private void onButtonClicked(SquareTextView button) {
        final int index = mButtons.indexOf(button);
        if (index == -1) {
            return;
        }

        switch (mButtonStates.get(button)) {
            case Enabled:
                break;
            default:
                return;
        }

        setButtonClicked(button);

        final int row = index / mCols;
        final int col = index % mCols;

        for (int i = 0; i < mButtons.size(); ++i) {
            final SquareTextView b = mButtons.get(i);
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