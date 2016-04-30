package com.draketb.ramble;

import java.util.Random;

class Die {
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