<?php
/*RETURN VALUES:
-1: Missing parameters
-2: Wrong id/password combination
1: Password changed successfully*/

function run() {
	if (isset($_REQUEST['userID']) && isset($_REQUEST['current_pass']) && isset($_REQUEST['new_pass'])) {
		//TODO: Filtrar inyecciones SQL
		$userID = $_REQUEST['userID'];
		$cur_pass = $_REQUEST['current_pass'];
		$new_pass = $_REQUEST['new_pass'];
		//TODO: New password complexity check
		//Connect to the db
		$db = new db();
		//Find users with matching id and password
		$db->query("SELECT * FROM users WHERE id = $userID AND password = '$cur_pass'");
		//Check if there is any coincidence
		if ($db->result_count() > 0) {
			//Change the password and return an 1 as a success code
			$db->exec("UPDATE users SET password = '$new_pass' WHERE id = $userID");
			echo "1";
		} else {
			echo "-2";
		}
	} else {
		echo "-1";
	}
}
?>
