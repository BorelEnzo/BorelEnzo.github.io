import socket
import md5
import threading
import time
import random

import MTRecover

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(('notrandom.ctf.insecurity-insa.fr', 10002))
values = []
i = 0
res = s.recv(1024)
print res
res = s.recv(1024)
print res
while i < 626:
	s.send(str("x\n"))
	res = s.recv(1024)
	res = s.recv(1024)
	print res
	nextnumber = res[res.find('+')+2:res.find('\n')].strip()	
	values.append(int(nextnumber))
	i+=1
len(values)
rand = MTRecover.recover(values)

print '***********************DONE*************************'
while 1:
	nextrand = rand.getrandbits(32)
	print nextrand
	print '**********************'
	index = res.find('jackpot')+8
	hashtofind = res[index:index+32]
	print hashtofind
	for i in range(2014):
		m = md5.new()
		m.update(str(nextrand+i))
		if m.hexdigest() == hashtofind:
			s.send(str(i))
			print 'Sent %d' % i
	res = s.recv(1024)
	print res
	if 'INSA' in res:
		break
	res = s.recv(1024)
	if 'INSA' in res:
		break
	print res
s.close()
#INSA{Why_w0ulD_U_Us3_b4s1c_r4nd0m}
#https://github.com/eboda/mersenne-twister-recover
