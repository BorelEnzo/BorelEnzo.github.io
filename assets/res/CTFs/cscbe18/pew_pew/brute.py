def brute(i):
	edx = 0x51eb851f
	edx = ((i * edx) & 0xffff00000000) >> 36
	eax = i >> 31
	eax = edx-eax
	eax = (eax * 0x32) & 0xffffffff
	eax = i - eax
	return eax == 0x23

for i in range(0x167a, 0x16a7+1, 1):
	if brute(i):
		print i
		exit(0)
