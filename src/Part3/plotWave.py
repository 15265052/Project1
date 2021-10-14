import matplotlib.pyplot as plt

fin = open("waves.txt", "r")
data = fin.read()
waves = data.split(',')[200000:202000]
for i in range(len(waves)):
    waves[i] = float(waves[i])
plt.plot(waves)
plt.show()
