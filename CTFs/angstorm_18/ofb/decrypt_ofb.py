import struct

def lcg(m, a, c, x):
	return (a*x + c) % m

m = pow(2, 32)

d = open('flag.png.enc').read()
d += '\x00' * (-len(d) % 4)
d = [d[i:i+4] for i in range(0, len(d), 4)]

e = ''
x = 0x91ca2302
a = 3204287424
c = 1460809397
for i in range(len(d)):
	e += struct.pack('>I', x ^ struct.unpack('>I', d[i])[0])
	x = lcg(m, a, c, x)

with open('flag.png', 'w') as f:
	f.write(e)
	f.close()
