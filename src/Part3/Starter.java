package Part3;

import Part1.Player;
import Part1.Recorder;
import Part3.waveaccess.WaveAccess;
import Part3.waveaccess.WaveFileReader;
import Part3.waveaccess.WaveFileWriter;

import java.util.Arrays;

/**
 * @author duxiaoyuan
 */
public class Starter {
    public static void main(String[] args) {
        Transmitter transmitter = new Transmitter();
        float[] o = transmitter.getOutput();
//        transmitter.startTransmit();
//        Receiver receiver = new Receiver();
//        receiver.startReceive();
//        int[][] data = new int[1][o.length];
//        for(int i = 0; i < o.length; i++) {
//            data[0][i] = (int) (o[i] * (float) 0x7FFFFFFF);
//        }
//        System.out.println(Arrays.toString(data[0]));
//        WaveFileWriter waveFileWriter = new WaveFileWriter("src/Part3/p.wav", data, 44100);
        WaveFileReader waveFileReader = new WaveFileReader("src/Part3/p.wav");
        int[][] o1 = waveFileReader.getData();
        float[] data1 = new float[o1[0].length];
        for(int i = 0; i < o1[0].length; i++){
            data1[i] = (float) o1[0][i] / (float) 0x7FFFFFFF;
        }
        System.out.println(Arrays.toString(data1));

    }
}
