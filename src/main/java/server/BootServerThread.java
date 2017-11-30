package server;

public class BootServerThread {
    public static void main(String[] args) {
        Thread t = new Thread(new ServerThread());
        t.start();
    }
}
