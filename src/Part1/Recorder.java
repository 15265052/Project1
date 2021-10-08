package Part1;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Recorder {
    public static final AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    public static final AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);


    private final long recordTime;
    private final String fileName;
    private TargetDataLine line;
    private AudioInputStream audioInputStream;
    public Recorder(long recordTime, String fileName){
        this.recordTime = recordTime;
        this.fileName = fileName;
    }

    private void start() {
        System.out.println("Start recording....");

        File wavFile = new File("src/Part1/" + fileName);
        if(!wavFile.exists()){
            try {
                wavFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();

            audioInputStream = new AudioInputStream(line);
            AudioSystem.write(audioInputStream, fileType, wavFile);

        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    private void finish() {
        line.stop();
        line.close();
        try {
            audioInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finish recording....");
    }

    public void recordSound() {
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(recordTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finish();
        });
        Thread timer = new Thread(() -> {
            long time = recordTime;
            while(time > 0){
                System.out.println("Timer: Recording time left: " + time / 1000 + " s");
                time -= 1000;
                if(time == 0) {
                    System.out.println("Timer: Recording time left: 0 s");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        stopper.start();
        timer.start();
        start();
    }
}
