import requests
import json
import time
from datetime import datetime, timedelta

def location():
	url_cs = "http://bd-exp.andrew.cmu.edu:81"
	url_ds = "http://bd-exp.andrew.cmu.edu:82"

	access_url = url_cs+"/oauth/access_token/client_id=0Pmnt97iI9XLloCCuORVPkrH7PMHagDcV9YEFres/client_secret=OnHvYroZDsXjZdHIOtEWsRWw7n4w2NDzpgzRwr0rURkiHP0U3F";

	headers = {'content-type':'application/json',
				'charset' : 'utf-8',
				'Authorization' : 'Bearer '}

	access_token = requests.get(access_url, headers = headers).json()
	headers['Authorization'] = 'Bearer '+ access_token['access_token']

	admin_tag_payload = {'data':{"Tags":["user:admin@buildingdepot.org"]}}
	non_admin_tag_payload = {'data':{"Tags":["user:cmuiotexpedition@gmail.com"]}}

	tag_search_url = url_cs + "/api/search"

	response_admin = requests.post(tag_search_url, headers = headers,data =json.dumps(admin_tag_payload))
	response_non_admin = requests.post(tag_search_url, headers = headers,data =json.dumps(non_admin_tag_payload))

	admin_uuid = findUUID(response_admin.json())
	non_admin_uuid = findUUID(response_non_admin.json())

	# Time series data fetch for locations of admin and non-admin
	end_time = int(time.time())
	start_time = int((datetime.now() - timedelta(days=1)).strftime("%s"))

	non_admin_url  = url_ds + "/api/sensor/"+non_admin_uuid+"/timeseries?start_time="+`start_time`+"&end_time="+`end_time`
	admin_url = url_ds + "/api/sensor/"+admin_uuid+"/timeseries?start_time="+`start_time`+"&end_time="+`end_time`

	# Get RESPONSE from BD on User Location	
	admin_Location_response_json = requests.get(admin_url, headers = headers).json()
	non_admin_Location_response_json = requests.get(non_admin_url, headers = headers).json()

	non_admin_availability = "no"
	admin_availability = "no"
	
	admin_availability = getLocationResponse(admin_Location_response_json)
	non_admin_availability = getLocationResponse(non_admin_Location_response_json)

	data_permission_r = {
	"data":{
    	"sensor_group":"admin_sensor_group",
      	"user_group":"admin_user_group",
      	"permission":"r"
  		}
	}	

	data_permission_dr = {
	"data":{
    	"sensor_group":"admin_sensor_group",
      	"user_group":"admin_user_group",
      	"permission":"dr"
  		}
	}


	if(non_admin_availability == "yes"):
		if(admin_availability == "yes"):
			print "Both are Available"
			requests.post(url_cs + "/api/permission", headers = headers, data = json.dumps(data_permission_r))
		else:
			requests.post(url_cs + "/api/permission", headers = headers, data = json.dumps(data_permission_dr))
			print "Only test is available"


def findUUID(json_data):
	return json_data['result'][0]['name']

def getLocationResponse(location_json):
	availability = location_json['data'] ['series'] [0] ['values']
	length = len(availability)
	return availability[length-1][2]


if __name__ == "__main__":
	while 1:
		location()
