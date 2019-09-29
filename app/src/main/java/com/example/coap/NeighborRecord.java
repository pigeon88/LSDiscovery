package com.example.coap;

public class NeighborRecord {

    public static final int STATE_DOWN = 0;
    public static final int STATE_INIT = 1;
    public static final int STATE_2WAY = 2;

    /**
     * mac地址
     */
    long neighborId; //mac

    /**
     * 优先级。默认为App应用内存M。如果设置为0，则路由器不能参与DR或BDR的选举。
     */
    int priority;

    /**
     * ip地址
     */
    int address;

    //int designatedRouter;

    //int backupDesignatedRouter;

    /**
     * 0 down 1 init 2 two-way
     * 0.down state:路由器还末收到邻居发来的HELLO包
     * 1.init state:路由器收到邻居发来的HELLO包，但在邻居阶段中看不到自己的ROUTER-ID
     * 2.two-way state:路由器在收到邻居发来的HEELO包中，能够看到自己的ROUTER-ID，就进入该状态(需要选举DR和BDR的在这阶段选举)
     */
    int state;

    /**
     * 失效时间
     */
    int deadTime;

    long activeTime;

    public NeighborRecord(long neighborId, int priority, int deadTime, int address) {
        this.neighborId = neighborId;
        this.priority = priority;
        this.deadTime = deadTime;
        this.address = address;
        this.activeTime = System.currentTimeMillis();
    }

    public long getNeighborId() {
        return neighborId;
    }

    public int getPriority() {
        return priority;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        if (state > STATE_DOWN) {
            deadTime = Env.UDP_HELLO_DEAD_TIME;
            activeTime = System.currentTimeMillis();
        }
    }

    public int getDeadTime() {
        return deadTime;
    }

    public int getAddress() {
        return address;
    }

    public String getAddressString() {
        byte[] addr = Utils.byteArrayToInt(address);
        return (addr[0] & 0xff) + "." + (addr[1] & 0xff) + "." + (addr[2] & 0xff) + "." + (addr[3] & 0xff);
    }

    public void decrementDeadTime() {
        if (deadTime > 0) {
            deadTime--;
            if (deadTime == 0) {
                setState(STATE_DOWN);
            }
        }
    }

    @Override
    public String toString() {
        return "NeighborRecord{" +
                "neighborId=" + neighborId +
                ", priority=" + priority +
                ", state=" + state +
                ", deadTime=" + deadTime +
                ", address=" + getAddressString() +
                '}';
    }
}
