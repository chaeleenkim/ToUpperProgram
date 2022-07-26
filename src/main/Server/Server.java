package main.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.concurrent.Executors;

public class Server {

    AsynchronousChannelGroup channelGroup1;
    AsynchronousChannelGroup channelGroup2;

    AsynchronousServerSocketChannel workerServerSocketChannel;
    AsynchronousServerSocketChannel heartbeatCheckServerSocketChannel;
    List<Client> connections;

    void startServer() {
        makeServerSocketChannel();
        System.out.println("[서버 시작]");

        acceptConnection();


    }

    void stopServer() {

    }

    void makeServerSocketChannel() {
        try {
            //heartbeat 체크용
            channelGroup2 = AsynchronousChannelGroup.withFixedThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    Executors.defaultThreadFactory()
            );
            heartbeatCheckServerSocketChannel = AsynchronousServerSocketChannel.open(channelGroup2);
            heartbeatCheckServerSocketChannel.bind(new InetSocketAddress(5001));

            //toUpper 처리용
            channelGroup1 = AsynchronousChannelGroup.withFixedThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    Executors.defaultThreadFactory()
            );

            workerServerSocketChannel = AsynchronousServerSocketChannel.open(channelGroup1);
            workerServerSocketChannel.bind(new InetSocketAddress(5002));

        } catch (Exception e) {
            if (workerServerSocketChannel.isOpen() || heartbeatCheckServerSocketChannel.isOpen()) {
                stopServer();
            }
            return;
        }
    }

    void acceptConnection() {

        heartbeatCheckServerSocketChannel.accept(null,
                new CompletionHandler<AsynchronousSocketChannel, Void>() {

                    @Override
                    public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
                        try {
                            String message = "[Heartbeat 연결 수락: " + socketChannel.getRemoteAddress() + ": " +
                                    Thread.currentThread().getName() + "]";
                            System.out.println(message);
                        } catch (IOException e) {
                        }

                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        if (heartbeatCheckServerSocketChannel.isOpen()) {
                            stopServer();
                        }
                    }
                });

        workerServerSocketChannel.accept(null,
                new CompletionHandler<AsynchronousSocketChannel, Void>() {

                    @Override
                    public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
                        try {
                            String message = "[worker 연결 수락: " + socketChannel.getRemoteAddress() + ": " +
                                    Thread.currentThread().getName() + "]";
                            System.out.println(message);
                        } catch (IOException e) {
                        }

/*                      Client client = new Client(socketChannel);
                        connections.add(client);
                        System.out.println("[연결 개수: " + connections.size() + "]" );*/

                        workerServerSocketChannel.accept(null, this);
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        if (workerServerSocketChannel.isOpen()) {
                            stopServer();
                        }
                    }
                });
    }




    class Client {
        AsynchronousSocketChannel socketChannel;

        Client(AsynchronousSocketChannel socketChannel) {
            this.socketChannel = socketChannel;
            System.out.println("Client 생성");
        }
    }


}
