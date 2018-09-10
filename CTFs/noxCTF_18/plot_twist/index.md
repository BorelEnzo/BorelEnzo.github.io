# Plot Twist

### [~$ cd ..](../)

We are given the following Python [script](server.py):

> ```python
>import socket
>import threading
>import random
>from Crypto.Cipher import AES
>
>class ThreadedServer(object):
>	def __init__(self, host, port):
>		self.host = host
>		self.port = port
>		self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
>		self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
>		self.sock.bind((self.host, self.port))
>		self.flag = open('flag.txt', 'r').read()
>
>	def listen(self):
>		self.sock.listen(20)
>		while True:
>		    client, address = self.sock.accept()
>		    client.settimeout(60)
>		    threading.Thread(target = self.listenToClient,args = (client,address)).start()
>	
>	def getKey(self, r):
>		return str(r.getrandbits(32)).rjust(16, '0')
>
>	def pad(self, s):
>		return s + chr(0)*((-len(s)) % 16)
>
>	def encrypt(self, key, plaintext):
>		aes = AES.new(key, AES.MODE_CBC, self.pad('SuperSecretIV'))
>		return aes.encrypt(self.pad(plaintext))		
>
>	def decrypt(self, key, ciphertext):
>		aes = AES.new(key, AES.MODE_CBC, self.pad('SuperSecretIV'))
>		return aes.decrypt(ciphertext)
>
>	def listenToClient(self, client, address):
>		client_flag = self.flag
>		r = random.Random()
>		key = self.getKey(r)
>		client_flag = self.encrypt(key, client_flag)
>		while True:
>			try:
>				client.send('Please insert the decryption key:\n')
>				key_guess = client.recv(16)
>				if key_guess == key:
>					client.send('Correct! Your flag is: ' + self.decrypt(key, client_flag) + '\n')
>					client.close()
>					break
>				else:
>					client.send('Wrong! The key was: ' + key + '\n')
>					client_flag = self.decrypt(key, client_flag)
>					key = self.getKey(r)
>					client_flag = self.encrypt(key, client_flag)
>			except Exception as e:
>				print e
>				client.close()
>				return False
>
>if __name__ == "__main__":
>	ThreadedServer('0.0.0.0', 5115).listen(
> ```

Because of the title, we immediately guessed that it was about PRNG, and that the goal would be to recover the internal state and guess the next "random" number.

We downloaded the eboda's [script](https://github.com/eboda/mersenne-twister-recover/blob/master/MTRecover.py), and added the following lines at the end:

> ```python
>def test_PythonMT19937Recover(n):
>	mtb = MT19937Recover()
>	r1 = random.Random()
>	r2 = mtb.go(n)
>	return r2.getrandbits(32)
>
>import socket
>import time
>i = 0
>data = []
>s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
>s.connect(('chal.noxale.com', 5115))
>while len(data) < 624:	
>	print s.recv(1024)
>	s.send("0123456789012345")
>	res = s.recv(1024)
>	print res
>	code =  res[res.find(':')+1:res.find('\n')]
>	print code
>	data.append(int(code))
>	i+=1
>	print i
>nextcode = test_PythonMT19937Recover(data)
>print s.recv(1024)
>s.send(str(nextcode).zfill(16))
>print "Sent %s " % str(nextcode).zfill(16)
>res = s.recv(1024)
>print res
>s.close()
> ```

And finally, we got the flag: **noxCTF{41w4ys_us3_cryp70_s3cur3d_PRNGs}**
