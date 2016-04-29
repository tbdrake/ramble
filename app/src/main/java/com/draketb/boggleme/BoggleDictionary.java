package com.draketb.boggleme;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * Created by draketb on 4/29/16.
 */
public class BoggleDictionary {
    private static final String TAG = BoggleDictionary.class.getSimpleName();
    private HashSet<String> _words = new HashSet<>();

    public synchronized void load(AssetManager assets) {
        _words.clear();
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open("enable1.txt")));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                _words.add(line);
            }
            reader.close();
        } catch (IOException e) {
            Log.w(TAG, "Failed to load enable1.txt", e);
        }
    }

    public synchronized boolean containsWord(String s) {
        return _words.contains(s);
    }
}
