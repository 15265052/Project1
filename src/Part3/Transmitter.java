package Part3;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author duxiaoyuan
 */
public class Transmitter implements AsioDriverListener {
    private static final AsioDriver asioDriver = AsioDriver.getDriver(AsioDriver.getDriverNames().get(0));
    private final List<Integer> input = Util.FileUtil.readInput("INPUT.txt");
    private final AsioDriverListener host = this;
    private Set<AsioChannel> asioChannels = new HashSet<>();
    private int bufferSize;
    private int inputSampleIndex = 0;
    private float[] modulatedInput;
    private int frameNum;
    private float[] output;
    private float[] asioOutput;
    private float transmitTime;
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
        transmitTime = (frameNum * Util.SoundUtil.symbolsPerFrame * Util.SoundUtil.symbolLength * 1000 ) / (Util.SoundUtil.sampleRate);
    }

    public void transmit() {
        asioChannels.add(asioDriver.getChannelOutput(0));
        asioDriver.addAsioDriverListener(host);
        bufferSize = asioDriver.getBufferPreferredSize();
        asioOutput = new float[bufferSize];
        asioDriver.createBuffers(asioChannels);
        asioDriver.start();
    }

    @Override
    public void sampleRateDidChange(double sampleRate) {

    }

    @Override
    public void resetRequest() {

    }

    @Override
    public void resyncRequest() {

    }

    @Override
    public void bufferSizeChanged(int bufferSize) {

    }

    @Override
    public void latenciesChanged(int inputLatency, int outputLatency) {

    }

    @Override
    public void bufferSwitch(long sampleTime, long samplePosition, Set<AsioChannel> activeChannels) {
        for (int i = 0; i < bufferSize; i++, inputSampleIndex++) {
            asioOutput[i] = output[inputSampleIndex];
        }

        for (AsioChannel asioChannel : activeChannels) {
            asioChannel.write(asioOutput);
        }
    }

    public void startTransmit() {
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep((long) transmitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            asioDriver.shutdownAndUnloadDriver();
            System.out.println("Finished....");
        });
        stopper.start();
        transmit();
    }
}
