def sumchars(start, string, lim, inc):
	i = start
	res = 0
	while i < lim:
		res += ord(string[i])
		i+=inc
	return res

mail = "dwq`hlv+qjvlklE`}dhui`+fjhr9\033n<\026"
name = "N}{jbf|/[`|faf-\x00+\x0d|Vhk.|\x18\x11bT\x0bij^"

def getX(i):
	global name
	global mail
	edx = (i << 2)+1
	esi = i * 4
	res1 = sumchars(esi,mail, edx,1)
	edx = (i << 2)+3
	esi = (i << 2)+2
	res2 = sumchars(esi,name, edx,1)
	return res1 % res2
	
def decode2(array):	
	i = 0
	sols = []
	while i < 6:
		x = getX(i)
		sols.append(array[i]-x)
		i+=1
	return sols

def decode1(codes):
	global name
	global mail
	i = 0
	sols = []
	while i <6:
		res1 = sumchars(0, name, 0x20, 1)
		res2 = sumchars(0, mail, 0x20, 1)
		if res1+res2 > codes[i]:
			sols.append((((1<<32) + codes[i])-(res1+res2)) & 0xffffffff)
		else:
			sols.append(codes[i]-(res1+res2))
		i+=1
	return sols
	
def unswap(array):
	res = []
	res.append(array[4])
	res.append(array[0])
	res.append(array[1])
	res.append(array[5])
	res.append(array[2])
	res.append(array[3])
	return res

def computeEAX(res1, res2):
	eax = res1 * res2
	x = eax*0x68db8bad
	msb = x >> 32
	msb = msb >> 0xc
	msb = msb - (eax >> 0x1f)
	msb = msb * 0x2710
	return eax - msb
	
def decode0(array):
	global mail
	i = 0
	sols = []
	while i < 6:
		res1 = sumchars(0, mail, 0x20, i+2)
		res2 = sumchars(i+1, mail, 0x20, i+2)
		res3 = sumchars(0, name, 0x20, 2)
		res4 = sumchars(1, name, 0x20, 2)
		newtoken = array[i] - (4 * (res3 - res4))
		eax = computeEAX(res1, res2)
		sols.append((eax + newtoken) & 0xffffffff)
		i+=1
	return sols
	
print decode0(unswap(decode1(decode2([0x000007f8, 0x00001780, 0x00000b94, 0x000001f8, 0x00000b4b, 0x000011f8]))))
