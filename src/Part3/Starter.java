package Part3;

import Part1.Player;
import Part1.Recorder;

/**
 * @author duxiaoyuan
 */
public class Starter {
    public static void main(String[] args) {
//        Transmitter transmitter = new Transmitter();
//        transmitter.startTransmit();
//
//        Player player = new Player((long) transmitter.transmitTime, "Part3.wav");
//        player.playSound();
        Receiver receiver = new Receiver();
        receiver.startReceive();
    }
}
