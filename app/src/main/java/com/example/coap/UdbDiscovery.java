package com.example.coap;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UdbDiscovery extends NetworkDiscovery {

    InetAddress inetAddress;
    ExecutorService executorService;
    ScheduledExecutorService scheduledExecutorService;
    HelloUdpMessage helloMessageSelf;
    MulticastSocket socket;
    Map<Integer, NeighborRecord> neighborRecords = new ConcurrentSkipListMap<>();
    ScheduledFuture<?> helloTimer;
    ScheduledFuture<?> waitTimer;

    int waitTimerCount = 5;
    boolean neighborChange = false;
    boolean backupSeen = false;

    public UdbDiscovery(String host, int port) throws IOException {
        inetAddress = InetAddress.getByName(host);
        socket = new MulticastSocket(port);
        socket.setTimeToLive(32);
        socket.setLoopbackMode(true);
        socket.joinGroup(inetAddress);
        helloMessageSelf = HelloUdpMessage.getDefault();
    }

    public void start() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new UdpReceivedRunnable());
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        helloTimer = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    //大于0的才有选举资格
                    if (helloMessageSelf.routerPriority > 0) {
                        if (waitTimerCount > 0) {
                            waitTimerCount--;
                            //选举计时器到期，进行选择
                            if (waitTimerCount == 0) {
                                if (helloMessageSelf.designatedRouter == 0 || helloMessageSelf.backupDesignatedRouter == 0) {
                                    electionDr();
                                }
                            }
                        }
                    }

                    //设置失效，超过40秒未更新的邻居
                    helloMessageSelf.activeNeighbors = getActiveNeighbors();
                    sendHello(helloMessageSelf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, helloMessageSelf.helloInterval, TimeUnit.SECONDS);

        waitTimer = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //设置失效，超过40秒未更新的邻居
                for (NeighborRecord record : neighborRecords.values()) {
                    record.decrementDeadTime();
                    notifyNeighborChanged(neighborRecords.values());
                }
            }
        }, 10, 1, TimeUnit.SECONDS);
    }

    private int[] getActiveNeighbors() {
        int activeCount = 0;
        Collection<NeighborRecord> values = neighborRecords.values();
        int[] result = new int[values.size()];
        for (Iterator<NeighborRecord> iterator = values.iterator(); iterator.hasNext(); ) {
            NeighborRecord next = iterator.next();
            if (next.getState() > NeighborRecord.STATE_DOWN) {
                result[activeCount++] = next.address;
            }
        }

        return Arrays.copyOf(result, activeCount);
    }

    /**
     * 先选举BDR，再选择DR
     */
    private void electionDr() {
        System.out.println("electionDr [" + helloMessageSelf.routerId + "] ");
        NeighborRecord dr = getNeighborDR();
        if (dr != null) {
            helloMessageSelf.backupDesignatedRouter = dr.address;
            if (helloMessageSelf.designatedRouter == 0) {
                helloMessageSelf.designatedRouter = helloMessageSelf.backupDesignatedRouter;
                helloMessageSelf.backupDesignatedRouter = 0;
                electionDr();
            }
        }

        System.out.println("electionDr " + helloMessageSelf);
    }

    private NeighborRecord getNeighborDR() {
        NeighborRecord dr = (helloMessageSelf.routerPriority > 0 && Utils.getLocalAddress() != helloMessageSelf.designatedRouter)
                ? new NeighborRecord(helloMessageSelf.routerId, helloMessageSelf.routerPriority, helloMessageSelf.routerDeadInterval, Utils.getLocalAddress())
                : null;
        Collection<NeighborRecord> values = neighborRecords.values();
        for (NeighborRecord record : values) {
            if (record.getState() >= NeighborRecord.STATE_2WAY && record.getPriority() > 0) {
                if (dr == null) {
                    dr = record;
                } else {
                    if (dr.getPriority() < record.getPriority()
                            || (dr.getPriority() == record.getPriority() && dr.neighborId < record.neighborId)) {
                        dr = record;
                    }
                }
            }
        }
        return dr;
    }

    public void sendHello(HelloUdpMessage message) throws IOException {
        System.out.println(">>> [" + inetAddress.getHostAddress() + ":" + socket.getLocalPort() + "] " + message);
        byte[] data = message.toByteArray();
        DatagramPacket dataPacket = new DatagramPacket(data, data.length, inetAddress, socket.getLocalPort());
        socket.send(dataPacket);
    }

    public void recvHello() {
        byte buf[] = new byte[Env.UDP_BUFF_SIZE];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                socket.receive(dp);
                byte[] data = dp.getData();
                HelloUdpMessage helloMessage = new HelloUdpMessage();
                helloMessage.fromByteArray(data);
                System.out.println("<<< [" + dp.getAddress().getHostAddress() + ":" + dp.getPort() + "] " + helloMessage);
                checkMessage(helloMessage);
                recvHelloHandle(dp, helloMessage);
            } catch (Exception e) {
                System.err.println("<<< [" + dp.getAddress().getHostAddress() + ":" + dp.getPort() + "] " + e.getMessage());
            }
        }
    }

    private void recvHelloHandle(DatagramPacket dp, HelloUdpMessage helloMessage) {
        if (helloMessage.type == MessageHeader.TYPE_HELLO) {
            int neighborId = dp.getAddress().hashCode();
            NeighborRecord neighborRecord = neighborRecords.get(neighborId);
            if (neighborRecord == null) {
                neighborRecord = new NeighborRecord(neighborId, helloMessage.routerPriority, helloMessage.routerDeadInterval, Utils.intToByteArray(dp.getAddress().getAddress()));
                neighborRecords.put(neighborId, neighborRecord);
            } else {
                //如果发现邻居优先级有改变，接收接口状态机调度执行事件 NeighborChange。
                if (neighborRecord.priority != helloMessage.routerPriority) {
                    neighborRecord.priority = helloMessage.routerPriority;
                    neighborChange = true;
                }
            }

            //判断Hello是否包含自己
            boolean containsNeighbor = helloMessage.containsNeighbor(socket.getLocalAddress().hashCode());
            if (containsNeighbor) {
                neighborRecord.setState(NeighborRecord.STATE_2WAY);
                notifyNeighborChanged(neighborRecords.values());
            } else {
                if (neighborRecord.getState() < NeighborRecord.STATE_2WAY) {
                    neighborRecord.setState(NeighborRecord.STATE_INIT);
                    notifyNeighborChanged(neighborRecords.values());
                }
            }

            if (helloMessage.designatedRouter > 0) {
                //如果发现自己是DR
                if (helloMessage.designatedRouter == helloMessageSelf.routerId) {
                    backupSeen = true;
                    //启动socket

                }
            }

            System.out.println("NeighborRecord[" + dp.getAddress().getHostAddress() + "] " + neighborRecord);
        }
    }

    /**
     * 校验版本及数据有效性
     *
     * @param helloMessage
     */
    private void checkMessage(HelloUdpMessage helloMessage) {
        if (helloMessage.version != HelloUdpMessage.VERSION
                || helloMessage.networkMask != Utils.getIpAddressMask()) {
            throw new SecurityException("signature verification failed: " + helloMessage.version + "|" + helloMessage.networkMask);
        }
    }

    public void stop() {
        neighborRecords.clear();
        if (helloTimer != null) {
            helloTimer.cancel(true);
            helloTimer = null;
        }
        if (waitTimer != null) {
            waitTimer.cancel(true);
            waitTimer = null;
        }
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
        }
        executorService.shutdownNow();
        //socket.leaveGroup();
        socket.close();
        notifyNeighborChanged(neighborRecords.values());
    }

    class UdpReceivedRunnable implements Runnable {


        public void run() {
            recvHello();
        }
    }
}
