package learn;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/**
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/11/7 20:17
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public class C10KClient {
    public static void main(String[] args) {
        // 持有引用 不释放连接
        LinkedList<Object> list = new LinkedList<>();
        InetSocketAddress serverAddr =
                new InetSocketAddress("192.168.30.128", 815);

        for (int i = 10000; i < 65000; i++) {
            try {
                SocketChannel client = SocketChannel.open();

                client.bind(new InetSocketAddress("192.168.30.1", i));
                client.connect(serverAddr);
                boolean client1Open = client.isOpen();
                list.add(client);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("connect success:" + (i - 10000));
        }

        System.out.println("client size:" + list.size());

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
