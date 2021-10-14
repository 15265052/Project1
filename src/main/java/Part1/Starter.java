package Part1;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class Starter {
    public static class Task1 {
        public static void main(String[] args) {
            final long recordTime = 12000;
            final String fileName = "part1_task1.wav";

            Recorder recorder = new Recorder(recordTime, fileName);
            recorder.recordSound();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Player player = new Player(recordTime, fileName);
            player.playSound();
        }
    }

    public static class Task2 {
        public static void main(String[] args) {
            final long playTime = 10000;
            final String fileName = "part1_task1.wav";
            final String fileNameRecord = "part1_task2_record.wav";

            Recorder recorder = new Recorder(playTime, fileNameRecord);
            Player player = new Player(playTime, fileName);

            new Thread(player::playSound).start();
            recorder.recordSound();

            Player player2 = new Player(playTime, fileNameRecord);
            new Thread(player2::playSound).start();
        }
    }
}
