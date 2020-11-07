package learn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOServerThread {

//    static byte[] bs = new byte[1024];


    // Blocking IO  阻塞IO模型 Tomcat
    // 弊端  每链接一个  就会有一个线程被创建 占用资源过大

    //    应用层
//    Tcp 三次握手  建立连接
//    close 四次挥手  释放资源（持有的线程）
    public static void main(String[] args) {
        try {
            int port = 815;
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server start, port:" + port);
            while (true) {
//                 阻塞点一
                Socket clientSocket = serverSocket.accept();


                System.out.println(clientSocket.getInetAddress() + "--" + clientSocket.getPort());

                // 多线程此处会创建过多子线程
                new Thread() {
                    @Override
                    public void run() {
                        byte[] bs = new byte[1024];
                        try {
                            String name = Thread.currentThread().getName();
//                            System.out.println(name + " Thread wait data");

                            InputStream inputStream = clientSocket.getInputStream();
                            inputStream.read(bs);

//                            System.out.println(name + " Receiver data success");

//                            System.out.println(new String(bs));

                            OutputStream outputStream = clientSocket.getOutputStream();
                            outputStream.write(bs);
                            inputStream.close();
                            outputStream.close();

//                            System.out.println(name + " End");
                        } catch (Exception e) {
                        }
                    }
                }.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
