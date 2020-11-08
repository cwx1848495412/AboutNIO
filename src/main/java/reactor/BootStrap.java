package reactor;

import reactor.EventThreadGroup.AcceptThreadGroup;
import reactor.EventThreadGroup.ReadThreadGroup;
import reactor.EventThreadGroup.WriteThreadGroup;

/**
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/11/8 15:28
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public class BootStrap {

    private int port;

    private int threadPoolSize;

    public AcceptThreadGroup acceptThreadGroup = new AcceptThreadGroup();

    public ReadThreadGroup readThreadGroup = new ReadThreadGroup();

    public WriteThreadGroup writeThreadGroup = new WriteThreadGroup();

    public BootStrap bindPort(int port) {
        this.port = port;
        return this;
    }

    public BootStrap bindThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
        return this;
    }

    /**
     * 启动服务器
     *
     * @return
     */
    public BootStrap start() throws Exception {
        startAcceptThreadGroup();
        startReadThreadGroup();
        startWriteThreadGroup();
        return this;
    }

    /**
     * 启动 accept 线程组
     *
     * @throws Exception
     */
    private void startAcceptThreadGroup() throws Exception {
        // 设置端口
        acceptThreadGroup.setPort(port);
        // 设置读的线程组
        acceptThreadGroup.setReadThreadGroup(readThreadGroup);
        acceptThreadGroup.initThreads(1);
        acceptThreadGroup.startThreads();
    }


    /**
     * 启动 read 线程组
     *
     * @throws Exception
     */
    private void startReadThreadGroup() throws Exception {
        readThreadGroup.setWriteThreadGroup(writeThreadGroup);
        readThreadGroup.initThreads(threadPoolSize);
        readThreadGroup.startThreads();
    }

    /**
     * 启动 write 线程组
     *
     * @throws Exception
     */
    private void startWriteThreadGroup() throws Exception {
        writeThreadGroup.initThreads(threadPoolSize);
        writeThreadGroup.startThreads();
    }


    public synchronized void sync() throws Exception {
        this.wait();
    }

}
