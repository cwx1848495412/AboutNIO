package reactor.Log;

import reactor.inter.Log;

/**
 * @Auther: 苏察哈尔丶灿
 * @Date: 2020/11/8 16:05
 * @Slogan: 我自横刀向天笑，笑完我就去睡觉。
 */
public class Logger implements Log {
    private static boolean printLog = true;

    /**
     * 打印接口
     *
     * @param log
     */
    @Override
    public void log(Object log) {
        if (printLog) {
            System.out.println(log);
        }
    }
}
