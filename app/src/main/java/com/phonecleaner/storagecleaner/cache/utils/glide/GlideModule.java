//package com.phonecleaner.storagecleaner.cache.utils.glide;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//
//import androidx.annotation.NonNull;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.Registry;
//import com.bumptech.glide.module.AppGlideModule;
//
//@com.bumptech.glide.annotation.GlideModule
//public class GlideModule extends AppGlideModule {
//
//    @Override
//    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
//        registry.prepend(String.class, Bitmap.class, new DrawableModelLoaderFactory(context));
//    }
//}
