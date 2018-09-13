from PIL import Image
im = Image.open('drumbone.new.png')

pixels = im.load()
out = Image.new(im.mode, im.size)
new_pix = out.load()
width, height = im.size
for i in xrange(width):
	for j in xrange(height):
		pix= pixels[i,j]
		if pix[0] == 0:
			a = -2
			while a < 4:
				b = -2
				while b < 4:
					new_pix[i+a,j+b] = (255,0,0)
					b += 1
				a += 1
		elif new_pix[i,j] != (255,0,0):
			new_pix[i,j] = (255,255,255)
		
im.close()
out.save("drumbone.out.png")
out.close()
