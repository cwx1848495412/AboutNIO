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

        // 创建一个channel
        SocketChannel socketChannel = SocketChannel.open();

        // 配置非阻塞
        socketChannel.configureBlocking(false);

        // 监听端口 此时是异步的 可能并没有连接成功
        socketChannel.connect(new InetSocketAddress(hostname, port));

        // 关心连接事件
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }


    public void start() throws Exception {
        initServer();

        while (true) {
            // 本方法是非阻塞的
            selector.select();

            // 拿到所有的事件
            // 可能包含  OP_CONNECT OP_READ OP_WRITE
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // selector 不会删除已经添加的事件
                // 我们获取之后需要手动删除
                // 不然此逻辑会重复消费
                iterator.remove();

                if (key.isConnectable()) {
                    // 有连接事件触发 调用处理
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
                    // 不管之前的事件集包含什么 本次写做完之后 将写事件移除
                    // 因为此处不同于服务端
                    // 会有 READ | WRITE
                    // 以及 CONNECT | WRITE 共存
                    // 所以无论什么样的事件集 只移除写事件
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

        // 此时关心写事件 触发读写轮回
        // 因为客户端连接成功后
        // 服务端和客户端 总要有一个人先开口 才能聊得起来
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

        // 给服务端写回消息
        String baskStr = new String(msg + "\n");
        ByteBuffer outBuffer = ByteBuffer.wrap(baskStr.getBytes("utf-8"));
        channel.write(outBuffer);
    }

    public static void main(String[] args) throws Exception {
        new Client().start();
    }
}
