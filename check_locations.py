import requests
import json
import time

def Location():

	#URL's for user and Anind
	testurl  = "http://buildingdepot.andrew.cmu.edu:82/service/api/v1/data/id=abbef1b7-403d-4bb3-b215-9217a3063e1a/interval=100d/"
	anindurl = "http://buildingdepot.andrew.cmu.edu:82/service/api/v1/data/id=2952cfd1-584b-4b03-918a-99944e7990ed/interval=100d/"

	#Check if the URL's are valid
	testurlstatuscode = (requests.head(testurl).status_code)
	anindurlstatuscode = (requests.head(anindurl).status_code)

	# Get RESPONSE from BD on User Location
	if(testurlstatuscode==200 and anindurlstatuscode==200):	
		test_Location_response = requests.get(testurl)
		Anind_Location_response = requests.get(anindurl)

	#Convert response obtained to a JSON String
	test_Location_json = json.dumps(test_Location_response.json(),indent=2)
	Anind_Location_json = json.dumps(Anind_Location_response.json(),indent=2)
	
	test_availability = Anind_availability = "no"

	test_availability = returnAvailability(test_Location_json)
	Anind_availability = returnAvailability(Anind_Location_json)

	if(test_availability == "yes"):
		if(Anind_availability == "yes"):
			print "Both are Available"
			requests.post("http://buildingdepot.andrew.cmu.edu:82/service/permission_change/user=gokulk@andrew.cmu.edu/sensor_group=GoogleSG/permission=rw")
			# time.sleep(10)
		else:
			requests.post("http://buildingdepot.andrew.cmu.edu:82/service/permission_change/user=gokulk@andrew.cmu.edu/sensor_group=GoogleSG/permission=dr")
			print "Only test is available"

def returnAvailability(location_json):
	location_json = json.loads(location_json)
	availability = location_json['data'] ['series'] [0] ['values'] 
	length = len(availability)
	availability = availability [length-1] [2]
	return availability



if __name__ == "__main__":
	while True:
		Location()
	