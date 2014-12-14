<?php
/*RETURN VALUES:
-1: Missing parameters
-2: Invalid email
-3: Email already in use
-4: Password too simple
1: Registered successfully*/

function run() {
	if (isset($_POST['alias']) && isset($_POST['email']) && isset($_POST['pass']) && isset($_POST['btAddress'])) {
		$postAlias = $_POST['alias'];
		$postEmail = $_POST['email'];
		$postPass = $_POST['pass'];
		$postBtAddress = $_POST['btAddress'];
		//Connect to the DB for check the data of the register
		$db = new db();
		$db->query("SELECT * FROM users WHERE email = '$postEmail'");
		if ($db->result_count() > 0) { //Check if the email exists on the DB
			echo -3;
		} else {
			//TODO: Comprobar la fortaleza de la contraseña
			if (strlen($postPass) >= 6) { //Check the minimum size of the password
				//Check that the email address is valid
				if (filter_var($postEmail, FILTER_VALIDATE_EMAIL) && preg_match('/@.+\./', $postEmail)) {
			     	//Insert the new user on the DB
					//Get the last ID
					$db->query("SELECT MAX(id) FROM users");
					$id = $db->result_set()->max+1;
					//Execute the insertion
					$db->exec("INSERT INTO users (id, email, password, register_ip, register_date, \"bluetooth_MAC\") VALUES ($id, '$postEmail', '$postPass', '".$_SERVER['REMOTE_ADDR']."', now(), '$postBtAddress')");
					//Set the alias of the newly created user
					$db->exec("INSERT INTO user_profile (user_id, alias) VALUES ($id, '$postAlias')");
				    echo "1";
				} else { 
				     echo "-2";
				}
			} else {
				echo "-4";
			}
		}
	} else {
		echo "-1";
	}
}
?>
