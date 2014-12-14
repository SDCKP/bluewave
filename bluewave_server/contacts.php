<?php
/*Returns a JSON with the contact list of the provided user 
-1: No id provided
-2: No user with such id
-3: No contacts*/

function run() {
	if (isset($_POST['ownerID'])) {
		$ownerID = $_POST['ownerID'];
		//Connect to the db
		$db = new db();
		//Find user with matching email
		$db->query("SELECT id FROM users WHERE id = $ownerID");
		if ($db->result_count() != 1) { //No user with that id (should never happen)
			echo "-2";
		} else {
			//Obtain the links of the specified user
			$db->query("SELECT * FROM user_links WHERE userid_1 = $ownerID OR userid_2 = $ownerID ORDER BY lastmsg_time DESC");
			$link_count = $db->result_count();
			if ($link_count <= 0) {
				echo "-3";
			} else {
				for ($i = 0; $i < $link_count; $i++) {
					$links[] = $db->result_set();
				}
				//Iterate through the links for obtain the data of every link
				$contacts[] = array();
				for ($i = 0; $i < $link_count; $i++) {
					if ($ownerID == $links[$i]->userid_1) {
						$db->query("SELECT * FROM user_profile WHERE user_id = '".$links[$i]->userid_2."'");
					} else {
						$db->query("SELECT * FROM user_profile WHERE user_id = '".$links[$i]->userid_1."'");
					}
					$resultset = $db->result_set();
					//Store the data of every contact on the array
					$contacts[$i]['id'] = $resultset->user_id;
					$contacts[$i]['alias'] = $resultset->alias;
					if ($links[$i]->lastmsg_content != null) {
						$contacts[$i]['lastmsg'] = substr($links[$i]->lastmsg_content, 0, 25);
					} else {
						$contacts[$i]['lastmsg'] = "";
					}
					if ($links[$i]->lastmsg_time != null) {
						$contacts[$i]['lastmsg_time'] = $links[$i]->lastmsg_time;
					} else {
						$contacts[$i]['lastmsg_time'] = "";
					}
					$contacts[$i]['avatar'] = "";
					if ($resultset->avatar != null) {
						$contacts[$i]['avatar'] = $resultset->avatar;
					}
				}
				echo json_encode($contacts);
			}
		}
	} else {
		echo "-1";
	}
}
?>
