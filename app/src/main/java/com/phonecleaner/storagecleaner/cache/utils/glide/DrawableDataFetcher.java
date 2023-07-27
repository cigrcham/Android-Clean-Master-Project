//package com.phonecleaner.storagecleaner.cache.utils.glide;
//
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.media.MediaMetadataRetriever;
//import android.net.Uri;
//
//import androidx.annotation.NonNull;
//
//import com.bumptech.glide.Priority;
//import com.bumptech.glide.load.DataSource;
//import com.bumptech.glide.load.data.DataFetcher;
//import com.phonecleaner.storagecleaner.cache.utils.Constants;
//import com.phonecleaner.storagecleaner.cache.R;
//
//import timber.log.Timber;
//
//public class DrawableDataFetcher implements DataFetcher<Bitmap> {
//
//    private final String mPath;
//    private final Context mContext;
//
//
//    public DrawableDataFetcher(Context context, String path) {
//        this.mPath = path;
//        mContext = context;
//    }
//
//    @Override
//    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
//        Timber.e(mPath.substring(6));
//        if (mPath.startsWith(Constants.AUDIO)) {
//            try {
//                Timber.e("Load Data");
//                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//                mmr.setDataSource(mPath.substring(Constants.AUDIO.length()));
//                final byte[] data;
//                data = mmr.getEmbeddedPicture();
//                if (data == null) {
//                    callback.onDataReady(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.song_default));
//                } else {
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                    if (bitmap != null) {
//                        callback.onDataReady(bitmap);
//                    } else {
//                        callback.onDataReady(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.song_default));
//                    }
//                }
//            } catch (Exception e) {
//                callback.onLoadFailed(e);
//                e.printStackTrace();
//            }
//        } else if (mPath.startsWith(Constants.VIDEO)) {
//            Timber.e("Load Video");
//            try {
//                MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
//                mMMR.setDataSource(mContext, Uri.parse(mPath.substring(Constants.VIDEO.length())));
//                if (mMMR.getFrameAtTime() != null) {
//                    callback.onDataReady(mMMR.getFrameAtTime());
//                }
//            } catch (Exception e) {
//                callback.onLoadFailed(e);
//                e.printStackTrace();
//            }
//        } else if (mPath.startsWith(Constants.APK)) {
//            try {
//                Drawable iconApp = mContext.getPackageManager().getApplicationIcon(mPath.substring(Constants.APK.length()));
//                callback.onDataReady(drawableToBitmap(iconApp));
//            } catch (PackageManager.NameNotFoundException e) {
//                callback.onLoadFailed(e);
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    public void cleanup() {
//        // Empty Implementation
//    }
//
//    @Override
//    public void cancel() {
//        // Empty Implementation
//    }
//
//    @NonNull
//    @Override
//    public Class<Bitmap> getDataClass() {
//        return Bitmap.class;
//    }
//
//    @NonNull
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.LOCAL;
//    }
//
//    public Bitmap drawableToBitmap(Drawable drawable) {
//        Bitmap bitmap = null;
//
//        if (drawable instanceof BitmapDrawable) {
//            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//            if (bitmapDrawable.getBitmap() != null) {
//                return bitmapDrawable.getBitmap();
//            }
//        }
//
//        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
//            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
//        } else {
//            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        }
//
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//        drawable.draw(canvas);
//        return bitmap;
//    }
//}
