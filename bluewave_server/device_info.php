<?php
/*Returns a JSON with the profile data of the MAC
-1: Missing parameters
-2: No user with that MAC
-3: Invalid owner MAC addr*/

function run() {
	if (isset($_REQUEST['ownerMAC']) && isset($_REQUEST['targetMAC'])) {
		$ownerMAC = $_REQUEST['ownerMAC'];
		$targetMAC = $_REQUEST['targetMAC'];
		//Connect to the db
		$db = new db();
		//Find users with matching email and password
		$db->query("SELECT id FROM users WHERE \"bluetooth_MAC\" = '$targetMAC'");
		if ($db->result_count() != 1) { //No user with that mac address
			echo "-2";
		} else {
			//Obtain the info of the user
			$rs = $db->result_set();
			$id = $rs->id;
			
			$db->query("SELECT * FROM user_profile WHERE user_id = $id");
			
			$resultset = $db->result_set();
			$alias = $resultset->alias;
			$avatar = $resultset->avatar;
			
			$usrinfo = array("id"=>$id, "alias"=>$alias, "avatar"=>$avatar);
			echo json_encode($usrinfo);
		}
	} else {
		echo "-1";
	}
}
?>
