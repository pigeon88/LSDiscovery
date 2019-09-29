package com.example.coap;

import android.text.TextUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Utils {

    public static final String DEFAULT_NET_INTERFACE_ETH0 = "eth0";
    public static final String DEFAULT_NET_INTERFACE_WLAN0 = "wlan0";

    public static String getIpAddress() {
        NetworkInterface networkInterface = getNetworkInterface();
        if (networkInterface != null) {
            Enumeration<InetAddress> enInetAddress = networkInterface.getInetAddresses();   //getInetAddresses 方法返回绑定到该网卡的所有的 IP 地址。
            while (enInetAddress.hasMoreElements()) {
                InetAddress inetAddress = enInetAddress.nextElement();
                if (inetAddress instanceof Inet4Address) {
                    return inetAddress.getHostAddress();
                }
            }
        }

        return "UNKNOWN";
    }

    public static int getIpAddressHashCode() {
        NetworkInterface networkInterface = getNetworkInterface();
        if (networkInterface != null) {
            Enumeration<InetAddress> enInetAddress = networkInterface.getInetAddresses();   //getInetAddresses 方法返回绑定到该网卡的所有的 IP 地址。
            while (enInetAddress.hasMoreElements()) {
                InetAddress inetAddress = enInetAddress.nextElement();
                if (inetAddress instanceof Inet4Address) {
                    return inetAddress.hashCode();
                }
            }
        }

        return -1;
    }

    public static int getLocalAddress() {
        NetworkInterface networkInterface = getNetworkInterface();
        if (networkInterface != null) {
            Enumeration<InetAddress> enInetAddress = networkInterface.getInetAddresses();   //getInetAddresses 方法返回绑定到该网卡的所有的 IP 地址。
            while (enInetAddress.hasMoreElements()) {
                InetAddress inetAddress = enInetAddress.nextElement();
                if (inetAddress instanceof Inet4Address) {
                    return Utils.intToByteArray(inetAddress.getAddress());
                }
            }
        }

        return -1;
    }

    public static short getIpAddressMask() {
        NetworkInterface networkInterface = getNetworkInterface();
        if (networkInterface != null) {
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {    //
                if (interfaceAddress.getAddress() instanceof Inet4Address) {    //仅仅处理ipv4
                    //return calcMaskByPrefixLength(interfaceAddress.getNetworkPrefixLength());   //获取掩码位数，通过 calcMaskByPrefixLength 转换为字符串
                    return interfaceAddress.getNetworkPrefixLength();
                }
            }
        }

        return -1;
    }

    public static String getMacAddress() {
        NetworkInterface networkInterface = getNetworkInterface();
        if (networkInterface != null) {
            try {
                byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if (hardwareAddress != null) {
                    StringBuilder res1 = new StringBuilder();
                    for (Byte b : hardwareAddress) {
                        res1.append(String.format("%02X:", b));
                    }
                    if (!TextUtils.isEmpty(res1)) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "UNKNOWN";
    }

    /**
     * 获取当前可用的网络接口
     *
     * @return
     * @throws SocketException
     */
    public static NetworkInterface getNetworkInterface() {
        try {
            Enumeration<NetworkInterface> enNetworkInterface = NetworkInterface.getNetworkInterfaces(); //获取本机所有的网络接口
            while (enNetworkInterface.hasMoreElements()) {  //判断 Enumeration 对象中是否还有数据
                NetworkInterface networkInterface = enNetworkInterface.nextElement();   //获取 Enumeration 对象中的下一个数据
                // 判断网口是否在使用及网口名称是否和需要的相同
                String interfaceDisplayName = networkInterface.getDisplayName();
                if (networkInterface.isUp()
                        && (DEFAULT_NET_INTERFACE_WLAN0.equals(interfaceDisplayName) || DEFAULT_NET_INTERFACE_ETH0.equals(interfaceDisplayName))) {
                    return networkInterface;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //通过子网掩码的位数计算子网掩码
    public static String calcMaskByPrefixLength(int length) {

        int mask = 0xffffffff << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int maskParts[] = new int[partsNum];
        int selector = 0x000000ff;

        for (int i = 0; i < maskParts.length; i++) {
            int pos = maskParts.length - 1 - i;
            maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
        }

        String result = "";
        result = result + maskParts[0];
        for (int i = 1; i < maskParts.length; i++) {
            result = result + "." + maskParts[i];
        }
        return result;
    }

    public static byte[] byteArrayToInt(int address) {
        byte[] addr = new byte[4];
        addr[0] = (byte) ((address >>> 24) & 0xFF);
        addr[1] = (byte) ((address >>> 16) & 0xFF);
        addr[2] = (byte) ((address >>> 8) & 0xFF);
        addr[3] = (byte) (address & 0xFF);
        return addr;
    }

    public static int intToByteArray(byte[] addr) {
        int address = addr[3] & 0xFF;
        address |= ((addr[2] << 8) & 0xFF00);
        address |= ((addr[1] << 16) & 0xFF0000);
        address |= ((addr[0] << 24) & 0xFF000000);
        return address;
    }

    static String IPV4_REGEX = "^((\\d|[1-9]\\d|1\\d\\d|2([0-4]\\d|5[0-5]))\\.){4}";
    static String IPV6_REGEX = "^(([\\da-fA-F]{1,4}):){8}$";

    public static boolean isIPAddress(String host) {
        return (host + ".").matches(IPV4_REGEX) || (host + ":").matches(IPV6_REGEX);
    }
}
