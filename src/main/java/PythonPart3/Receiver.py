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
    record_seconds = 15
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
    t = np.linspace(0, 1, 48000, endpoint=True, dtype=np.float32)
    t = t[0:480]
    f_p = np.concatenate([np.linspace(2500, 8000, 240), np.linspace(8000, 2500, 240)])
    header = (np.sin(2 * np.pi * integrate.cumtrapz(f_p, t))).astype(np.float32)
    return header


def detect_header(sync, pointer):
    ref_header = gen_header()
    corr = signal.correlate(sync, ref_header, mode='full', method='fft')
    max = np.max(abs(corr))
    argmax = np.argmax(abs(corr))

    if max > threshold:
        print("detected a header, max:", max, "arg: ", pointer + argmax - header_length)
        # plt.plot(range(len(corr)), corr)
        # plt.show()
        return argmax - header_length
    else:
        return 'error'


def decode(buffer):
    str1 = ''
    for i in range(symbol_per_frame):
        sample_buffer = buffer[i * samples_per_symbol:(i + 1) * samples_per_symbol]
        if len(sample_buffer) < samples_per_symbol:
            str = '0'
        else:
            str = decode_one_bit(sample_buffer)
        str1 = str1 + str
    write_file(str1)


def write_file(str):
    with open("OUTPUT.txt", 'a') as f:
        f.write(str)


def decode_one_bit(s_buffer):
    sum = np.sum(s_buffer * carrier0)
    if sum > 0:
        return '0'
    else:
        return '1'


file_name = "test125.wav"
#receive(file_name)

is_receiving = False
header = gen_header()
header_length = 480
carrier_freq = 2000
sample_rate = 48000
header_count = 0
# skip the time when micro is heating

carrier0 = np.sin(2 * np.pi * carrier_freq * np.arange(0, 0.001, 1 / sample_rate)).astype(np.float32)
carrier1 = -(np.sin(2 * np.pi * carrier_freq * np.arange(0, 0.001, 1 / sample_rate))).astype(np.float32)

frame_num = 80
detected_frame = 0
symbol_per_frame = 125
samples_per_symbol = 48
with sf.SoundFile(file_name) as sf_dest:
    data = sf_dest.read(dtype=np.float32)
corr1 = signal.correlate(data, header)
plt.plot(range(len(corr1)), corr1)
plt.show()
max = np.max(signal.correlate(data, header))
threshold = 1
data_length = len(data)
sync = []
pointer = 0
while detected_frame < frame_num and pointer < data_length:
    if not is_receiving:
        # fill in sync FIFO
        sync = data[pointer:pointer + header_length + symbol_per_frame * samples_per_symbol]
        pointer_header = detect_header(sync, pointer)
        if pointer_header != 'error':
            # detect a header
            header_count += 1
            is_receiving = True
            pointer += pointer_header
            continue
        else:
            pointer += 480
    else:
        # start decode bits
        frame_buffer = data[pointer + header_length: pointer + header_length + symbol_per_frame * samples_per_symbol]
        detected_frame += 1
        decode(frame_buffer)
        pointer += header_length + symbol_per_frame * samples_per_symbol
        is_receiving = False
if detected_frame < frame_num:
    complement_str = '0'*symbol_per_frame
    write_file(complement_str)

print(header_count,detected_frame)
# n = len(data) / 480
# for i in range(int(n)):
#     # if i == 300:
#     #     pass
#     detect_header(data[i*480:(i+1)*480], i)
#
# plt.plot(range(len(corr)),corr)
# plt.show()
