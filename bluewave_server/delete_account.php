<?php
/*RETURN VALUES:
-1: Missing parameters
-2: Wrong id/password combination
1: Account deleted successfully*/

function run() {
	if (isset($_REQUEST['userID']) && isset($_REQUEST['current_pass'])) {
		//TODO: Filtrar inyecciones SQL
		$userID = $_REQUEST['userID'];
		$cur_pass = $_REQUEST['current_pass'];
		//Connect to the db
		$db = new db();
		//Find users with matching id and password
		$db->query("SELECT * FROM users WHERE id = $userID AND password = '$cur_pass'");
		//Check if there is any coincidence
		if ($db->result_count() > 0) {
			//Delete the account data from the database
			$db->exec("DELETE FROM user_links WHERE userid_1 = $userID OR userid_2 = $userID");
			$db->exec("DELETE FROM user_msg_queue WHERE id_from = $userID OR id_to = $userID");
			$db->exec("DELETE FROM user_profile WHERE user_id = $userID");
			$db->exec("DELETE FROM users WHERE id = $userID");
			echo "1";
		} else {
			echo "-2";
		}
	} else {
		echo "-1";
	}
}
?>
