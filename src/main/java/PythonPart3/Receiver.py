import struct

import pyaudio
import soundfile as sf
import sounddevice as sd
import wave
import numpy as np
from scipy import signal, integrate
import matplotlib.pyplot as plt
from scipy.fft import fft


def receive(file_name):
    sample_rate = 48000
    channels = 1
    record_seconds = 2
    print("start recording")
    frame = sd.rec(int(sample_rate * record_seconds),
                   samplerate=sample_rate,
                   channels=channels,
                   blocking=True,
                   dtype="float32")
    sf.write(file_name, frame, sample_rate)
    print("finish recording")


def gen_header():
    ''' header for 0.01 second'''
    header_length = 480
    t = np.linspace(0, 1, 48000, endpoint=True, dtype=np.float32)
    t = t[0:header_length]
    f_var = np.concatenate([np.linspace(8000, 12000, 240), np.linspace(12000, 8000, 240)])
    header = np.sin(2 * np.pi * integrate.cumtrapz(f_var, t))
    return header

def detect_header(sync):
    ref_header = gen_header()
    corr = signal.correlate(sync, ref_header)
    plt.plot(range(len(corr)), corr)
    plt.show()


file_name = "test.wav"
receive(file_name)

is_receiving = False
header = gen_header()
header_length = 480
# skip the time when micro is heating


frame_num = 20
detected_frame = 0

with sf.SoundFile(file_name) as sf_dest:
    data = sf_dest.read(dtype=np.float32)
# while detected_frame < frame_num and pointer_now < wav.getnframes():
#     pointer_now = wav.tell()
#     if not is_receiving:
#     data = data[300000:300480]
#     detect_header(data)
plt.plot(range(len(data)), data)
plt.show()
y = fft(data)
y = abs(y)
y = y[:10000]
print(np.argmax(y))
plt.plot(range(len(y)), y)
plt.show()

