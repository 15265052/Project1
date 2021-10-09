package Part3;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;

import java.util.*;

/**
 * @author duxiaoyuan
 */
public class Receiver implements AsioDriverListener {
    private static final AsioDriver asioDriver = AsioDriver.getDriver(AsioDriver.getDriverNames().get(0));
    private final AsioDriverListener host = this;
    private Set<AsioChannel> asioChannels = new HashSet<>();
    private int bufferSize;
    private float[] inputBuffer;
    private int frameNum;
    private float transmitTime;
    private float[] allInputs;
    private int inputIndex = 0;
    public Receiver() {
        frameNum = Util.SoundUtil.inputSize * Util.SoundUtil.bitLength / Util.SoundUtil.bitsPerFrame;
        if(((Util.SoundUtil.inputSize * Util.SoundUtil.bitLength) % Util.SoundUtil.bitsPerFrame) != 0) {
            frameNum++;
        }
        allInputs = new float[frameNum * Util.SoundUtil.frameLength];
        transmitTime = (frameNum * Util.SoundUtil.bitsPerFrame * Util.SoundUtil.bitLength  * 1000 ) / (Util.SoundUtil.sampleRate);
    }

    public void receive() {
        asioChannels.add(asioDriver.getChannelInput(0));
        asioDriver.addAsioDriverListener(host);
        bufferSize = asioDriver.getBufferPreferredSize();
        inputBuffer = new float[bufferSize];
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
        for(AsioChannel asioChannel : activeChannels){
            asioChannel.read(inputBuffer);
        }
        Util.SoundUtil.setArray(allInputs, inputBuffer, inputIndex);
        inputIndex += bufferSize;
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
        receive();

    }

    public void decode() {
        Queue<Float> syncFIFO = new ArrayDeque<>();
        // initialize syncFIFO
        for(int i = 0; i < Util.SoundUtil.headerLength; i++){
            syncFIFO.add(allInputs[i]);
        }
         for(int i = Util.SoundUtil.; i < allInputs.length; i++){

         }
    }
}
