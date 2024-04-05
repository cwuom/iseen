package com.cwuom.iseen.Util;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
/**
 * 全局崩溃拦截
 * ----------------------
 * @author mjSoftKing
 * ----------------------
 * */

public class NeverCrash {

    private final static String TAG = NeverCrash.class.getSimpleName();
    private final static NeverCrash INSTANCE = new NeverCrash();

    private boolean debugMode;
    private MainCrashHandler mainCrashHandler;
    private UncaughtCrashHandler uncaughtCrashHandler;

    private NeverCrash() {
    }

    public static NeverCrash getInstance() {
        return INSTANCE;
    }

    private synchronized MainCrashHandler getMainCrashHandler() {
        if (null == mainCrashHandler) {
            mainCrashHandler = (t, e) -> {
            };
        }
        return mainCrashHandler;
    }

    public NeverCrash setMainCrashHandler(MainCrashHandler mainCrashHandler) {
        this.mainCrashHandler = mainCrashHandler;
        return this;
    }

    private synchronized UncaughtCrashHandler getUncaughtCrashHandler() {
        if (null == uncaughtCrashHandler) {
            uncaughtCrashHandler = (t, e) -> {
            };
        }
        return uncaughtCrashHandler;
    }

    public NeverCrash setUncaughtCrashHandler(UncaughtCrashHandler uncaughtCrashHandler) {
        this.uncaughtCrashHandler = uncaughtCrashHandler;
        return this;
    }

    private boolean isDebugMode() {
        return debugMode;
    }

    public NeverCrash setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    public void register(Application application) {
        //主线程异常拦截
        new Handler(Looper.getMainLooper()).post(() -> {
            while (true) {
                try {
                    Looper.loop();
                } catch (Throwable e) {
                    if (isDebugMode()) {
                        Log.e(TAG, "未捕获的主线程异常行为", e);
                    }
                    getMainCrashHandler().mainException(Looper.getMainLooper().getThread(), e);
                }
            }
        });

        //子线程异常拦截
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (isDebugMode()) {
                Log.e(TAG, "未捕获的子线程异常行为", e);
            }
            getUncaughtCrashHandler().uncaughtException(t, e);
        });
    }

    public interface MainCrashHandler {
        void mainException(Thread t, Throwable e);
    }

    public interface UncaughtCrashHandler {
        void uncaughtException(Thread t, Throwable e);
    }
}
