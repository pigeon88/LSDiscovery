package com.example.coap;

import java.nio.ByteBuffer;

public class MessagePrt {

    public static final int DEFAULT_LENGTH = 2; // type + length, 1 + 1 = 2

    //当前版本(通信版本必须一致)
    public static final int VERSION = 1;

    private short version = VERSION;

    //type=1为hello报文，用来建立和维护邻居关系，邻居关系建立之前，路由器之间需要进行参数协商
    private short type = 1;

    private int length = DEFAULT_LENGTH;

    //short checksum; //校验整个OSPF报文
    //short authType; //认证类型（1、明文；2、MD5）
    //authData

    private byte[] content;

    private void calculateLength() {
        if (content != null) {
            length = DEFAULT_LENGTH + content.length; // type + length + contentLength;
        } else {
            length = DEFAULT_LENGTH; // type + length;
        }
    }

    public static byte[] toBytes(MessagePrt message) {
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

    public static MessagePrt fromBytes(byte[] bytes) {
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
    }
}
