package com.exa.pcmutil.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 日志规则：
 * 循环日志尽量使用VERBOSE级别
 * 少量重要单次日志用INFO级别
 * 异常日志用ERROR级别
 * 打印判断用：
 * DEBUG = Log.isLoggable(TAG,Log.DEBUG);
 * VERBOSE = Log.isLoggable(TAG,Log.VERBOSE);
 * @author lsh
 */
public class L {
    public static String TAG = "MPcmUtil";
    /**
     * 使用下面adb命令使 DEBUG=true,设置后重启APP【设置级别等于/低于DEBUG的都生效】
     * adb shell setprop log.tag.CompanyDemo DEBUG
     * adb shell setprop log.tag.CompanyDemo D
     * adb shell setprop log.tag.CompanyDemo V
     * adb shell setprop log.tag.CarLocationService VERBOSE
     */
    private static boolean isLog = true;
    private static boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG) | isLog;
    private static boolean VERBOSE = Log.isLoggable(TAG, Log.VERBOSE) | isLog;
    public static String msg = null;
    private static final String TAG_DIVIDER = ">>";
    // 空格占位符 xml中使用 &#32; 占位半角空格宽度
    private static final String SPACE = "\u3000";

    public static void init(String tag, boolean isLog) {
        L.TAG = tag;
        L.isLog = isLog;
    }

    public static void d(String msg) {
        L.msg = msg;
        if (DEBUG) {
            Log.d(TAG, "" + msg);
        }
    }

    public static void df(String args, Object... obj) {
        L.msg = String.format(args, obj);
        if (DEBUG) {
            Log.d(TAG, String.format(args, obj));
        }
    }

    public static void e(String msg) {
        L.msg = msg;
        if (isLog) {
            Log.e(TAG, "" + msg);
        }
    }

    public static void w(String msg) {
        L.msg = msg;
        if (isLog) {
            Log.w(TAG, "" + msg);
        }
    }

    public static void v(String msg) {
        L.msg = msg;
        if (VERBOSE) {
            Log.v(TAG, "" + msg);
        }
    }

    public static void i(String msg) {
        L.msg = msg;
        if (isLog) {
            Log.i(TAG, "" + msg);
        }
    }

    public static void i(String TAG, String msg) {
        L.msg = msg;
        if (isLog) {
            Log.i(L.TAG + TAG_DIVIDER + TAG, "" + msg);
        }
    }

    public static void w(String TAG, String msg) {
        L.msg = msg;
        if (isLog) {
            Log.w(L.TAG + TAG_DIVIDER + TAG, "" + msg);
        }
    }


    public static void e(String TAG, String msg) {
        L.msg = msg;
        if (isLog) {
            Log.e(L.TAG + TAG_DIVIDER + TAG, "" + msg);
        }
    }

    public static void v(String TAG, String msg) {
        L.msg = msg;
        if (VERBOSE) {
            Log.v(L.TAG + TAG_DIVIDER + TAG, "" + msg);
        }
    }

    public static void d(String TAG, String msg) {
        L.msg = msg;
        if (DEBUG) {
            Log.e(L.TAG + TAG_DIVIDER + TAG, "" + msg);
        }
    }

    public static void e(String msg, Throwable throwable) {
        L.msg = msg;
        if (isLog) {
            Log.e(TAG, msg + "," + getThrowableLineNum(throwable));
        }
    }

    public static void e(String TAG, String msg, Throwable throwable) {
        L.msg = msg;
        if (isLog) {
            Log.e(L.TAG + TAG_DIVIDER + TAG, msg + "," + getThrowableLineNum(throwable));
        }
    }

    //获取异常行
    private static String getThrowableLineNum(Throwable throwable) {
        try {
            StackTraceElement[] trace = throwable.getStackTrace();
            // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
            StackTraceElement tmp = trace[0];
            return tmp.getClassName() + "." + tmp.getMethodName()
                    + "(" + tmp.getFileName() + ":" + tmp.getLineNumber() + ")";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取当前方法名
     */
    public static String getCurrentMethodName() {
        int level = 1;
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[level].getMethodName();
    }

    public static String getCurrentClassName() {
        int level = 1;
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[level].getClassName();
    }

    /**
     * 获取调用方法名
     */
    public static void dd() {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        String methodName = s[3].getMethodName();
        d(methodName);
    }

    /**
     * 获取调用方法名
     */
    public static void dd(String msg) {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        String methodName = s[3].getMethodName();
        d(methodName + SPACE + msg);
    }

    /**
     * 获取调用方法名
     */
    public static void dw(String msg) {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        String methodName = s[3].getMethodName();
        w(methodName + SPACE + msg);
    }

    /**
     * 获取调用方法名
     */
    public static void de(String msg) {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        String methodName = s[3].getMethodName();
        e(methodName + SPACE + msg);
    }

    /**
     * 获取调用方法名
     */
    public static void de(Throwable e) {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        String methodName = s[3].getMethodName();
        e(methodName + "," + getThrowableLineNum(e));
    }

    /**
     * 获取调用方法名
     */
    public static void dd(int msg) {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        String methodName = s[3].getMethodName();
        d(methodName + SPACE + msg);
    }

    /**
     * 获取调用方法名
     */
    public static void dd(boolean state) {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        String methodName = s[3].getMethodName();
        d(methodName + SPACE + state);
    }

    /**
     * 打印json字符串
     *
     * @param jsonContent
     */
    public static void json(String jsonContent) {
        final String separator = System.getProperty("line.separator");
        String message = jsonContent;
        try {
            if (jsonContent.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(jsonContent);
                message = jsonObject.toString(4);//最重要的方法，就一行，返回格式化的json字符串，其中的数字4是缩进字符数
            } else if (jsonContent.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(jsonContent);
                message = jsonArray.toString(4);
            } else {
                message = jsonContent;
            }
        } catch (JSONException e) {
            e("log json err", e);
        }
        String[] lines = message.split(separator);
        for (String line : lines) {
            e(line);
        }
    }
}
