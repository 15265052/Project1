package Part3;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;

import java.util.*;

/**
 * @author duxiaoyuan
 */
public class Receiver implements AsioDriverListener {
    private List<Integer> outputList = new ArrayList<>();
    private static final AsioDriver asioDriver = AsioDriver.getDriver(AsioDriver.getDriverNames().get(0));
    private final AsioDriverListener host = this;
    private Set<AsioChannel> asioChannels = new HashSet<>();
    private int bufferSize;
    private float[] inputBuffer;
    private int frameNum;
    private final float transmitTime;
    private float[] allInputs;
    private int inputIndex = 0;
    private int fileOutIndex = 0;
    public Receiver() {
        frameNum = Util.SoundUtil.inputSize * Util.SoundUtil.symbolLength / Util.SoundUtil.dataLength;
        if(((Util.SoundUtil.inputSize * Util.SoundUtil.symbolLength) % Util.SoundUtil.dataLength) != 0) {
            frameNum++;
        }
        allInputs = new float[frameNum * Util.SoundUtil.frameLength];
        transmitTime = (frameNum * Util.SoundUtil.symbolsPerFrame * Util.SoundUtil.symbolLength * 1000 ) / (Util.SoundUtil.sampleRate);
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

    public void startReceive() {
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep((long) transmitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            asioDriver.shutdownAndUnloadDriver();
            System.out.println(Arrays.toString(allInputs));
            decode();
            Util.FileUtil.writeOutput("OUTPUT.txt", outputList);
            System.out.println("Finished....");
        });

        stopper.start();
        receive();
    }

    public void decode() {
        float max = 0;
        float power = 0;
        int count = 0;
        Queue<Float> syncFIFO = new ArrayDeque<>(Util.SoundUtil.headerLength);
        Queue<Float> frameBuffer = new ArrayDeque<>(Util.SoundUtil.frameLength);

        // initialize syncFIFO
        for(int i = 0; i < Util.SoundUtil.frameLength; i++){
            if(i < Util.SoundUtil.headerLength){
                syncFIFO.add(allInputs[i]);
            }
            frameBuffer.add(allInputs[i]);
        }
        max = Util.MathUtil.correlation(syncFIFO, Util.SoundUtil.getHeader());
        if(Util.MathUtil.correlation(syncFIFO, Util.SoundUtil.getHeader()) > Util.SoundUtil.threshold / 5e8){
            // detect a frame
            decodeFrame(new ArrayDeque<>(frameBuffer));
        }

        float currentSample;
         for(int i = Util.SoundUtil.frameLength; i < allInputs.length; i++){
            currentSample = allInputs[i];
             int headerIndex = i - (Util.SoundUtil.frameLength - Util.SoundUtil.headerLength);
             syncFIFO.remove();
             syncFIFO.add(allInputs[headerIndex]);

             frameBuffer.remove();
             frameBuffer.add(currentSample);
             if (Util.MathUtil.correlation(syncFIFO, Util.SoundUtil.getHeader()) > max) {
                 max = Util.MathUtil.correlation(syncFIFO, Util.SoundUtil.getHeader());
             }
             if(Util.MathUtil.correlation(syncFIFO, Util.SoundUtil.getHeader()) > 0.5){
                 count++;
             }
            if(Util.MathUtil.correlation(syncFIFO, Util.SoundUtil.getHeader()) >= Util.SoundUtil.threshold / 5e8){
                // detect a frame
                decodeFrame(new ArrayDeque<>(frameBuffer));
            }
         }
         System.out.println(max);
         System.out.println(count);
    }

    private void decodeFrame(Queue<Float> frame){
        //decode 100 bits of a frame
        for(int i = 0; i < Util.SoundUtil.headerLength; i++){
            frame.remove();
        }
        float[] bitBuffer = new float[Util.SoundUtil.symbolLength];
        for(int i = 0; i < Util.SoundUtil.symbolsPerFrame; i++, fileOutIndex++) {
            if (fileOutIndex >= Util.SoundUtil.inputSize){
                break;
            }
            for (int j = 0; j < Util.SoundUtil.symbolLength ; j++) {
                //fill in bit buffer
                bitBuffer[j] = frame.remove();
            }
            outputList.add(decodeOneSymbol(bitBuffer));
        }
    }

    private int decodeOneSymbol(float[] symbol){
        return Util.MathUtil.correlation(symbol, Util.SoundUtil.one) > 0 ? 1 : 0;
    }


}
