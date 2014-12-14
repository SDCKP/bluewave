<?php
/*RETURN VALUES:
-1: Missing parameters
-2: Invalid action
-3: Already on contact list
-4: User dont exists
Returns a JSON with the request list*/

function run() {
	$result;
	if (isset($_POST['action'])) {
		switch ($_POST['action']) {
			case "getrequests":
				$result = getRequests();
				break;
			case "sendrequest":
				$result = sendRequest();
				break;
			case "rejectrequest":
				$result = rejectRequest();
				break;
			default:
				$result = "-2";
				break;
		}
		
	} else {
		$result = "-1";
	}
	echo $result;
}

//Gather the contact requests for the specified user
function getRequests() {
	if (isset($_POST['id'])) {
		$ownerID = $_POST['id'];
		//Connect to the db
		$db = new db();
		$db->query("SELECT * FROM user_requests WHERE seen = 0 AND \"targetID\" = $ownerID");
		$requestcount = $db->result_count();
		$requests = array();
		//Check if there is any request
		if ($requestcount > 0) {
			for ($r = 0; $r < $requestcount; $r++) {
				$req = $db->result_set();
				$requests[$r]['requester_id'] = $req->requesterID;
				$requests[$r]['found_time'] = $req->found_time;
			}
			for ($r = 0; $r < $requestcount; $r++) {
				$db->query("SELECT * FROM user_profile WHERE user_id = ".$requests[$r]['requester_id']);
				$req = $db->result_set();
				$requests[$r]['alias'] = $req->alias;
				$requests[$r]['avatar'] = $req->avatar;
			}
			//Set the requests as seen
			$db->exec("UPDATE user_requests SET seen = 1 WHERE \"targetID\" = $ownerID");
			//Return the JSON-formatted data with the requests
			return json_encode($requests);
		} else {
			return "0";
		}
	} else {
		return "-1";
	}
}

//Send the contacting request to the user
function sendRequest() {
	if (isset($_POST['ownerID']) && isset($_POST['targetID'])) {
		$ownerID = $_POST['ownerID'];
		$targetID = $_POST['targetID'];
		//Connect to the db
		$db = new db();
		//Check that the user exists
		$db->query("SELECT * FROM users WHERE id = $targetID");
		if ($db->result_count() > 0) {
			//Get the links of the user
			$db->query("SELECT * FROM user_links WHERE userid_1 = $ownerID OR userid_2 = $ownerID");
			//Iterate through the links and check that the owner doesnt have already the contact on its list
			for ($c = 0; $c < $db->result_count(); $c++) {
				$link = $db->result_set();
				if ($link->userid_1 == $targetID || $link->userid_2 == $targetID) {
					return "-3";
				}
			}
			//Check if the other user already requested the user to be contact
			$db->query("SELECT * FROM user_requests WHERE \"requesterID\" = $targetID AND \"targetID\" = $ownerID");
			if ($db->result_count() > 0) {
				//The other user requested to be contact aswell, remove the contact request and add both users to the contacts
				$db->exec("DELETE FROM user_requests WHERE \"requesterID\" = $targetID AND \"targetID\" = $ownerID");
				$db->exec("INSERT INTO user_links VALUES ($targetID, $ownerID, now())");

				//Return a success code (adds contact)
				return "2";
			} else {
				$db->exec("INSERT INTO user_requests VALUES ($ownerID, $targetID, now(), 0)");
				//Return a success code (request sent)
				return "1";
			}
		} else {
			return "-4";
		}
		return "-3";
	} else {
		return "-1";
	}
}

//Remove the request of contact from the DB
function rejectRequest() {
	if (isset($_POST['ownerID']) && isset($_POST['targetID'])) {
		$ownerID = $_POST['ownerID'];
		$targetID = $_POST['targetID'];

		//Connect to the db
		$db = new db();
		//Check that the user exists
		$db->query("SELECT * FROM users WHERE id = $targetID");
		if ($db->result_count() > 0) {
			//Get the links of the user
			$db->query("SELECT * FROM user_links WHERE userid_1 = $ownerID OR userid_2 = $ownerID");
			//Iterate through the links and check that the owner doesnt have already the contact on its list
			for ($c = 0; $c < $db->result_count(); $c++) {
				$link = $db->result_set();
				if ($link->userid_1 == $targetID || $link->userid_2 == $targetID) {
					return "-3";
				}
			}
			//Check if the other user requested the user to be contact
			$db->query("SELECT * FROM user_requests WHERE \"requesterID\" = $targetID AND \"targetID\" = $ownerID");
			if ($db->result_count() > 0) {
				//The other user requested to be contact, remove the contact request
				$db->exec("DELETE FROM user_requests WHERE \"requesterID\" = $targetID AND \"targetID\" = $ownerID");

				//Return a success code
				return "1";
			}
		} else {
			return "-4";
		}
		return "-3";
	} else {
		return "-1";
	}
}

?>
