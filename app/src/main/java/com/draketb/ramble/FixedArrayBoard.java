package com.draketb.ramble;

/**
 * Created by draketb on 5/5/16.
 */
public class FixedArrayBoard implements Board {
    private final int mNumRows;
    private final int mNumCols;
    private final String[] mDieFaces;

    public FixedArrayBoard(int numRows, int numCols, String[] dieFaces) {
        mNumRows = numRows;
        mNumCols = numCols;
        mDieFaces = dieFaces;
    }

    @Override
    public int getNumRows() {
        return mNumRows;
    }

    @Override
    public int getNumCols() {
        return mNumCols;
    }

    @Override
    public String getDieFace(int row, int col) {
        if (row < 0 || row >= mNumRows || col < 0 || col >= mNumCols) {
            return null;
        }

        final int index = row * mNumCols + col;
        if (index >= mDieFaces.length) {
            return "";
        }

        return mDieFaces[index];
    }
}
