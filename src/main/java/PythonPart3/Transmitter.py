import matplotlib.pyplot as plt
import numpy as np
import pyaudio
from scipy import signal, integrate
import time
import sounddevice as sd
from scipy.fft import fft


def transmit(file_name):
    channels = 1
    sample_rate = 48000
    carrier_freq = 2000
    carrier0 = np.sin(2 * np.pi * carrier_freq * np.arange(0, 0.001, 1 / sample_rate)).astype(np.float32)
    carrier1 = -(np.sin(2 * np.pi * carrier_freq * np.arange(0, 0.001, 1 / sample_rate))).astype(np.float32)
    with open(file_name, 'r') as file:
        file_data = file.read()
    input_index = 0
    symbols_in_frame = 125
    frame_num = int(len(file_data) / symbols_in_frame)
    if frame_num * 125 < len(file_data):
        frame_num += 1
    header = gen_header()
    data = []
    for i in range(frame_num):
        data.append(header)
        for j in range(symbols_in_frame):
            if file_data[input_index] == '0':
                data.append(carrier0)
            else:
                data.append(carrier1)
            input_index += 1
    data = np.concatenate(data)
    sd.default.channels = 1
    sd.play(data, samplerate=sample_rate)
    sd.wait()

def gen_header():
    ''' header for 0.01 second'''
    t = np.linspace(0, 1, 48000, endpoint=True, dtype=np.float32)
    t = t[0:480]
    f_p = np.concatenate([np.linspace(2500, 8000, 240), np.linspace(8000, 2500, 240)])
    header = (np.sin(2 * np.pi * integrate.cumtrapz(f_p, t))).astype(np.float32)
    return header


start = time.time()
transmit("INPUT.txt")
end = time.time()
print("Transmitting time: ", end - start)
# header = gen_header()
# sd.play(header, 48000)
# sd.wait()
# sd.sleep(1000)
# sd.play(header, 48000)
# sd.wait()