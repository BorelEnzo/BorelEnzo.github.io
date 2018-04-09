import itertools
import sys

cmd = sys.argv[1]
for string in itertools.product('ACTG', repeat=4):
	x = ''.join(y for y in string)
	res = 0x0
	for i in range(len(x)):
		y = ord(x[i])
		if y != 0x41:
			z = 2*(3-i)
			if y == 0x43:
				y = 1 << z
			elif y == 0x47:
				y = 2 << z
			elif y == 0x54:
				y = 3 << z
			res = (y|res) & 0xff
	if chr(res) in sys.argv[1]:
		cmd = cmd.replace(chr(res),x)
print 'ACTG' * 128 + cmd
