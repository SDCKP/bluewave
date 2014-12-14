<?php
/*Returns a JSON with the profile data of the email
-1: No email provided
-2: Email doesn't exists*/

function run() {
	if (isset($_REQUEST['email'])) {
		$postEmail = $_REQUEST['email'];
		//Connect to the db
		$db = new db();
		//Find users with matching email and password
		$db->query("SELECT id FROM users WHERE email = '$postEmail'");
		if ($db->result_count() != 1) { //No user with that email (should never happen)
			echo "-2";
		} else {
			//Obtain the info of the user
			$rs = $db->result_set();
			$id = $rs->id;
			
			$db->query("SELECT * FROM user_profile WHERE user_id = $id");
			
			$resultset = $db->result_set();
			$alias = $resultset->alias;
			
			$usrinfo = array("id"=>$id, "alias"=>$alias);
			echo json_encode($usrinfo);
		}
	} else if (isset($_REQUEST['id'])) {
		$postID = $_REQUEST['id'];
		//Connect to the db
		$db = new db();
		//Get the info of the user
		$db->query("SELECT * FROM user_profile WHERE user_id = $postID");
		
		$resultset = $db->result_set();
		$alias = $resultset->alias;
		
		$usrinfo = array("id"=>$postID, "alias"=>$alias);
		echo json_encode($usrinfo);
	} else {
		echo "-1";
	}
}
?>
