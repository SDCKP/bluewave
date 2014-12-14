<?php
/*RETURN VALUES:
-1: Missing parameters
-2: Wrong email or password
>0: Login successfully (return user id)*/

function run() {
	if (isset($_POST['email']) && isset($_POST['pass'])) {
		//TODO: Filtrar inyecciones SQL
		$postEmail = $_POST['email'];
		$postPass = $_POST['pass'];
		//Connect to the db
		$db = new db();
		//Find users with matching email and password
		$db->query("SELECT * FROM users WHERE email = '$postEmail' AND password = '$postPass'");
		//Check if there is any coincidence
		if ($db->result_count() > 0) {
			$id = $db->result_set()->id;
			$db->exec("UPDATE users SET last_login_ip = '".$_SERVER['REMOTE_ADDR']."', last_login_date = now() WHERE id = ".$id);
			echo $id;
		} else {
			echo "-2";
		}
	} else {
		echo "-1";
	}
}
?>
