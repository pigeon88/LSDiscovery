package com.example.coap;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class HelloUdpMessage extends MessageHeader {

    static final int __1M = 1024 * 1024; //1M
    static final short DEFAULT_HELLO_INTERVAL = 10;
    static final short DEFAULT_DEAD_INTERVAL = DEFAULT_HELLO_INTERVAL * 4;

    /**
     * 发送Hello报文的接口所在网络的掩码，如果相邻两台路由器的网络掩码不同，则不能建立邻居关系。
     */
    int networkMask;

    /**
     * 发送Hello报文的时间间隔。
     */
    short helloInterval;

    /**
     * 失效时间。如果在此时间内未收到邻居发来的Hello报文，则认为邻居失效。
     */
    short routerDeadInterval;

    /**
     * 路由器优先级。如果设置为0，则该路由器接口不能成为DR/BDR。统计出当前APP应用可用内存以KB为单位
     */
    int routerPriority;

    /**
     * 指定路由器DR的接口的IP地址。
     */
    int designatedRouter;

    /**
     * 备份指定路由器BDR的接口的IP地址。
     */
    int backupDesignatedRouter;

    /**
     * 邻居路由器的Router ID。
     */
    int[] activeNeighbors = new int[0];

    public HelloUdpMessage() {
        super((byte) 1, 0);
    }

    public HelloUdpMessage(long routerId, int networkMask, short helloInterval, short routerDeadInterval, int routerPriority) {
        super((byte) 1, routerId);
        this.networkMask = networkMask;
        this.helloInterval = helloInterval;
        this.routerDeadInterval = routerDeadInterval;
        this.routerPriority = routerPriority;
    }

    public static HelloUdpMessage getDefault() {
        return new HelloUdpMessage(Utils.getIpAddressHashCode(),
                Utils.getIpAddressMask(),
                DEFAULT_HELLO_INTERVAL,
                DEFAULT_DEAD_INTERVAL,
                (int) (Runtime.getRuntime().maxMemory() / __1M)
        );
    }

    protected void calculateLength() {
        super.calculateLength();
        length = (short) (length
                + sizeOf(networkMask)
                + sizeOf(helloInterval)
                + sizeOf(routerDeadInterval)
                + sizeOf(routerPriority)
                + sizeOf(designatedRouter)
                + sizeOf(backupDesignatedRouter)
                + sizeOf(0) * activeNeighbors.length);
    }

    @Override
    public byte[] toByteArray() {
        calculateLength();
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(version);
        buffer.put(type);
        buffer.putShort(length);
        buffer.putLong(routerId);
        buffer.putInt(networkMask);
        buffer.putShort(helloInterval);
        buffer.putShort(routerDeadInterval);
        buffer.putInt(routerPriority);
        buffer.putInt(designatedRouter);
        buffer.putInt(backupDesignatedRouter);
        for (int activeNeighbor : activeNeighbors) {
            buffer.putInt(activeNeighbor);
        }
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] bytes) {
        super.fromByteArray(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        version = buffer.get();
        type = buffer.get();
        length = buffer.getShort();
        routerId = buffer.getLong();
        networkMask = buffer.getInt();
        helloInterval = buffer.getShort();
        routerDeadInterval = buffer.getShort();
        routerPriority = buffer.getInt();
        designatedRouter = buffer.getInt();
        backupDesignatedRouter = buffer.getInt();

        int arouterLen = length - buffer.position();
        int[] aaaa = new int[arouterLen / 4];
        for (int i = 0; i < arouterLen; i += 4) {
            aaaa[i / 4] = buffer.getInt();
        }
    }

    /**
     * 判断是否包含当前ip
     *
     * @param address
     * @return
     */
    public boolean containsNeighbor(int address) {
        if (activeNeighbors != null) {
            for (int neighbor : activeNeighbors) {
                if (neighbor == address) {
                    return true;
                }
            }
        }
        return false;
    }

    /*public static byte[] toBytes(HelloUdpMessage message) {
        message.calculateLength();
        ByteBuffer buffer = ByteBuffer.allocate(message.length);
        buffer.putShort(message.version);
        buffer.putShort(message.type);
        buffer.putInt(message.length);
        if (message.content != null) {
            buffer.put(message.content);
        }
        return buffer.array();
    }

    public static HelloUdpMessage fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        MessagePrt message = new MessagePrt();
        message.version = buffer.getShort();
        message.type = buffer.getShort();
        int len = buffer.getInt();
        message.length = len;
        if (len > DEFAULT_LENGTH) {
            byte[] dst = new byte[len - DEFAULT_LENGTH];
            buffer.get(dst);
            message.content = dst;
        }
        return message;
    }*/

    @Override
    public String toString() {
        return super.toString() + " -> HelloUdpMessage{" +
                "networkMask=" + networkMask +
                ", helloInterval=" + helloInterval +
                ", routerDeadInterval=" + routerDeadInterval +
                ", routerPriority=" + routerPriority +
                ", designatedRouter=" + designatedRouter +
                ", backupDesignatedRouter=" + backupDesignatedRouter +
                ", activeNeighbors=" + Arrays.toString(activeNeighbors) +
                '}';
    }
}
