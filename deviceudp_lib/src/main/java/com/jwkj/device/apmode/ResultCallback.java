package com.jwkj.device.apmode;

/**
 * AP配网结果回调
 * Created by HDL on 2017/12/28.
 *
 * @author HDL
 */

public interface ResultCallback {
    /**
     * 配网开始的时候回调
     */
    void onStart();

    /**
     * AP设备已经设置配网成功，此时需要连接设备连上的wifi，及时接收设备的回复。
     */
    void onConfigPwdSuccess(String deviceId, int stateCode);

    /** 配网过程进度状态的改变监听
     * @param deviceId 设备id
     * @param stateCode 配网进度状态码
     */
    void onStateChange(String deviceId, int stateCode);
    /**
     * 当错误的时候回调
     *
     * @param throwable 错误信息
     */
    void onError(Throwable throwable);
}
