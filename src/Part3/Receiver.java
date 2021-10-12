package Part3;

import Part3.waveaccess.WaveFileReader;
import com.synthbot.jasiohost.AsioChannel;

import java.util.*;

/**
 * @author duxiaoyuan
 */
public class Receiver {
    private final float transmitTime;
    private List<Integer> outputList = new ArrayList<>();
    private int frameNum;
    private float[] allInputs;
    private int inputIndex = 0;
    private int fileOutIndex = 0;

    public Receiver() {
        frameNum = Util.SoundUtil.inputSize * Util.SoundUtil.symbolLength / Util.SoundUtil.dataLength;
        if (((Util.SoundUtil.inputSize * Util.SoundUtil.symbolLength) % Util.SoundUtil.dataLength) != 0) {
            frameNum++;
        }
        transmitTime = (frameNum * Util.SoundUtil.symbolsPerFrame * Util.SoundUtil.symbolLength * 1000) / (Util.SoundUtil.sampleRate);
    }

    public void receive() {
        WaveFileReader waveFileReader = new WaveFileReader("src/Part1/p.wav");
        int[][] data = waveFileReader.getData();
        allInputs = new float[data[0].length];
        for(int i = 0; i < data[0].length;i++){
            allInputs[i] = (float) data[0][i] / 10000;
        }
//        Transmitter transmitter = new Transmitter();
//        allInputs = transmitter.getOutput();
    }


    public void decode() {
        float max = 0;
        float power = 0;
        int count = 0;
        Queue<Float> syncFIFO = new ArrayDeque<>(Util.SoundUtil.headerLength);
        Queue<Float> frameBuffer = new ArrayDeque<>(Util.SoundUtil.frameLength);

        // initialize syncFIFO
        for (int i = 0; i < Util.SoundUtil.frameLength; i++) {
            if (i < Util.SoundUtil.headerLength) {
                syncFIFO.add(allInputs[i]);
            }
            frameBuffer.add(allInputs[i]);
        }
        max = Util.MathUtil.correlation(syncFIFO, Util.SoundUtil.getHeader());
        if (Util.MathUtil.correlation(syncFIFO, Util.SoundUtil.getHeader()) > Util.SoundUtil.threshold / 5e5) {
            // detect a frame
            decodeFrame(new ArrayDeque<>(frameBuffer));
        }

        float currentSample;
        for (int i = Util.SoundUtil.frameLength; i < allInputs.length; i++) {
            currentSample = allInputs[i];
            int headerIndex = i - (Util.SoundUtil.frameLength - Util.SoundUtil.headerLength);
            syncFIFO.remove();
            syncFIFO.add(allInputs[headerIndex]);

            frameBuffer.remove();
            frameBuffer.add(currentSample);
            if (Util.MathUtil.correlation(syncFIFO, Util.SoundUtil.getHeader()) > max) {
                max = Util.MathUtil.correlation(syncFIFO, Util.SoundUtil.getHeader());
            }
            if (Util.MathUtil.correlation(syncFIFO, Util.SoundUtil.getHeader()) >= 6e-6) {
                // detect a frame
                count++;
                decodeFrame(new ArrayDeque<>(frameBuffer));
            }
        }
        System.out.println(max);
        System.out.println(count);
    }

    private void decodeFrame(Queue<Float> frame) {
        //decode 100 bits of a frame
        for (int i = 0; i < Util.SoundUtil.headerLength; i++) {
            frame.remove();
        }
        float[] bitBuffer = new float[Util.SoundUtil.symbolLength];
        for (int i = 0; i < Util.SoundUtil.symbolsPerFrame; i++, fileOutIndex++) {
            if (fileOutIndex >= Util.SoundUtil.inputSize) {
                break;
            }
            for (int j = 0; j < Util.SoundUtil.symbolLength; j++) {
                //fill in bit buffer
                bitBuffer[j] = frame.remove();
            }
            outputList.add(decodeOneSymbol(bitBuffer));
        }
    }

    private int decodeOneSymbol(float[] symbol) {
        return Util.MathUtil.correlation(symbol, Util.SoundUtil.one) > 0 ? 1 : 0;
    }


}
