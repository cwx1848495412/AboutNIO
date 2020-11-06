package practice;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/11/6 15:02
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public class Client {
    // selector
    private Selector selector;
    private String hostname = "10.10.24.85";
    private int port = 815;

    private void initServer() throws Exception {
        // 创建selector
        selector = Selector.open();

        SocketChannel socketChannel = SocketChannel.open();

        // 配置非阻塞
        socketChannel.configureBlocking(false);

        // 监听端口
        socketChannel.connect(new InetSocketAddress(hostname, port));

        // 注册连接事件
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }


    public void start() throws Exception {
        initServer();

        while (true) {
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isConnectable()) {
                    connectHandler(key);
                }

                if (key.isReadable()) {
                    String msg = readHandler(key);
                    // SelectionKey 去关心写事件 因为此时要写了
                    int alreadyOps = key.interestOps();
                    key.interestOps(alreadyOps | SelectionKey.OP_WRITE);
                    key.attach(msg);
                }

                if (key.isWritable()) {
                    // 写事件只要操作系统写队列可写 就会有
                    // 但是什么时候要写是我们自己才知道的
                    writeHandler(key);
                    // 发送完了就取消写事件
                    // 否则下次还会由于操作系统写队列可写而触发写事件
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                }

            }

        }
    }

    private void connectHandler(SelectionKey key) throws Exception {
        System.out.println("connectHandler.......");
        SocketChannel channel = (SocketChannel) key.channel();

        // 等待连接完成
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }

        channel.configureBlocking(false);

        // 此时注册写事件 触发读写轮回
        channel.register(selector,
                SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                "Client connect success, start talking...");
    }

    private String readHandler(SelectionKey key) throws Exception {
        System.out.println("readHandler.......");

        SocketChannel channel = (SocketChannel) key.channel();
        // 创建读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(512);
        channel.read(buffer);

        byte[] data = buffer.array();
        String msg = new String(data).trim();

        System.out.println("----------------------------------------------------");
        System.out.println("服务端返回：" + msg);
        System.out.println("----------------------------------------------------");
        return msg;
    }

    private void writeHandler(SelectionKey key) throws Exception {
        System.out.println("writeHandler.......");
        // 客户端写回消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        String msg = (String) key.attachment();

        // 给客户端写回消息
        String baskStr = new String(msg + "\n");
        ByteBuffer outBuffer = ByteBuffer.wrap(baskStr.getBytes("utf-8"));
        channel.write(outBuffer);
    }

    public static void main(String[] args) throws Exception {
        new Client().start();
    }
}
