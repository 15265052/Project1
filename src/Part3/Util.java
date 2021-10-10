package Part3;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Util {
    public static class FileUtil {
        public static List<Integer> readInput(String fileName) {
            File inputFile = new File("src/Part3/" + fileName);
            try {
                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(inputFile));
                List<Integer> ret = new ArrayList<>();
                while (dataInputStream.available() > 0) {
                    ret.add(Integer.parseInt(String.valueOf((char) dataInputStream.readByte())));
                }
                dataInputStream.close();
                return ret;
            } catch (FileNotFoundException e) {
                System.out.println("File Not Found!");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static void writeOutput(String fileName, List<Integer> output) {
            File outputFile = new File("src/Part3/" + fileName);
            try {
                OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
                for (Integer out : output) {
                    outputStream.write(out.toString());
                }
                outputStream.close();
            } catch (FileNotFoundException e) {
                System.out.println("File Not Found!");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class MathUtil {
        public static float correlation(Collection<Float> x, float[] y) {
            float ret = 0;
            int i = 0;
            for(Float e : x) {
                ret += e * y[i];
                i++;
                if(i >= y.length) {
                    break;
                }
            }
            return ret;
        }

        public static float correlation(float[] x, float[] y){
            float ret = 0;
            for(int i = 0; i < y.length; i++){
                ret += x[i] * y[i];
            }
            return ret;
        }
    }

    public static class SoundUtil {
        public static final int inputSize = 13;
        public static final float sampleRate = 44100;
        public static final float bitRate = 1000;
        public static final float carrFreq = 2000;
        public static final int symbolLength = (int) (sampleRate / bitRate);
        public static final int threshold = 30;
        public static float[] one = new float[symbolLength];
        public static float[] zero = new float[symbolLength];
        public final static int headerLength = 256;
        private final static float[] header = generateHeader();
        public static final int symbolsPerFrame = 100;
        public static final int frameLength = symbolLength * symbolsPerFrame + headerLength;// each frame contains 100 bits and a header

        public static final int dataLength = frameLength - headerLength;
        static {
            for (int i = 0; i < one.length; i++) {
                one[i] = (float) Math.sin(2 * Math.PI * i * carrFreq / sampleRate);
                zero[i] = -(float) Math.sin(2 * Math.PI * i * carrFreq / sampleRate);
            }
        }
        public static final float bitThreshold = MathUtil.correlation(header, one);
        public static float[] getHeader() {
            return header;
        }

        public static void addHeader(float[] array, int from) {
            setArray(array, header, from);
        }

        public static void addData(float[] output, float[] data, int outFrom, int inFrom) {
            for (int i = inFrom; i < inFrom + symbolsPerFrame * symbolLength; i++) {
                if (i < data.length) {
                    output[outFrom] = data[i];
                } else {
                    output[outFrom] = 0;
                }
                outFrom++;
                if(outFrom >= output.length){
                    break;
                }
            }
        }

        private static float[] generateHeader() {
            final float scale = 0.5f; //the amplitude of header segment
            final float minFrequency = 2000;
            final float maxFrequency = 20000;

            float phase = 0;
            float frequency = minFrequency;
            float frequencyStep = (maxFrequency - minFrequency) / 32;
            float timeGap = 1 / sampleRate;

            float[] header = new float[headerLength];

            for (int i = 0; i < headerLength; i++) {
                header[i] = (float) Math.sin(phase) * scale;

                if (i <= 32) {
                    frequency += frequencyStep;
                } else {
                    frequency -= frequencyStep;
                }

                phase += 2 * Math.PI * frequency * timeGap;
            }

            return header;
        }

        public static float[] modulate(List<Integer> input) {
            float[] modulatedInput = new float[input.size() * symbolLength];
            for (int i = 0, m = 0; i < input.size(); i++, m += symbolLength) {
                if (input.get(i) == 0) {
                    setArray(modulatedInput, zero, m);
                } else if (input.get(i) == 1) {
                    setArray(modulatedInput, one, m);
                }
            }

            return modulatedInput;
        }

        /**
         * set x's content from 'from' to y.length
         *
         * @param x    the destination array
         * @param y    the source array
         * @param from the start index of setting
         */
        public static void setArray(float[] x, float[] y, int from) {
            System.arraycopy(y, 0, x, from, y.length);
        }

        public static void main(String[] args) {
            System.out.println(MathUtil.correlation(one, zero));
        }
    }
}
