import Image
		
def read_pix(index):
	im = Image.open('frames/frame-'+str(index)+'.png')
	rgb_im = im.convert('RGB')
	width, height = im.size
	res = []
	for i in xrange(width):
		for j in xrange(height):
			pixel = rgb_im.getpixel((j, i))
			res.extend(pixel)
	return "".join(chr(i) for i in res)

#file = open('execme', 'wb')
#file.write("".join(read_pix(i) for i in xrange(110)))
#file.close()
res = read_pix(0)
for i in xrange(1,110):
	res += read_pix(i)[:48]
file = open('execme', 'wb')
file.write(res)
file.close()
