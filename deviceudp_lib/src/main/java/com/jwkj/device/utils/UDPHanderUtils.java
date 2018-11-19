package com.jwkj.device.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jwkj.device.apmode.ResultCallback;
import com.p2p.core.global.P2PConstants;
import com.p2p.core.utils.AddContact;
import com.p2p.core.utils.MyUtils;
import com.p2p.shake.ShakeManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class UDPHanderUtils {
    private final static String UDPLOCK = "UDPLOCK";
    private static UDPHanderUtils UDPc = null;
    MulticastSocket datagramSocket = null;
    ResultCallback callback;
    byte[] data;
    int port;
    SendThread sendThread;
    boolean isSend = false;
    String contactId;
    boolean isReceive = false;
    int Count = 0;
    public final static int REPLAY_DEVICE_SUCCESS = 100;
    private WeakReference<Context> mActivityReference;
    private WifiManager.MulticastLock lock;
    private boolean isListn = false;
    private static Context context;

    private UDPHanderUtils(Context mContext) {
        mActivityReference = new WeakReference<>(mContext);
        WifiManager manager = (WifiManager) mActivityReference.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lock = manager.createMulticastLock(UDPLOCK);
    }

    public synchronized static UDPHanderUtils getInstance(Context mContext) {
        if (null == UDPc) {
            synchronized (UDPHanderUtils.class) {
                UDPc = new UDPHanderUtils(mContext.getApplicationContext());
            }
        }
        context = mContext;
        return UDPc;
    }

    public void setResultCallback(ResultCallback callback) {
        this.callback = callback;
    }

    public void send(byte[] messgae, int port, String contactId) throws Exception {
        this.data = messgae;
        this.port = port;
        this.contactId = contactId;
        openThread();
    }

    public void sendReceive(byte[] messgae, int port, String ip, String contactId) throws Exception {

    }


    public void startListner(final int port) {
        ShakeManager.getInstance().stopShaking();
        new Thread() {
            @Override
            public void run() {
                listner(port);
            }
        }.start();
    }

    private void listner(int port) {
        // UDP服务器监听的端口
        // 接收的字节大小，客户端发送的数据不能超过这个大小
        byte[] message = new byte[512];
        isListn = true;
        try {
            // 建立Socket连接
            datagramSocket = new MulticastSocket(port);
            //InetAddress group=InetAddress.getByName("255.255.255.255");//设备端的广播地址
            //datagramSocket.joinGroup(group);
            datagramSocket.setBroadcast(true);
            datagramSocket.setLoopbackMode(true);//不接受自己发的包
            DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
            while (isListn) {
                // 准备接收数据
                MulticastLock();
                datagramSocket.receive(datagramPacket);
                int cmd = MyUtils.bytesToInt(message, 0);
                int result = MyUtils.bytesToInt(message, 4);
                int deviceId = MyUtils.bytesToInt(message, 16);
                if (cmd == 17 && contactId.equals(String.valueOf(deviceId))) {
                    if (result == 0 || result == 255) {
                        if (null != callback ) {
                            if (255 == result) {
                                callback.onError(new Throwable("No supported by device"));
                            }
                            callback.onStateChange(String.valueOf(deviceId),result);
                        }
                    }
                    if (result == 0) {
                        isSend = false;
                        isReceive = true;
                    }
                }
                MulticastUnLock();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            MulticastUnLock();
            StopListen();
        }
    }

    private void MulticastLock() {
        if (this.lock != null) {
            try {
                this.lock.acquire();
            } catch (Exception e) {
                Log.e("SDK", "MulticastLock error");
            }
        }
    }

    private void MulticastUnLock() {
        if (this.lock != null) {
            try {
                this.lock.release();
            } catch (Exception e) {
                Log.e("SDK", "MulticastUnLock error");
            }
        }
    }

    public void StopListen() {
        isListn = false;
        if (null != datagramSocket) {
            datagramSocket.close();
            datagramSocket = null;
        }
    }

    class SendThread extends Thread {
        //是否重启过当前ap wifi
        private boolean isRestartWifi = false;
        @Override
        public void run() {
            DatagramSocket udpSocket = null;
            DatagramPacket dataPack = null;
            try {
                Count = 0;
                udpSocket = new DatagramSocket();
                InetAddress local = null;
                while (isSend) {
                    String ip = WifiUtils.getApDeviceIpAdress(context);
                    //Log.d("get_ap_wifi_ip", "ip = " + ip);
                    local = InetAddress.getByName(ip);
                    dataPack = new DatagramPacket(data, data.length, local, port);
                    try {
                        sleep(1000);
                        udpSocket.send(dataPack);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                byte[] receiveData = {52, 0, 0, 0};
                dataPack = new DatagramPacket(receiveData, receiveData.length, local, port);
                //Log.d("get_ap_wifi_ip", "start 发送确认设备收到设置WiFi的信息：" + isReceive +";count " + Count);
                while (isReceive && Count < 10) {
                    //Log.d("get_ap_wifi_ip", "发送确认设备收到设置WiFi的信息：" + isReceive +";count " + Count);
                    try {
                        udpSocket.send(dataPack);
                        sleep(300);
                        Log.d("get_ap_wifi_ip", "--------" );
                        Count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Log.d("get_ap_wifi_ip", "---Exception-----" +e.getMessage());
                    }
                }
                //Log.d("get_ap_wifi_ip", "end 发送确认设备收到设置WiFi的信息：" + isReceive +";count " + Count);
                if (isReceive) {
                    //Log.d("get_ap_wifi_ip", "发送成功返回");
                    if (null != callback) {
                        callback.onConfigPwdSuccess(contactId,REPLAY_DEVICE_SUCCESS);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (udpSocket != null) {
                    udpSocket.close();
                    udpSocket = null;
                }
            }

        }
    }

    public void openThread() {
        isSend = true;
        if (null == sendThread || !sendThread.isAlive()) {
            sendThread = new SendThread();
            sendThread.start();
        }
    }

    public void stopSend() {
        isSend = false;
        sendThread = null;
    }

}
