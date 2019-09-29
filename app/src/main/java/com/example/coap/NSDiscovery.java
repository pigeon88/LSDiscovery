package com.example.coap;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class NSDiscovery extends NetworkDiscovery {

    static final String TAG = "NSDiscovery";
    private static final String NSD_SERVER_TYPE = "_http._tcp.";
    private NsdManager mNsdManager;


    //注册服务器端
    public void registerService(Context context, String serviceName, int port) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(Utils.getIpAddress());
        serviceInfo.setPort(port);
        serviceInfo.setServiceType(NSD_SERVER_TYPE);//客户端发现服务器是需要对应的这个Type字符串
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

        discoveryNSDServer();
    }

    public void stopNSDServer() {
        mNsdManager.stopServiceDiscovery(mNSDDiscoveryListener);
        mNsdManager.unregisterService(mRegistrationListener);

    }

    private NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {
        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, "RegistrationListener -> NsdServiceInfo onRegistrationFailed");

        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, "RegistrationListener -> onUnregistrationFailed serviceInfo: " + serviceInfo + " ,errorCode:" + errorCode);

        }

        @Override
        public void onServiceRegistered(NsdServiceInfo serviceInfo) {
            Log.i(TAG, "RegistrationListener -> onServiceRegistered: " + serviceInfo);

        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            Log.i(TAG, "RegistrationListener -> onServiceUnregistered serviceInfo: " + serviceInfo);

        }
    };


    public void discoveryNSDServer() {
        //三个参数
        //第一个参数要和NSD服务器端定的ServerType一样，
        //第二个参数是固定的
        //第三个参数是扫描监听器
        mNsdManager.discoverServices(NSD_SERVER_TYPE, NsdManager.PROTOCOL_DNS_SD, mNSDDiscoveryListener);
    }

    //对得到的NDSServiceInfo进行解析
    public void resoleServer(NsdServiceInfo serviceInfo) {

        //第一个参数是扫描得到的对象，第二个参数是解析监听对象
        mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "ResolveListener -> onResolveFailed--> " + serviceInfo);

            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {//到这里就是我们要的最终数据信息
                Log.i(TAG, "ResolveListener -> onServiceResolved: " + serviceInfo);

            }
        });
    }

    NsdManager.DiscoveryListener mNSDDiscoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "DiscoveryListener -> onStartDiscoveryFailed--> " + serviceType + ":" + errorCode);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "DiscoveryListener -> onStopDiscoveryFailed--> " + serviceType + ":" + errorCode);
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            Log.i(TAG, "DiscoveryListener -> onDiscoveryStarted--> " + serviceType);
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.i(TAG, "DiscoveryListener -> onDiscoveryStopped--> " + serviceType);
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {//关键的回调方法
            if (Utils.isIPAddress(serviceInfo.getServiceName()) && !Utils.getIpAddress().equals(serviceInfo.getServiceName())) {
                //这里的 serviceInfo里面只有NSD服务器的主机名，要解析后才能得到该主机名的其他数据信息
                Log.i(TAG, "DiscoveryListener -> onServiceFound Info: --> " + serviceInfo);
                //开始解析数据
                //resoleServer(serviceInfo);

                neighborRecords.put(1, new NeighborRecord(0, 0, 40, 0));
                notifyNeighborChanged(neighborRecords.values());
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            if (serviceInfo.getServiceName().equals("NSD-")) {
                Log.e(TAG, "DiscoveryListener -> onServiceLost--> " + serviceInfo);
            }
        }
    };

    NsdManager.ResolveListener mNSDResolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, "ResolveListener -> onResolveFailed--> " + serviceInfo);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {//到这里就是我们要的最终数据信息
            Log.i(TAG, "ResolveListener -> onServiceResolved: " + serviceInfo);

        }
    };

}
