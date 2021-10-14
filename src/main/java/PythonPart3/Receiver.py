import pyaudio
import soundfile as sf
import sounddevice as sd
import wave
import numpy as np
from scipy import signal, integrate


def receive(file_name):
    sample_rate = 48000
    channels = 1
    record_seconds = 12
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


file_name = "test.wav"
receive(file_name)

is_receiving = False
header = gen_header()

wav = wave.open(file_name, 'rb')
# skip the time when micro is heating
wav.setpos(48000)

frame_num = 20


