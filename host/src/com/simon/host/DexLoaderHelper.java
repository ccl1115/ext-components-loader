package com.simon.host;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

/**
 * DexLoaderHelper用于加载外部的dex文件。
 *
 * 因为外部有很多Activity之类的基于IOC（控制反转）的对象，它们是使用Context的ClassLoader来加载的。所以导致这些外部Activity即便使用我们
 * ClassLoader加载之后仍然不能直接被使用。这个类同时反射了系统中对应我们应用的Context的ClassLoader，把它替换成我们加载了外部Dex的ClassLoader.
 *
 * <b>注意：在低版本的Android系统中，DexClassLoader要求被加载的dex文件以class.dex命名，并且放入到一个jar/apk文件中，本质上它是一个zip文件。
 * 而高版本的DexClassLoader能够向下兼容，同时可以直接加载dex文件。</b>
 */
public class DexLoaderHelper {
    private static final String TAG = "DexLoaderHelper";

    private static final boolean DEBUG = true;

    private Context mContext;
    private DexClassLoader mClassLoader;
    private boolean mInit;


    public DexLoaderHelper(Context context) {
        mContext = context;
    }

    /**
     * 从Assets下面拷贝到/data/data/com.baidu.tieba/files/下面
     */
    private File copyFile(String source, String out) {
        File outBackFile = new File(mContext.getFilesDir() + "/" + out + "_back");
        File outFile = new File(mContext.getFilesDir() + "/" + out);

        final int buffer_size = 1024 * 64; // 64k
        final byte[] buffer = new byte[buffer_size];

        if (outFile.exists()) {
            //如果文件存在，先判断是否和源文件相同。
            try {
                byte[] outDigest = digestFile(outFile);
                byte[] sourceDigest = digestStream(mContext.getAssets().open(source, AssetManager.ACCESS_STREAMING));
                if (!Arrays.equals(outDigest, sourceDigest)) {
                    //如果不相同，先拷贝到以后缀_back的目标文件。然后删除目标文件，然后重命名back文件为目标文件
                    FileOutputStream fos = null;
                    InputStream is = null;
                    try {
                        int n;
                        fos = new FileOutputStream(outBackFile);
                        is = mContext.getAssets().open(source, AssetManager.ACCESS_STREAMING);
                        while ((n = is.read(buffer, 0, buffer_size)) != -1) {
                            fos.write(buffer, 0, n);
                        }
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "load bd browser failed: FileNotFoundException");
                    } catch (IOException e) {
                        Log.d(TAG, "load bd browser failed: IOException");
                    } finally {
                        if (fos != null) {
                            fos.close();
                        }
                        if (is != null) {
                            is.close();
                        }
                    }
                    if (outFile.delete()) {
                        if (outBackFile.renameTo(outFile)) {
                            Log.d(TAG, "load bd browser failed: renameFile");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 如果不存在目标文件，则直接拷贝
            FileOutputStream fos = null;
            InputStream is = null;
            try {
                int n;
                fos = new FileOutputStream(outFile);
                is = mContext.getAssets().open(source, AssetManager.ACCESS_STREAMING);
                while ((n = is.read(buffer, 0, buffer_size)) != -1) {
                    fos.write(buffer, 0, n);
                }
                is.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "load bd browser failed: FileNotFoundException");
            } catch (IOException e) {
                Log.e(TAG, "load bd browser failed: IOException");
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ignored) {}
            }
        }

        return outFile;
    }

    private byte[] digestFile(File f) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(f);
            final int buffer_size = 1024 * 128;
            final byte[] buffer = new byte[buffer_size];
            int n;
            while ((n = is.read(buffer, 0, buffer_size)) != -1) {
                digest.update(buffer, 0, n);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] digestStream(InputStream is) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            final int buffer_size = 1024 * 128;
            final byte[] buffer = new byte[buffer_size];
            int n;
            while ((n = is.read(buffer, 0, buffer_size)) != -1) {
                digest.update(buffer, 0, n);
            }
            is.close();
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     */
    private void loadExtDex() {
        File f = copyFile("ext.jar", "ext_optimized.jar");

        mClassLoader = new DexClassLoader(f.getAbsolutePath(), mContext.getFilesDir().getAbsolutePath(),
                null, mContext.getClassLoader());
    }

    private Field getField(Class<?> cls, String name) {
        for (Field field : cls.getDeclaredFields()) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    /**
     * 使用反射替换系统内部的BaseDexClassLoader
     * 使得可以加载外部的控件，activity，service等等。
     *
     * TODO Need to be test
     *
     * @param classLoader class loader
     */
    @SuppressWarnings("unchecked")
    private void hackClassLoader(Application application, ClassLoader classLoader) {
        try {
            Field mLoadedApk = getField(Application.class, "mLoadedApk");
            Object apk = mLoadedApk.get(application);
            Field mClassLoader = getField(apk.getClass(), "mClassLoader");

            mClassLoader.set(apk, classLoader);
        } catch (IllegalArgumentException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            Log.e(TAG, "load bd browser failed: IllegalArgumentException");
        } catch (IllegalAccessException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            Log.e(TAG, "load bd browser failed: IllegalAccessException");
        }
    }

    void init(Application application) {
        if (!mInit) {
            try {
                loadExtDex();
                hackClassLoader(application, mClassLoader);
            } catch (Throwable e) {
                Log.e(TAG, "load dex class failed: Exception: " + e.getMessage());
                return;
            }
            mInit = true;
        }
    }

}
