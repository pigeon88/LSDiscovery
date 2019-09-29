package com.example.coap;

import java.util.HashMap;
import java.util.Map;

public class MessageHeader {

    //当前版本(通信版本必须一致)
    public static final int VERSION = 1;

    public static final short TYPE_HELLO = 1;

    byte version = VERSION;

    //type=1为hello报文，用来建立和维护邻居关系，邻居关系建立之前，路由器之间需要进行参数协商
    byte type;

    short length;

    long routerId; //主机mac

    //short checksum; //校验整个OSPF报文
    //short authType; //认证类型（1、明文；2、MD5）
    //authData

    public MessageHeader(byte type, long routerId) {
        this.type = type;
        this.routerId = routerId;
    }

    protected void calculateLength() {
        length = (short) (sizeOf(version, type, length, routerId));
    }

    public byte[] toByteArray() {
        return new byte[0];
    }

    public void fromByteArray(byte[] bytes) {

    }

    @Override
    public String toString() {
        return "MessageHeader{" +
                "version=" + version +
                ", type=" + type +
                ", length=" + length +
                ", routerId=" + routerId +
                '}';
    }

    public static final Map<Class, Integer> sizeOfMap = new HashMap<>();

    static {
        sizeOfMap.put(Byte.class, 1);
        sizeOfMap.put(Short.class, 2);
        sizeOfMap.put(Integer.class, 4);
        sizeOfMap.put(Long.class, 8);
        sizeOfMap.put(Float.class, 4);
        sizeOfMap.put(Double.class, 8);
    }

    protected int sizeOf(Number... numbers) {
        int count = 0;
        if (numbers != null) {
            for (Number number : numbers) {
                count += sizeOfMap.get(number.getClass());
            }
        }
        return count;
    }
}
