package Part3;

import Part1.Player;
import Part3.waveaccess.WaveFileWriter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author duxiaoyuan
 */
public class Transmitter {
    private final List<Integer> input = Util.FileUtil.readInput("INPUT.txt");
    private float[] modulatedInput;
    private int frameNum;
    private float[] output;
    public float transmitTime;

    public Transmitter() {
        modulatedInput = Util.SoundUtil.modulate(input);
        frameNum = modulatedInput.length / Util.SoundUtil.dataLength;
        if (modulatedInput.length % Util.SoundUtil.dataLength != 0) {
            frameNum++;
        }
        // a frame contains 100 symbols, while it needs 100 * symbolLength = 4400
        output = new float[frameNum * Util.SoundUtil.frameLength];
        int index = 0;
        int m = 0;
        for (int i = 0; i < frameNum; i++) {
            Util.SoundUtil.addHeader(output, i * Util.SoundUtil.frameLength);
            index += Util.SoundUtil.headerLength;

            Util.SoundUtil.addData(output, modulatedInput, index, m);
            m += Util.SoundUtil.dataLength;
            index += Util.SoundUtil.dataLength;
        }
        transmitTime = (frameNum * Util.SoundUtil.symbolsPerFrame * Util.SoundUtil.symbolLength * 1000) / (Util.SoundUtil.sampleRate);
    }

    public void transmit() {
        int[][] data = new int[1][output.length];
        for (int i = 0; i < output.length; i++) {
            data[0][i] = (int) (output[i] * 10000);
        }
        WaveFileWriter waveFileWriter = new WaveFileWriter("src/Part1/p.wav", data, (long) Util.SoundUtil.sampleRate);
//        Player player = new Player((long) transmitTime, "p.wav");
//        player.playSound();
    }

    public float[] getOutput() {
        return output;
    }

    public static void main(String[] args) {
        Transmitter transmitter = new Transmitter();
        transmitter.transmit();
    }

}
