package com.phonecleaner.storagecleaner.cache.utils.glide;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.phonecleaner.storagecleaner.cache.utils.Constants;

//class DrawableModelLoader implements ModelLoader<String, Bitmap> {
//
//    private final Context mContext;
//
//    DrawableModelLoader(Context context) {
//        mContext = context;
//    }
//
//    @Nullable
//    @Override
//    public LoadData<Bitmap> buildLoadData(@NonNull String path, int width, int height, @NonNull Options options) {
//
//        return new LoadData<>(new ObjectKey(path),
//                new DrawableDataFetcher(mContext, path));
//    }
//
//    @Override
//    public boolean handles(@NonNull String model) {
//        return model.startsWith(Constants.AUDIO) || model.startsWith(Constants.VIDEO) || model.startsWith(Constants.APK);
//    }
//}