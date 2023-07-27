package com.phonecleaner.storagecleaner.cache.utils.customView.fileDup;

import android.util.Log;

/**
 * Created by Thinhvh on 21/11/2022.
 * Phone: 0398477967
 * Email: thinhvh.fpt@gmail.com
 */

public class SysoutOutputter implements Outputter {

    @Override
    public void output(final String message) {
        System.out.println(message);
        Log.d("Thinhvh", "output" + message);
    }

    @Override
    public void outputError(final String message) {
        System.err.println(message);
        Log.e("Thinhvh", "outputError " + message);
    }
}
