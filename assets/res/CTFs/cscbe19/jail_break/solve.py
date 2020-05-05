import subprocess
import os

arch = "3airqiv23gkr.zip"
prev = ""
name = ""
try:
	while True:
		res = subprocess.check_output(["unzip", arch])
		prev = arch
		arch = res[res.find("extracting: ")+12:].strip()
		name += prev + "\n"
		os.system("rm " + prev)
except:
	print name
