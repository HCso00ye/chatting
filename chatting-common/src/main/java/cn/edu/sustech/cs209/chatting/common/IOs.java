package cn.edu.sustech.cs209.chatting.common;

import java.io.*;

public class IOs {
  public static class MyObjectOutputStream extends ObjectOutputStream {

    public MyObjectOutputStream(OutputStream out) throws IOException {
      super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
      super.reset();
    }
  }

  public static class MyObjectInputStream extends ObjectInputStream {
    public MyObjectInputStream(InputStream in) throws IOException {
      super(in);
    }
  }
}
