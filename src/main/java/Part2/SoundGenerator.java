package Part2;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;

import java.util.HashSet;
import java.util.Set;

public class SoundGenerator implements AsioDriverListener {
    private final static AsioDriver asioDriver = AsioDriver.getDriver(AsioDriver.getDriverNames().get(0));
    private Set<AsioChannel> asioChannels = new HashSet<>();
    private AsioDriverListener host = this;
    private double sampleRate = asioDriver.getSampleRate();
    private int bufferSize = asioDriver.getBufferPreferredSize();
    private int sampleIndex = 0;
    private float[] output = new float[bufferSize];

    public SoundGenerator(){
        asioChannels.add(asioDriver.getChannelOutput(0));
        asioChannels.add(asioDriver.getChannelOutput(1));

        asioDriver.addAsioDriverListener(host);
        asioDriver.createBuffers(asioChannels);
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            asioDriver.shutdownAndUnloadDriver();
            System.out.println("Finished....");
        });
        stopper.start();
        asioDriver.start();
        asioDriver.openControlPanel();
        System.out.println("Playing...");
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
        for(int i = 0; i < bufferSize; i++, sampleIndex++){
            output[i] = 0.5f * ((float) Math.sin(2 * Math.PI * 1000 * (sampleIndex / sampleRate)) + (float) Math.sin(2 * Math.PI * 10000 * (sampleIndex / sampleRate)));
        }

        for(AsioChannel asioChannel : activeChannels){
            asioChannel.write(output);
        }
    }
}
