package learn;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/11/4 09:16
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public class FileChannelLearn {
    public static void main(String[] args) throws Exception {
        RandomAccessFile file = new RandomAccessFile("nio-data.txt", "rw");
        FileChannel channel = file.getChannel();

        ByteBuffer buffer = ByteBuffer.allocateDirect(50);
        int read = channel.read(buffer);

        // 切换读模式 读出往数组里写
        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        System.out.println(Arrays.toString(bytes));

        channel.close();
        file.close();
    }
}
