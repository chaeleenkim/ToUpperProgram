package main.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;

public class Client {

    SocketChannel socketChannel;

    Map<Integer, SocketChannel> connectionPool = new HashMap<>(); //<소켓채널아이디, 소켓채널>
    Map<Integer, Thread> connectionThreads = new HashMap<>();
    String data;


    ByteBuffer receiveBuffer = ByteBuffer.allocate(100);
    Charset charset = Charset.forName("UTF-8");


    void startClient() {

        watchConnectionPool();
    }

    void watchConnectionPool() {
        makeConnectionPool();
        Thread watcherThread = new Thread() {
            @Override
            public void run() {
                for (int i = 1; i < connectionThreads.size() + 1; i++) {
                    connectionThreads.get(i).start();
                }
                while (true) {


                }
            }
        };

        watcherThread.start();

    }


    //소켓 채널 생성
    void makeSocketChannel() {


    }


    void makeConnectionPool() {
        for (int i = 1; i < 11; i++) {
            int finalI = i;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        socketChannel = SocketChannel.open();
                        socketChannel.configureBlocking(true);

                        if (finalI == 1) {
                            socketChannel.connect(new InetSocketAddress("localhost", 5001));
                        } else {
                            socketChannel.connect(new InetSocketAddress("localhost", 5002));
                        }

                        connectionPool.put(finalI, socketChannel);

                        System.out.println("socketChannel" + finalI + "번 연결 완료");


                    } catch (Exception e) {

                        System.out.println("[서버 통신 안 됨]");
                        System.out.println(finalI + "번 : " + connectionPool.get(finalI));
                        System.out.println("사이즈 : " + connectionPool.size());
                        /*if (socketChannel.isOpen()) {
                            stopClient();
                        }
                        return;*/
                    }
            /*
                         //스레드 작업 정의
                         if (finalI == 1) {
                           heartbeatCheck();
                         } else {
                           toUpper();
                         }
            */
                }


            };

            connectionThreads.put(finalI, thread);
            System.out.println(finalI + "번 스레드 : " + connectionThreads.get(finalI));
        }
    }


    void heartbeatCheck() {
        while (true) {
            socketChannel = connectionPool.get(1);
            data = " ";

            send(data, socketChannel);

            receive(socketChannel);

            if (data.equals("")) {

            } else {

            }


        }
    }

    void toUpper() {
        while (true) {
            System.out.println("연결중");
        }

    }


    void stopClient() {
        System.out.println("[연결 끊음]");


    }

    void send(String data, SocketChannel socketChannel) {
        try {
            ByteBuffer byteBuffer = charset.encode(data);
            socketChannel.write(byteBuffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    void receive(SocketChannel socketChannel) {
        while (true) {

            try {
                int readByteCount = socketChannel.read(receiveBuffer);

                if (readByteCount == -1) {
                    throw new IOException();
                }

                receiveBuffer.flip();
                data = charset.decode(receiveBuffer).toString();
                receiveBuffer.clear();


            } catch (Exception e) {

                System.out.println("[서버 통신 안됨]");
                stopClient();
                break;

            }

        }

    }


}
