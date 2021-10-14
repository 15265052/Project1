package Part1;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Player {
    private final String fileName;
    private final long playTime;
    private Clip clip;
    public Player(long playTime, String fileName) {
        this.playTime = playTime;
        this.fileName = fileName;
    }

    private void start() {
        System.out.println("Start playing....");
        try {
            clip = AudioSystem.getClip();
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("src/Part1/" + fileName));
            clip.open(audioInputStream);
            clip.start();
            Thread.sleep(playTime);
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void finish() {
        clip.stop();
        clip.close();
        System.out.println("Finish playing....");
    }

    public void playSound(){
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(playTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finish();
        });
        Thread timer = new Thread(() -> {
            long time = playTime;
            while(time > 0){
                System.out.println("Timer: Playing time left: " + time / 1000 + " s");
                time -= 1000;
                if(time == 0) {
                    System.out.println("Timer: Playing time left: 0 s");
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
