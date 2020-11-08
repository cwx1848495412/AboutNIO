package reactor.EventThread;

import reactor.EventThreadGroup.AcceptThreadGroup;
import reactor.Log.Logger;
import reactor.inter.EventThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/11/8 15:15
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public class AcceptThread extends Thread implements EventThread {
    private final Logger logger = new Logger();

    // 每个事件线程要有自己的线程组引用
    private AcceptThreadGroup acceptThreadGroup;

    // 每个线程有一个属于自己的 selector
    private Selector selector;

    public AcceptThread(int port) throws Exception {
        selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public AcceptThreadGroup getAcceptThreadGroup() {
        return acceptThreadGroup;
    }

    public void setAcceptThreadGroup(AcceptThreadGroup threadGroup) {
        this.acceptThreadGroup = threadGroup;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 打印点
                logger.log(this.getName() + " ready to select......");

                // select 此时会阻塞 没有任何人注册 OP_ACCEPT
                int readyKeys = selector.select();

                // handle keys
                if (readyKeys > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        // 此 selector 只会往上注册 accept 事件
                        // 不会处理其他事件
                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        }
                    }
                }
                // run all task

            } catch (Exception e) {
                logger.log("accept Exception");
                e.printStackTrace();
            }
        }

    }

    private void acceptHandler(SelectionKey key) throws IOException {
        logger.log(this.getName() + " acceptHandler.......");
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();

        SocketChannel clientChannel = serverSocket.accept();

        clientChannel.configureBlocking(false);

        // 测试性能打印
        logger.log(this.getName() + " " + clientChannel.getRemoteAddress());

        EventThread eventThread = this.getAcceptThreadGroup().getReadThreadGroup().allocateEventThread();
        ReadThread readThread = (ReadThread) eventThread;

        // TODO: 读的 selector 要换 已经更换完成
        // 注册到均分的 读的 selector 上去
        readThread.getTaskList().add(clientChannel);
        // 唤醒 selector
        readThread.getSelector().wakeup();
        // clientChannel.register(readThread.getSelector(), SelectionKey.OP_READ);
    }
}
