package reactor;

/**
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/11/8 15:43
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public class Main {
    public static void main(String[] args) throws Exception {
        BootStrap bootStrap = new BootStrap();
        bootStrap.bindPort(815).bindThreadPoolSize(3).start().sync();
    }
}
