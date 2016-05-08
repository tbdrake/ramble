package com.draketb.ramble;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by draketb on 5/5/16.
 */
public class Solver {
    private final Board mBoard;
    private final WordList mWordList;
    private final List<String> mWordsFound = new ArrayList<>();

    public Solver(Board board, WordList wordList) {
        mBoard = board;
        mWordList = wordList;
    }

    public List<String> solve() {
        if (mWordsFound.isEmpty()) {
            for (int i = 0; i < mBoard.getNumRows(); ++i) {
                for (int j = 0; j < mBoard.getNumCols(); ++j) {
                    solveAt(i, j);
                }
            }
        }
        return new ArrayList<String>(mWordsFound);
    }

    private void solveAt(int row, int col) {

    }
}
