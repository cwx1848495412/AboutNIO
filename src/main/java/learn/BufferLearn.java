package learn;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/10/23 09:49
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public class BufferLearn {
    public static void main(String[] args) {
        // 分配内存
        ByteBuffer buffer = ByteBuffer.allocateDirect(50);
        // 分配堆外内存
        // ByteBuffer buffer = ByteBuffer.allocateDirect(50);

        // 写十个字节进去
        byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        buffer.put(bytes);
        printStatus(buffer);

        // 切换读模式
        buffer.flip();
        System.out.println("======flip======");
        printStatus(buffer);

        buffer.get();
        buffer.get();
        buffer.get();
        buffer.get();
        buffer.get();
        buffer.get();
        buffer.get();
        System.out.println("======get 7 over======");
        printStatus(buffer);

        buffer.mark();
        System.out.println("======mark 7 over======");
        printStatus(buffer);

        buffer.get();
        buffer.get();// 9

        System.out.println("======get 9 over======");
        printStatus(buffer);

        // 发现不够一个消息体或者什么协议逻辑规定字节
        // reset 回到逻辑标记位
        // 配合剩余的 粘包拆包用
        // 将本次剩余的 reset的那些 放到下次
        buffer.reset();
        System.out.println("======reset over======");
        printStatus(buffer);

        // 将上次回滚剩余的3个
        // 放到这次的前三个  接着写
        buffer.compact();
        System.out.println("======compact over======");
        printStatus(buffer);

        // 上面操作已经切换到了写模式 要读再去flip 数据包完整
        // 再写十个进去
        buffer.put(bytes);
        System.out.println("======put over======");
        printStatus(buffer);

        // 再读 完成粘包
        buffer.flip();
        System.out.println("======flip again over======");
        printStatus(buffer);

        byte[] bytesAgain = new byte[13];
        buffer.get(bytesAgain);
        System.out.println("======get over======");
        printStatus(buffer);

        // 粘包数据打印
        // 上一次的 789 剩余三个
        // 和本次的完整十个  正好
        System.out.println("bytesAgain" + Arrays.toString(bytesAgain));
    }

    private static void printStatus(ByteBuffer buffer) {
        System.out.println("position:" + buffer.position());
        System.out.println("limit:" + buffer.limit());
        System.out.println("capacity:" + buffer.capacity());
    }
}
