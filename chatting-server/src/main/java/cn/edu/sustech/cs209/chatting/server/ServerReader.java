package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.*;


import java.io.*;
import java.net.Socket;
import java.util.*;


class ServerReader implements Runnable {

  private Socket socket;

  public ServerReader(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try {
      IOs.MyObjectInputStream ois = new IOs.MyObjectInputStream(socket.getInputStream());
      IOs.MyObjectOutputStream os;
      while (true) {
        Message message = (Message) ois.readObject();
        switch (message.getType()) {
          case COMMAND:
            if (!Users.user_socket_map.containsKey(message.getSentBy())) {
              Users.user_socket_map.put(message.getSentBy(), socket);
              System.out.println("发送新的userlist");
              String userString = getUsers();
              for (Map.Entry<String, Socket> entry : Users.user_socket_map.entrySet()) {
                os = new IOs.MyObjectOutputStream(entry.getValue().getOutputStream());
                os.writeObject(
                        new Message(System.currentTimeMillis(), "Server", entry.getKey(), userString, MsgType.REQ)
                );
                os.flush();
              }
            } else {
              os = new IOs.MyObjectOutputStream(socket.getOutputStream());
              os.writeObject(new Message(System.currentTimeMillis(), "Server", message.getSentBy(), "not allow login", MsgType.NOT_ALLOW_LOGIN));
              os.flush();
            }
            break;

          case EXIT:
            Users.user_socket_map.remove(message.getSentBy());
            for (Map.Entry<String, Socket> entry : Users.user_socket_map.entrySet()) {
              os = new IOs.MyObjectOutputStream(entry.getValue().getOutputStream());
              os.writeObject(new Message(System.currentTimeMillis(), message.getSentBy(), entry.getKey(), Integer.toString(Users.user_socket_map.size()), MsgType.EXIT));
              os.flush();
            }
            break;

          case EXIT_NO_KEEP:
            String userString = getUsers();
            for (Map.Entry<String, Socket> entry : Users.user_socket_map.entrySet()) {
              os = new IOs.MyObjectOutputStream(entry.getValue().getOutputStream());
              os.writeObject(new Message(System.currentTimeMillis(), message.getSentBy(), entry.getKey(), userString, MsgType.EXIT_NO_KEEP));
              os.flush();
            }
            break;

          case TALK:
            if (Users.one_to_one.get(message.getSendTo()).equals(message.getSentBy())) {
              Users.user_user_messages.get(message.getSentBy() + "`" + message.getSendTo()).add(message);
              Users.user_user_messages.get(message.getSendTo() + "`" + message.getSentBy()).add(message);

              os = new IOs.MyObjectOutputStream(Users.user_socket_map.get(message.getSendTo()).getOutputStream());
              os.writeObject(message);
              os.flush();
            } else {
              Users.user_user_messages.get(message.getSentBy() + "`" + message.getSendTo()).add(message);
              Users.user_user_messages.get(message.getSendTo() + "`" + message.getSentBy()).add(message);

            }
            break;

          case TALKINGTO:
            Users.one_to_one.put(message.getSentBy(), message.getSendTo());
            String str = message.getSentBy() + "`" + message.getSendTo();

            if (!Users.user_user_messages.containsKey(str)) {
              System.out.println("str input = " + str);
              Users.user_user_messages.put(str, new ArrayList<>());
              Users.user_user_messages.put(message.getSendTo() + "`" + message.getSentBy(), new ArrayList<>());
            } else {

              for (int i = 0; i < Users.user_user_messages.get(str).size(); i++) {
                os = new IOs.MyObjectOutputStream(Users.user_socket_map.get(message.getSentBy()).getOutputStream());
                os.writeObject(Users.user_user_messages.get(str).get(i));
                System.out.println("返回聊天记录给" + message.getSentBy());
                os.flush();
              }
            }
            break;

          case SERVER_EXIT:
            System.out.println("Confirm server exit");
            System.exit(0);
            break;

          default:
            break;
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }

  }

  public String getUsers() {
    StringBuilder stringBuilder = new StringBuilder();
    for (Map.Entry<String, Socket> entry : Users.user_socket_map.entrySet()) {
      stringBuilder.append(entry.getKey());
      stringBuilder.append("~");
    }
    return stringBuilder.append(Users.user_socket_map.size()).toString();
  }


}

