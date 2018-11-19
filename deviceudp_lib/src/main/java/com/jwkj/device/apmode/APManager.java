package com.jwkj.device.apmode;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.jwkj.device.entity.APDeviceConfig;
import com.jwkj.device.entity.SSIDType;
import com.jwkj.device.utils.MUtils;
import com.jwkj.device.utils.UDPHanderUtils;
import com.jwkj.device.utils.WifiUtils;


/**
 * AP模式的管理器
 */
public class APManager {
    private String ssid = "";
    private String pwd;
    private static APManager manager;
    private int port = 8899;
    private ResultCallback callback;
    private boolean isCanReceive = true;//是否可以接收
    private Context mContext;
    private APDeviceConfig apDeviceConfig;
    private APManager() {
    }

    public static APManager getInstance() {
        if (manager == null) {
            manager = new APManager();
        }
        return manager;
    }

    public APManager with(Context context) {
        this.mContext = context.getApplicationContext();
        checkContextIsNull();
        return this;
    }

    /**
     * 检测上下文对象是否为空
     */
    private void checkContextIsNull() {
        if (mContext == null) {
            throw new NullPointerException("context is null,please call SoundWaveSender.getInstance().with(context).***");
        }
    }


    public APManager setApDeviceConfig(APDeviceConfig apDeviceConfig) {
        if (TextUtils.isEmpty(apDeviceConfig.getDeviceID())) {
            try {
                apDeviceConfig.setDeviceID(MUtils.getAPDeviceId(apDeviceConfig.getWifiSSID()));
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onError(new Throwable("A device that is not a AP mode"));
                }
            }
        }
        if (apDeviceConfig.getType() == SSIDType.NONE) {
            checkContextIsNull();
            apDeviceConfig.setType(MUtils.getSSIDType(mContext));
        }
        this.apDeviceConfig = apDeviceConfig;
        return this;
    }

    /**
     * 设置wifi
     *
     * @param ssid wifi名字
     * @param pwd  密码
     * @return
     */
    public APManager setWifi(String ssid, String pwd) {
        if (ssid.startsWith("GW_AP_")) {
            if (callback != null) {
                callback.onError(new Throwable("A device that is not a AP mode"));
                stopReceive();
                stopSend();
            }
        }
        this.ssid = ssid;
        this.pwd = pwd;
        return this;
    }


    private ResultCallback resultCallback = new ResultCallback() {
        @Override
        public void onStart() {
            if (null != callback) {
                callback.onStart();
            }
        }

        @Override
        public void onConfigPwdSuccess(String deviceId, int stateCode) {
            stopReceive();
            if (null != callback) {
                callback.onConfigPwdSuccess(deviceId,stateCode);
            }
        }

        @Override
        public void onStateChange(String deviceId, int stateCode) {
            if (null != callback) {
                callback.onStateChange(deviceId,stateCode);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            stopReceive();
            if (null != callback) {
                callback.onError(throwable);
            }

        }
    };

    public void send(ResultCallback callback) {
        this.callback = callback;
        if (isCanReceive) {
            isCanReceive = false;
            startReceive();
        }else{
            if (null != callback) {
                callback.onError(new Throwable("The last time the delivery was not completed"));
            }
        }
    }

    /**
     * 开始接收数据，单次任务只能调用一次
     */
    private void startReceive() {
        if (null != resultCallback) {
            resultCallback.onStart();
        }
        if (apDeviceConfig == null) {
            System.err.println("\n\n\n没有设置ApDeviceConfig对象\n\n\n");
            callback.onError(new Throwable("No call APManager.getInstance().with(mContext).setApDeviceConfig(***)"));
            return;
        }
        if (!apDeviceConfig.getApWifiSSID().startsWith("GW_AP_")) {
            if (callback != null) {
                System.err.println("\n\n\n " + apDeviceConfig.getApWifiSSID() + " 不是AP模式的设备,请设置 GW_AP_ 开头的WIFI\n\n\n");
                callback.onError(new Throwable("A device that is not a AP mode"));
                return;
            }
        }
        if (!WifiUtils.getInstance().isConnectWifi(apDeviceConfig.getApWifiSSID())) {
            if (callback != null) {
                System.err.println("\n\n\n "  + " 配网之前需要将手机连接到需要配网设备发出的热点WiFi\n\n\n");
                callback.onError(new Throwable("配网之前需要将手机连接到需要配网设备发出的热点WiFi"));
                return;
            }
        }
        try {
            UDPHanderUtils.getInstance(mContext).setResultCallback(resultCallback);
            UDPHanderUtils.getInstance(mContext).startListner(8899);
            UDPHanderUtils.getInstance(mContext).send(apDeviceConfig.getSendData(), 8899, apDeviceConfig.getDeviceID());
        } catch (Exception e) {
            resultCallback.onError(e);
            e.printStackTrace();
        }
    }

    /**
     * 停止接收数据
     */
    private void stopReceive() {
        isCanReceive = true;//停止任务了要复位
        UDPHanderUtils.getInstance(mContext).stopSend();
        UDPHanderUtils.getInstance(mContext).StopListen();
    }


    /**
     * 停止发送局域网信息
     *
     * @return
     */
    public APManager stopSend() {
        stopReceive();
        return this;
    }
}
