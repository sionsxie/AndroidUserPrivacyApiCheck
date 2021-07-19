package com.xing2387.xposedtest;

import android.content.ContentResolver;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.log;

public class MyModule implements IXposedHookLoadPackage {
    private static final String TAG_GL = "MyModule-GL";

    private static final String TAG_GS = "MyModule-GS";

    private HashMap<String, HashMap<String, ArrayList<String>>> hookList = new HashMap<String, HashMap<String, ArrayList<String>>>() {{
        put("android.telephony.TelephonyManager", new HashMap<String, ArrayList<String>>() {{
            //IMEI
            put("getImei", new ArrayList<String>() {{
                add("int");
            }});
            put("getDeviceId", new ArrayList<>());

//            //基站定位
//            put("getCellLocation", new ArrayList<>());
//            put("getAllCellInfo", new ArrayList<>());
//
//            //sim卡信息
//            put("getSubscriberId", new ArrayList<String>() {{
//                add("int");
//            }});
//            put("getSimSerialNumber", new ArrayList<String>() {{
//                add("int");
//            }});
//            put("getNetworkOperatorForPhone", new ArrayList<String>() {{
//                add("int");
//            }});
//            put("getSimCountryIsoForPhone", new ArrayList<String>() {{
//                add("int");
//            }});
        }});
        put("android.net.wifi.WifiInfo", new HashMap<String, ArrayList<String>>() {{
            //MAC地址
            put("getMacAddress", new ArrayList<>());
            //获取IP地址
            put("getIpAddress", new ArrayList<>());
//            //WiFi
//            put("getSSID", new ArrayList<>());
//            put("getBSSID", new ArrayList<>());
        }});
        put(NetworkInterface.class.getName(), new HashMap<String, ArrayList<String>>() {{
            //MAC地址
            put("getHardwareAddress", new ArrayList<>());
        }});
//        put("android.net.wifi.WifiManager", new HashMap<String, ArrayList<String>>() {{
//            //WiFi
//            put("getScanResults", new ArrayList<>());
//        }});

        put("android.provider.Settings.Secure", new HashMap<String, ArrayList<String>>() {{
            //android_id
            put("getString", new ArrayList<String>() {{
                //Settings.Secure.ANDROID_ID
//            public static String getString(ContentResolver resolver, String name) {
                add(ContentResolver.class.getName());
                add(String.class.getName());
            }});
            //蓝牙信息
//            put("getString", new ArrayList<String>() {{
        }});

        put("android.app.ApplicationPackageManager", new HashMap<String, ArrayList<String>>() {{
            //安装列表
            put("getInstalledApplications", new ArrayList<String>() {{
                add("int");
            }});
            put("getInstalledPackages", new ArrayList<String>() {{
                add("int");
            }});
            put("getInstalledModules", new ArrayList<String>() {{
                add("int");
            }});
        }});

//        put("android.hardware.SensorManager", new HashMap<String, ArrayList<Object>>() {{
//            //传感器
//            put("registerListener", new ArrayList<Object>() {{
//                add(SensorEventListener.class);
//                add(Sensor.class);
//                add("int");
//                add(Handler.class);
//            }});
//        }});

//        //GPS
//        put("android.location.LocationManager", new HashMap<String, ArrayList<Object>>() {{
//        }});

//        put("android.accounts.AccountManager", new HashMap<String, ArrayList<String>>() {{
//            //账户
//            put("getAccounts", new ArrayList<>());
//            put("getAccountsByType", new ArrayList<String>() {{
//                add(String.class.getName());
//            }});
//        }});
//        put("android.bluetooth.le.BluetoothLeScanner", new HashMap<String, ArrayList<String>>() {{
//            //蓝牙信息
//            put("startScan", new ArrayList<String>() {{
//                add(List.class.getName());
//                add(ScanSettings.class.getName());
//                add(WorkSource.class.getName());
//                add(ScanCallback.class.getName());
//                add(PendingIntent.class.getName());
//                add(List.class.getName());
//            }});
//        }});

//        put("android.app.ActivityManager", new HashMap<String, ArrayList<String>>() {{
//            //安装列表
//            put("getRunningAppProcesses", new ArrayList<>());
//        }});
    }};
    //    com.tencent.smtt.sdk.TbsDownloader$1.handleMessage

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpp) {
        hookGL(lpp);
        hookGS(lpp);
//        hookGetImei(lpp);
    }

    private void hookGS(XC_LoadPackage.LoadPackageParam lpp) {
        hookWithPackageName(lpp, "com.netease.gameforums", TAG_GS);
    }

    private void hookGL(XC_LoadPackage.LoadPackageParam lpp) {
        hookWithPackageName(lpp, "com.netease.gl", TAG_GL);
    }


    private void hookWithPackageName(XC_LoadPackage.LoadPackageParam lpp, String packageName, String logTag) {
        if (TextUtils.isEmpty(packageName) || !packageName.equals(lpp.packageName)) {
            return;
        }

        Log.d(logTag, "555 handleLoadPackage: packageName = " + lpp.packageName + ", processName = " + lpp.processName);
        Log.d(logTag, "package classloader: " + lpp.classLoader.toString());

//        HashMap<String, HashMap<String, ArrayList<Object>>>
        for (Map.Entry<String, HashMap<String, ArrayList<String>>> classEntry : hookList.entrySet()) {
            String className = classEntry.getKey();
            for (Map.Entry<String, ArrayList<String>> methodEntry : classEntry.getValue().entrySet()) {
                String methodName = methodEntry.getKey();
                ArrayList<String> params = methodEntry.getValue();
                doHook(logTag, lpp, className, methodName, params);
            }
        }
    }

    private void doHook(String logTag, XC_LoadPackage.LoadPackageParam lpp, String className, String methodName, ArrayList<String> params) {
        try {
            String[] p = params.toArray(new String[params.size()]);
            Class clazz = XposedHelpers.findClass(className, lpp.classLoader);
            Method method = XposedHelpers.findMethodExactIfExists(clazz, methodName, p);
            if (method == null) {
                Log.e(logTag, "doHook: 方法没找到： " + className + "#" + methodName + " ");
            } else {
                Log.d(logTag, "doHook: 找到方法： " + className + "#" + methodName + " ");
                XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if ("android.provider.Settings.Secure".equals(className) &&
                                "getString".equals(methodName)) {
                            Object value = param.args[param.args.length - 1];
                            if (value instanceof String) {
                                if (Settings.Secure.ANDROID_ID.equals(value)) {
                                    try {
                                        throw new RuntimeException("打印" + className + "#" + methodName + "调用栈 - " + value);
                                    } catch (Throwable e) {
//                                        log(e);
                                        Log.d(logTag, "\n\n\n-----------------------------------------------------------");
                                        Log.e(logTag, "beforeHookedMethod: ", e);
                                        Log.d(logTag, "-----------------------------------------------------------\n\n\n");

                                    }
                                } else {
                                    String logMsg = "调用了 android.provider.Settings.Secure#getString() - " + value;
//                                    log(logMsg);
                                    Log.d(logTag, "\n\n\n-----------------------------------------------------------");
                                    Log.e(logTag, logMsg);
                                    Log.d(logTag, "-----------------------------------------------------------\n\n\n");
                                }
                            }
                            return;
                        }
                        try {
                            throw new RuntimeException("打印" + className + "#" + methodName + "调用栈");
                        } catch (Throwable e) {
//                            log(e);
                            Log.d(logTag, "\n\n\n-----------------------------------------------------------");
                            Log.e(logTag, "beforeHookedMethod: ", e);
                            Log.d(logTag, "-----------------------------------------------------------\n\n\n");
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(logTag, "doHook: 方法没找到 " + methodName + " ", e);
        }
    }

    private void hookGetImei(XC_LoadPackage.LoadPackageParam lpp) {
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", lpp.classLoader,
                "getImei", "int", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            throw new RuntimeException("打印getImei调用栈");
                        } catch (Throwable e) {
                            log(e);
                            Log.e("MyModule", "beforeHookedMethod: ", e);
                        }
                    }
                });
    }

}

