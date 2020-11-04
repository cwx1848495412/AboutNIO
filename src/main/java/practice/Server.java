package practice;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/11/4 09:48
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public class Server {
    // selector
    private Selector selector;
    private String hostname = "10.10.24.85";
    private int port = 815;

    private void initServer() throws Exception {
        // 创建selector
        selector = Selector.open();

        // 创建serverSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 给服务端绑定端口
        serverSocketChannel.bind(new InetSocketAddress(hostname, port));

        // 设置服务端为非阻塞
        serverSocketChannel.configureBlocking(false);

        // 将这个服务端channel注册到selector上
        // 且因为是server 需要对接收事件感兴趣
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * 启动服务器
     *
     * @throws Exception
     */
    public void start() throws Exception {
        // 初始化 关注accept 事件
        initServer();

        // server Channel准备就绪 准备接收连接
        while (true) {
            System.out.println("Ready for select.......");
            // 获取可用channel 数量
            int readyChannels = selector.select();

            // 没有连接 继续轮询selector
            if (readyChannels == 0) continue;

            // 有连接 获取可用channel集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            // 里面可能有 OP_ACCEPT OP_CONNECT OP_READ OP_WRITE
            // 没有可能为空 有可能最多四个事件都有
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // selector 不会删除已经添加的事件
                // 我们获取之后需要手动删除
                // 不然此逻辑会重复消费
                iterator.remove();

                // 根据已经拿到的事件对象
                // 来决定要处理的业务
                if (key.isAcceptable()) {
                    // 接受事件
                    // 有客户端进来就触发
                    // 由server Channel处理
                    acceptHandler(key);
                }

                if (key.isReadable()) {
                    // 有数据进来 触发读事件
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

    /**
     * 接收连接事件处理
     *
     * @param key
     * @throws Exception
     */
    private void acceptHandler(SelectionKey key) throws Exception {
        System.out.println("acceptHandler.......");
        // 有accept 事件了 可以确定一定有accept
        // 这时调用阻塞的方法 去获取接受
        // 因为事件已经有了  所以这个阻塞一经开启 必中 无需多于等待
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();

        SocketChannel clientChannel = serverSocket.accept();
        // 将server 配置为非阻塞
        clientChannel.configureBlocking(false);

        // 且将本channel 注册到全局的selector上
        // 因为你本身已经监听了接受事件
        // 由你派生出来的channel 要处理的是你接受之后
        // 客户端给你发送的数据 你要去读
        // 所以这块要监听读事件
        clientChannel.register(selector, SelectionKey.OP_READ);
        // 已经就绪 准备好读客户端发来的请求了
    }

    /**
     * 读事件处理
     *
     * @param key
     * @throws Exception
     */
    private String readHandler(SelectionKey key) throws Exception {
        System.out.println("readHandler.......");
        // 服务器可读取消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        // 创建读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(512);
        channel.read(buffer);

        byte[] data = buffer.array();
        String msg = new String(data).trim();

        System.out.println("客户端消息：" + msg);
        return msg;
    }

    /**
     * 回写
     *
     * @param key
     */
    private void writeHandler(SelectionKey key) throws Exception {
        System.out.println("writeHandler.......");
        // 服务器写回消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        String msg = (String) key.attachment();

        // 给客户端写回消息
        String baskStr = new String("Server receive:" + msg + "\n");
        ByteBuffer outBuffer = ByteBuffer.wrap(baskStr.getBytes("utf-8"));
        channel.write(outBuffer);
    }


    public static void main(String[] args) throws Exception {
        new Server().start();
    }

}
