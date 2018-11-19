package com.jwkj.device.entity;

import com.p2p.core.P2PHandler;
import com.p2p.core.utils.MyUtils;

import java.util.Random;

/**
 * AP模式设备的配置项
 * Created by HDL on 2017/12/28.
 *
 * @author HDL
 */

public class APDeviceConfig {
    /**
     * 设备id
     */
    private String deviceID = "";
    /**
     * wifi名字
     */
    private String wifiSSID = "";

    /**
     * 设备ap配网时发出的热点WiFi的名字
     */
    private String apWifiSSID = "";
    /**
     * wifi密码
     */
    private String wifiPwd = "";
    /**
     * 加密方式
     */
    private SSIDType type = SSIDType.PSK;
    /**
     * 设备密码
     */
    private String devicePwd = "";

    public APDeviceConfig() {
    }

    public APDeviceConfig(String deviceID, String wifiSSID, String wifiPwd, SSIDType type, String devicePwd) {
        this.deviceID = deviceID;
        this.wifiSSID = wifiSSID;
        this.wifiPwd = wifiPwd;
        this.type = type;
        this.devicePwd = devicePwd;
    }

    public APDeviceConfig(String wifiSSID, String wifiPwd, String devicePwd) {
        this.wifiSSID = wifiSSID;
        this.wifiPwd = wifiPwd;
        this.devicePwd = devicePwd;
    }

    public APDeviceConfig(String wifiSSID, String wifiPwd, String appWifiSSID, String devicePwd) {
        this.wifiSSID = wifiSSID;
        this.wifiPwd = wifiPwd;
        this.apWifiSSID = appWifiSSID;
        this.devicePwd = devicePwd;
    }

    /**
     * 获取发送数据的byte数组
     *
     * @return
     */
    public byte[] getSendData() {
        return MyUtils.getApSendWifi(deviceID, wifiSSID, wifiPwd, type.getValue(), devicePwd);
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getWifiSSID() {
        return wifiSSID;
    }

    public void setWifiSSID(String wifiSSID) {
        this.wifiSSID = wifiSSID;
    }

    public String getWifiPwd() {
        return wifiPwd;
    }

    public void setWifiPwd(String wifiPwd) {
        this.wifiPwd = wifiPwd;
    }

    public SSIDType getType() {
        return type;
    }

    public void setType(SSIDType type) {
        this.type = type;
    }

    public String getDevicePwd() {
        return devicePwd;
    }

    public void setDevicePwd(String devicePwd) {
        this.devicePwd = devicePwd;
    }

    public String getApWifiSSID() {
        return apWifiSSID;
    }

    public void setApWifiSSID(String apWifiSSID) {
        this.apWifiSSID = apWifiSSID;
    }

    @Override
    public String toString() {
        return "APDeviceConfig{" +
                "deviceID='" + deviceID + '\'' +
                ", wifiSSID='" + wifiSSID + '\'' +
                ", wifiPwd='" + wifiPwd + '\'' +
                ", type='" + type + '\'' +
                ", devicePwd='" + devicePwd + '\'' +
                '}';
    }

    /**
     * 生成随机密码
     *
     * @param pwdType 生成随机密码类型(0是主人非0是访客)
     * @return 生成的随机字母密码与随机数字密码 String[0]为随机字母密码，String[1]为随机数字密码
     */
    public static String[] createRandomPassword(int pwdType) {
        String prePwd = pwdType == 0 ? "master" : "visitor";
        String time = String.valueOf(System.currentTimeMillis());
        String proPwd = getRandomString(8);
        String UserPwd = prePwd + time + proPwd;
        String contactPwd = P2PHandler.getInstance().EntryPassword(UserPwd);
        return new String[]{UserPwd, contactPwd};
    }

    /**
     * 产生随机字符串
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
