import requests
import json
import time
from datetime import datetime, timedelta

def Location():

# --------------------------------------------------------------------------------------------------------------------------------------------- #
	# The Granter's OAuth credentials are to be obtained from the BuildingDepot's DataService, in the OAuth dropdown
	# Login credentials for the granter in BuildingDepot demo is:
	# userID : check@check.com 
	# password : check
# --------------------------------------------------------------------------------------------------------------------------------------------- # 
	# Enter details here
	ip = "http://128.2.113.192:82"
	client_id = "GQLRaedrQHnlXDbsd8BIBdDchmdyBotuHfT7W0rt" # Granter's client ID
	client_secret = "yXTE8ZfcGkfnWqkx4KqQKT365WXzNd51Mietf53m9wTkC2Az31" # Granter's client secret
	nonadmin_email = "non-admin@non-admin.com"
	admin_email = "admin@admin.com"
# --------------------------------------------------------------------------------------------------------------------------------------------- #

	# Headers
	headers = {'content-type':'application/json',
			'charset' : 'utf-8',
			'Authorization' : 'Bearer dummy'}

	# Get an AccessToken and update header
	checker_access_url = ip + "/oauth/access_token/client_id="+client_id+"/client_secret="+client_secret
	checker_access_token = requests.get(checker_access_url, headers = headers).json()
	headers['Authorization'] = "Bearer "+checker_access_token['access_token']

	# Time series data fetch for locations of admin and non-admin
	end_time = int(time.time())
	start_time = int((datetime.now() - timedelta(days=20)).strftime("%s"))

	# URL's for nonadmin and admin
	nonadmin_url  = ip + "/api/sensor/6cf69555-a7f4-4fca-b655-b141752cc4f7/timeseries?start_time="+`start_time`+"&end_time="+`end_time`
	admin_url = ip + "/api/sensor/3fdb4978-a192-4269-bbb8-22314583a25c/timeseries?start_time="+`start_time`+"&end_time="+`end_time`

	# Check if the URL's are valid
	# nonadmin_urlstatuscode = (requests.head(nonadmin_url).status_code)
	# admin_urlstatuscode = (requests.head(admin_url).status_code)

	# Get RESPONSE from BD on User Location	
	nonadmin_Location_response = requests.get(nonadmin_url, headers = headers)
	admin_Location_response = requests.get(admin_url, headers = headers)

	# Convert response obtained to a JSON String
	nonadmin_Location_json = json.dumps(nonadmin_Location_response.json(),indent=2)
	admin_Location_json = json.dumps(admin_Location_response.json(),indent=2)
	
	nonadmin_availability = admin_availability = "no"

	nonadmin_availability = returnAvailability(nonadmin_Location_json)
	admin_availability = returnAvailability(admin_Location_json)
	
	if(nonadmin_availability == "yes"):
		if(admin_availability == "yes"):
			print "Both are Available"
			requests.get(ip+"/api/permission_change/user="+nonadmin_email+"/sensor_group=GoogleSG/permission=rw")
			# time.sleep(10)
		else:
			requests.get(ip+"/api/permission_change/user="+nonadmin_email+"/sensor_group=GoogleSG/permission=dr")
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
