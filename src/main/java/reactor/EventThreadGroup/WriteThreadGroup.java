package reactor.EventThreadGroup;

import reactor.EventThread.WriteThread;
import reactor.inter.EventThread;
import reactor.inter.ThreadGroup;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/11/8 15:22
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public class WriteThreadGroup implements ThreadGroup {
    private String threadName = "Write-Thread-";

    public ArrayList<WriteThread> threads = new ArrayList<WriteThread>();

    // 当前线程组的分配自增序列
    AtomicInteger atomicInteger = new AtomicInteger();

    /**
     * 初始化线程组
     * 创建线程 并再创建的过程中 初始化每个对应的 selector
     *
     * @param threadNumbers 线程数量
     */
    @Override
    public void initThreads(int threadNumbers) throws Exception {
        for (int i = 0; i < threadNumbers; i++) {
            WriteThread writeThread = new WriteThread();
            writeThread.setName(threadName + i);
            writeThread.setWriteThreadGroup(this);

            threads.add(writeThread);
        }
    }

    /**
     * 启动所有创建好的线程
     */
    @Override
    public void startThreads() {
        for (WriteThread thread : threads) {
            thread.start();
        }
    }

    /**
     * 获取均分的下一个事件线程对象
     *
     * @return
     */
    @Override
    public EventThread allocateEventThread() {
        int xId = atomicInteger.getAndIncrement();

        int i = xId % threads.size();

        return threads.get(i);
    }

}
