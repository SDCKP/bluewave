<?php
/*Returns a JSON with the contact list of the provided user 
-1: No id specified
-2: User doesnt exists
1: Success*/

function run() {
	if (isset($_REQUEST['ownerID']) && isset($_REQUEST['contactID'])) {
		$ownerID = $_REQUEST['ownerID'];
		$contactID = $_REQUEST['contactID'];
		//Connect to the db
		$db = new db();
		//Find user with matching id
		$db->query("SELECT id FROM users WHERE id = $contactID");
		if ($db->result_count() != 1) { //No user with that id (should never happen)
			echo "-2";
		} else {
			//Delete the link between the users
			$db->exec("DELETE FROM user_links WHERE (userid_1 = $ownerID AND userid_2 = $contactID) OR (userid_1 = $contactID AND userid_2 = $ownerID)");
			echo "1";
		}
	} else {
		echo "-1";
	}
}
?>
