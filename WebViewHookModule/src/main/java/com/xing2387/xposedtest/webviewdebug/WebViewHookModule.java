package com.xing2387.xposedtest.webviewdebug;

import android.webkit.WebView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * webView debug
 */
class WebViewHookModule implements IXposedHookLoadPackage {
    // handleLoadPackage 会在android加载每一个apk后执行
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        // 可以从lpparam中获取当前apk的名字
        String packageName = lpparam.packageName;
        /*if (!packageName.equals("com.tencent.mobileqq")) {
            return;
        }*/
        XposedBridge.log("WebViewHook handleLoadPackage: " + packageName);
        // 勾住 WebView 所有的构造器
        XposedBridge.hookAllConstructors(WebView.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                // 打开webContentsDebuggingEnabled
                XposedHelpers.callStaticMethod(WebView.class, "setWebContentsDebuggingEnabled", true);
                XposedBridge.log("WebViewHook new WebView(): " + packageName);
            }
        });
        XposedBridge.hookAllConstructors(com.tencent.smtt.sdk.WebView.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                // 打开webContentsDebuggingEnabled
                XposedHelpers.callStaticMethod(com.tencent.smtt.sdk.WebView.class, "setWebContentsDebuggingEnabled", true);
                XposedBridge.log("WebViewHook new WebView(): " + packageName);
            }
        });
    }
}
