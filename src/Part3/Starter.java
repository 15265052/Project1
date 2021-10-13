package Part3;

/**
 * @author duxiaoyuan
 */
public class Starter {
    public static void main(String[] args) {
        Receiver receiver = new Receiver();
        receiver.receive();
        receiver.decode();
    }
}
