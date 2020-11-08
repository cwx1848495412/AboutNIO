package reactor.EventThread;

import reactor.EventThreadGroup.ReadThreadGroup;
import reactor.Log.Logger;
import reactor.inter.EventThread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/11/8 16:44
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public class ReadThread extends Thread implements EventThread {

    private final Logger logger = new Logger();

    private ReadThreadGroup readThreadGroup;

    private LinkedList<SocketChannel> taskList = new LinkedList<SocketChannel>();

    // 每个线程有一个属于自己的 selector
    private Selector selector;

    public ReadThread() throws Exception {
        selector = Selector.open();
    }

    public LinkedList<SocketChannel> getTaskList() {
        return taskList;
    }

    public void setTaskList(LinkedList<SocketChannel> taskList) {
        this.taskList = taskList;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public ReadThreadGroup getReadThreadGroup() {
        return readThreadGroup;
    }

    public void setReadThreadGroup(ReadThreadGroup readThreadGroup) {
        this.readThreadGroup = readThreadGroup;
    }

    @Override
    public void run() {
        while (true) {
            try {
                logger.log(this.getName() + " ready to select......");

                // 这个select 是阻塞的
                int readyKeys = selector.select();

                if (readyKeys > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();

                        iterator.remove();

                        if (key.isReadable()) {
                            readHandler(key);
                        }
                    }
                }

                // run all task
                if (!taskList.isEmpty()) {
                    SocketChannel clientChannel = taskList.poll();
                    clientChannel.register(selector, SelectionKey.OP_READ);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取处理
     *
     * @param key
     */
    private void readHandler(SelectionKey key) throws IOException {
        logger.log(this.getName() + " readHandler.......");
        // 服务器可读取消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel) key.channel();

        // 创建读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(512);
        int read = channel.read(buffer);

        if (read < 0) {
            logger.log(channel.getRemoteAddress() + " 连接断开");
            key.cancel();
            return;
        }

        byte[] data = buffer.array();
        String msg = new String(data).trim();

        logger.log("----------------------------------------------------");
        logger.log(this.getName() + " 客户端消息：" + msg);
        logger.log("----------------------------------------------------");

        // TODO: 写的 selector 要换 已经更换完成
        key.attach(msg);
        EventThread eventThread = this.getReadThreadGroup().getWriteThreadGroup().allocateEventThread();
        WriteThread writeThread = (WriteThread) eventThread;

        writeThread.getTaskList().add(key);
        writeThread.getSelector().wakeup();
        // channel.register(selector, SelectionKey.OP_WRITE, msg);
    }

}
