package reactor.inter;

/**
 * 事件线程组接口规范
 *
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/11/8 15:32
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public interface ThreadGroup {

    /**
     * 初始化线程组
     *
     * @param threadNumbers 线程数量
     * @throws Exception
     */
    void initThreads(int threadNumbers) throws Exception;

    /**
     * 启动所有线程
     */
    void startThreads();

    /**
     * 获取均分的下一个事件线程对象
     *
     * @return
     */
    EventThread allocateEventThread();

}
