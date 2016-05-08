package com.draketb.ramble;

/**
 * Created by draketb on 5/4/16.
 */
public interface Board {
    int getNumRows();
    int getNumCols();
    String getDieFace(int row, int col);
}
