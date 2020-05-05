import requests
import json
url = 'http://54.171.88.202:8000/easy/'

s = 'start'
gates = []
def call(str):
	global gates
	res = requests.get(url+str)
	data = json.loads(res.text)
	gateways =  data['gateways']
	for g in gateways:
		gate = gateways[g]
		if gate != None and gate not in gates:
			gates.append(gate)
			call(gate)
call(s)
print gates

#CSCBE{It's dangerous to hack alone}
