package com.draketb.ramble;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeSet;

/**
 * Created by draketb on 4/29/16.
 */
public class SynchronizedTreeSetWordList implements WordList {
    private static final String TAG = SynchronizedTreeSetWordList.class.getSimpleName();
    private TreeSet<String> _words = new TreeSet<>();

    public synchronized void load(AssetManager assets) {
        _words.clear();
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open("enable1.txt")));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                _words.add(line.toLowerCase());
            }
            reader.close();
        } catch (IOException e) {
            Log.w(TAG, "Failed to load enable1.txt", e);
        }
    }

    public synchronized boolean containsWord(String word) {
        return _words.contains(word);
    }

    public synchronized boolean containsWords(String prefix) {
        return !_words.subSet(prefix, prefix + '\uFFFF').isEmpty();
    }
}
