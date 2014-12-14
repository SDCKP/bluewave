<?php
/*Returns a JSON with the profile data of the id
-1: Missing parameters
-2: Contact not linked with owner*/

function run() {
	if (isset($_REQUEST['contactID']) && isset($_REQUEST['ownerID'])) {
		$ownerID = $_REQUEST['ownerID'];
		$contactID = $_REQUEST['contactID'];
		//Connect to the db
		$db = new db();
		//Get the links of the user
		$db->query("SELECT * FROM user_links WHERE userid_1 = '$ownerID' OR userid_2 = '$ownerID'");
		//Iterate through the links and check that the from user have the to user on his contact list
		//for ($c = 0; $c < $db->result_count(); $c++) {
		//	$link = $db->result_set();
		//	if ($link->userid_1 == $contactID || $link->userid_2 == $contactID) {
				$contactProfile = array();
				//Query the data of the user
				$db->query("SELECT * FROM user_profile WHERE user_id = $contactID");
				$result = $db->result_set();
				$contactProfile['alias'] = $result->alias;
				$contactProfile['birthdate'] = $result->birthdate;
				$contactProfile['gender'] = $result->gender;
				$contactProfile['nationality'] = $result->nationality;
				$contactProfile['lookingfor'] = $result->lookingfor;
				$contactProfile['about'] = $result->about;
				$contactProfile['height'] = $result->height;
				$contactProfile['weight'] = $result->weight;
				$contactProfile['interests'] = $result->interests;
				$contactProfile['avatar'] = $result->avatar;
				//Print the data of the profile and exits the function
				echo json_encode($contactProfile);
				return;
		//	}
		//}
		echo "-2";
	} else {
		echo "-1";
	}
}
?>
