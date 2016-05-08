package com.draketb.ramble;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

class DieViewGridAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private final Activity mActivity;
    private final int mRows;
    private final int mCols;
    private final List<DieView> mDieViews = new ArrayList<>();
    private final Stack<DieView> mClickedDieViews = new Stack<>();
    private final WordListener mListener;
    private boolean mClickEnabled = false;

    public void duplicateWordFound() {
        final DieView[] dieViews = mClickedDieViews.toArray(new DieView[0]);
        startBackgroundColorAnimation(dieViews, mActivity.getResources().getColor(R.color.dieDuplicateWordColor), 250);
    }

    public void newWordFound() {
        final DieView[] dieViews = mClickedDieViews.toArray(new DieView[0]);
        startBackgroundColorAnimation(dieViews, mActivity.getResources().getColor(R.color.dieNewWordColor), 250);
    }

    public void invalidWordFound() {
        final DieView[] dieViews = mClickedDieViews.toArray(new DieView[0]);
        startBackgroundColorAnimation(dieViews, mActivity.getResources().getColor(R.color.dieInvalidWordColor), 250);
    }

    public interface WordListener {
        void OnWordChanged(String word);
    }

    public DieViewGridAdapter(Activity activity, int rows, int cols, WordListener listener) {
        mActivity = activity;
        mRows = rows;
        mCols = cols;
        mListener = listener;

        for (int i = 0; i < (mRows * mCols); ++i) {
            mDieViews.add((DieView) mActivity.getLayoutInflater().inflate(R.layout.die, null));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!mClickEnabled) {
            return;
        }

        final DieView dieView = (DieView) view;

        // Check if dieView has already been clicked
        if (mClickedDieViews.contains(dieView)) {
            if (mClickedDieViews.peek() == dieView) {
                // Undo click
                dieView.setTextColor(mActivity.getResources().getColor(R.color.dieNormalTextColor));
                dieView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                mClickedDieViews.pop();

                if (mListener != null) {
                    mListener.OnWordChanged(getCurrentWord());
                }
            }

            return;
        }

        // Check if dieView position is valid
        if (!mClickedDieViews.empty() && !areButtonsAdjacent(mClickedDieViews.peek(), dieView)) {
            return;
        }

        dieView.setTextColor(mActivity.getResources().getColor(R.color.dieClickedTextColor));
        dieView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        mClickedDieViews.push(dieView);

        if (mListener != null) {
            mListener.OnWordChanged(getCurrentWord());
        }
    }

    public void resetButtons() {
        for (DieView dieView : mDieViews) {
            dieView.setTextColor(mActivity.getResources().getColor(R.color.dieNormalTextColor));
        }

        mClickedDieViews.clear();

        if (mListener != null) {
            mListener.OnWordChanged("");
        }
    }

    public void setClickEnabled(boolean clickEnabled) {
        mClickEnabled = clickEnabled;
    }

    public void updateButtonTexts(String[] dieViewTexts, boolean animate) {

        final Random random = new Random();
        for (int i = 0; i < mDieViews.size(); ++i) {
            final String text = i < dieViewTexts.length ? dieViewTexts[i] : "";
            final DieView dieView = mDieViews.get(i);
            dieView.setText(text);

            if (animate) {
                startRotationAnimation(
                        dieView,
                        5 * (random.nextBoolean() ? 1 : -1),
                        1000,
                        random.nextInt(dieView.getWidth()),
                        random.nextInt(dieView.getHeight()));
            }
        }
    }

    private void startRotationAnimation(DieView dieView, int numRotations, int durationMs, int pivotX, int pivotY) {
        final RotateAnimation animation = new RotateAnimation(0, numRotations * 360, pivotX, pivotY);
        animation.setDuration(durationMs);
        dieView.startAnimation(animation);
    }

    private void startBackgroundColorAnimation(final DieView[] dieViews, int color, int durationMs) {
        final int strokeWidth = mActivity.getResources().getDimensionPixelSize(R.dimen.dieStrokeWidth);
        final int strokeColor = mActivity.getResources().getColor(R.color.dieStrokeColor);
        final ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), color, strokeColor);
        animator.setDuration(durationMs);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int color = (int) animation.getAnimatedValue();
                for (DieView dieView : dieViews) {
                    final GradientDrawable background = (GradientDrawable) dieView.getBackground();
                    background.setStroke(strokeWidth, color);
                }
            }
        });
        animator.start();
    }

    @Override
    public int getCount() {
        return mDieViews.size();
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

    public void setButtonColorClicked(int row, int col) {
        final DieView dieView = getButton(row * mCols + col);
        if (dieView != null) {
            setButtonColorClicked(dieView);
        }
    }

    private DieView getButton(int index) {
        return index < mDieViews.size() ? mDieViews.get(index) : null;
    }

    private void setButtonColorClicked(DieView dieView) {
        dieView.setTextColor(mActivity.getResources().getColor(R.color.dieClickedTextColor));
    }

    private boolean areButtonsAdjacent(DieView dieView1, DieView dieView2) {
        final int index1 = mDieViews.indexOf(dieView1);
        final int row1 = index1 / mCols;
        final int col1 = index1 % mRows;

        final int index2 = mDieViews.indexOf(dieView2);
        final int row2 = index2 / mCols;
        final int col2 = index2 % mCols;

        return (row1 - 1) <= row2 && row2 <= (row1 + 1)
                && (col1 - 1) <= col2 && col2 <= (col1 + 1);
    }

    private String getCurrentWord() {
        final StringBuilder builder = new StringBuilder(mClickedDieViews.size());
        for (DieView dieView : mClickedDieViews) {
            builder.append(dieView.getText());
        }
        return builder.toString();
    }
}