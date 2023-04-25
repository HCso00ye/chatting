package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MsgType;
import cn.edu.sustech.cs209.chatting.common.IOs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

  public static Set<IOs.MyObjectOutputStream> oosSet = new HashSet<>();

  public static void main(String[] args) {
    System.out.println("Starting server");
    Thread exitThread = new Thread(() -> {
      Scanner inputExit = new Scanner(System.in);
      while (inputExit.hasNext()) {
        String i = inputExit.next();
        if (i.equals("exit")) {
          System.out.println("server exit");
          for (IOs.MyObjectOutputStream o : oosSet) {
            try {
              o.writeObject(new Message(System.currentTimeMillis(), "Server", "Server", "server exit", MsgType.SERVER_EXIT));
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    });
    exitThread.start();

    try {
      ServerSocket serverSocket = new ServerSocket(6666);
      ExecutorService executorService = Executors.newFixedThreadPool(20);
      for (int i = 1; i < 100; i++) {
        Socket socket = serverSocket.accept();
        oosSet.add(new IOs.MyObjectOutputStream(socket.getOutputStream()));
        executorService.execute(new ServerReader(socket));
      }
      executorService.shutdown();
      serverSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
