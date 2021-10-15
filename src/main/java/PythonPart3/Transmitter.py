import matplotlib.pyplot as plt
import numpy as np
import pyaudio
from scipy import signal, integrate
import time
import sounddevice as sd
from scipy.fft import fft


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
    # header_length = 480
    # sample_rate = 48000
    # F0 = 1875
    # dF = 46.875
    # header_index = [0, 3, 4, 7, 8, 11, 12, 15, 16, 19, 20, 23, 24, 27, 28, 31]
    # header = np.zeros(48000, dtype=np.float32)
    # for i in header_index:
    #     if i % 2 == 0:
    #         header += np.sin(2*np.pi*(F0 + i * dF) * np.arange(0, 1, 1 / sample_rate)).astype(np.float32)
    #     else:
    #         header += np.sin(2*np.pi*(F0 + i * dF) * np.arange(0, 1, 1 / sample_rate)).astype(np.float32)
    # corr = signal.correlate(header, header)
    # y = fft(header)
    # y = abs(y)
    # y = y[0:4000]
    # plt.plot(range(len(y)), y)
    # plt.show()
    t = np.linspace(0,1, 48000,endpoint = True,dtype = np.float32)
    t=t[0:480]
    f_p = np.concatenate([np.linspace(2500, 8000, 240), np.linspace(8000,2500, 240)])
    header = (np.sin(2*np.pi*integrate.cumtrapz(f_p, t))).astype(np.float32)
    corr = signal.correlate(header, header)
    max = np.max(abs(corr))
    avg = np.average(abs(corr))
    print(max / avg)
    return header


# start = time.time()
# transmit("INPUT.txt")
# end = time.time()
# print("Transmitting time: ", end - start)
# header = gen_header()
# sd.play(header, 48000)
# sd.wait()
carrier0 = np.sin(2 * np.pi * 2000 * np.arange(0, 0.001, 1 / 48000)).astype(np.float32)
carrier1 = -np.sin(2 * np.pi * 2000 * np.arange(0, 0.001, 1 / 48000)).astype(np.float32)
y = abs(fft(carrier0))
plt.plot(range(len(y)), y)
plt.show()