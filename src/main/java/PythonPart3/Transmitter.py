import numpy as np
import pyaudio
from scipy import signal, integrate
import time


def transmit(file_name):
    channels = 1
    format = pyaudio.paFloat32
    sample_rate = 48000
    P = pyaudio.PyAudio()
    carrier_freq = 2000
    carrier0 = np.sin(2 * np.pi * carrier_freq * np.arange(0, 0.001, 1 / sample_rate)).astype(np.float32)
    carrier1 = -(np.sin(2 * np.pi * carrier_freq * np.arange(0, 0.001, 1 / sample_rate))).astype(np.float32)
    with open(file_name, 'r') as file:
        file_data = file.read()
    stream = P.open(format=format,
                    channels=channels,
                    rate=sample_rate,
                    output=True)
    input_index = 0
    symbols_in_frame = 500
    frame_num = int(len(file_data) / symbols_in_frame)
    if frame_num * 1000 < len(file_data):
        frame_num += 1
    header = gen_header()
    for i in range(frame_num):
        stream.write(header.tobytes())
        for j in range(symbols_in_frame):
            if (input_index < len(file_data)):
                input = file_data[input_index]
            else:
                input = '0'
            if input == '0':
                stream.write(carrier0.tobytes())
            else:
                stream.write(carrier1.tobytes())
            input_index += 1


def gen_header():
    ''' header for 0.01 second'''
    header_length = 480
    t = np.linspace(0, 1, 48000, endpoint=True, dtype=np.float32)
    t = t[0:header_length]
    f_var = np.concatenate([np.linspace(8000, 12000, 240), np.linspace(12000, 8000, 240)])
    header = np.sin(2 * np.pi * integrate.cumtrapz(f_var, t))
    return header


start = time.time()
transmit("INPUT.txt")
end = time.time()
print("Transmitting time: ", end-start)