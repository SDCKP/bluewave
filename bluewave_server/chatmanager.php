<?php
/*RETURN VALUES:
-1: Missing parameters
-2: Invalid action
-3: Contact is not on the list of the sender
Any positive number: Message sent successful (the number is the ID of the msg sent)*/

function run() {
	$result;
	if (isset($_POST['action'])) {
		switch ($_POST['action']) {
			case "sendmsg":
				$result = sendMessage();
				break;
			case "getmsgs":
				$result = readMessages();
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

//Send a message to the user. Technically the message gets stored on a queue until the target asks for it
function sendMessage() {
	if (isset($_POST['from']) && isset($_POST['to']) && isset($_POST['content'])) {
		$fromID = $_POST['from'];
		$toID = $_POST['to'];
		$content = $_POST['content'];
		//Connect to the db
		$db = new db();
		//Get the current time from the DB
		$db->query("SELECT now()");
		$time = $db->result_set()->now;
		//Get the links of the user
		$db->query("SELECT * FROM user_links WHERE userid_1 = '$fromID' OR userid_2 = '$fromID'");
		//Iterate through the links and check that the from user have the to user on his contact list
		for ($c = 0; $c < $db->result_count(); $c++) {
			$link = $db->result_set();
			if ($link->userid_1 == $toID || $link->userid_2 == $toID) {
				$db->exec("INSERT INTO user_msg_queue VALUES ($fromID, $toID, '$time', '$content')");
				$sent = true;
				//Update the last message of the link
				$db->exec("UPDATE user_links SET lastmsg_time = now(), lastmsg_content = '$content' WHERE (userid_1 = $fromID AND userid_2 = $toID) OR (userid_1 = $toID AND userid_2 = $fromID)");
				//Get the message ID and return it to the user
				$db->query("SELECT msgid FROM user_msg_queue WHERE id_from = $fromID AND id_to = $toID AND content = '$content' AND send_time = '$time'");
				return $db->result_set()->msgid;
			}
		}
		return "-3";
	} else {
		return "-1";
	}
}

//Gather the messages on the queue
//This should be secured, so only the owner of this ID can get the messages
function readMessages() {
	if (isset($_POST['id'])) {
		$ownerID = $_POST['id'];
		//Connect to the db
		$db = new db();
		$db->query("SELECT * FROM user_msg_queue WHERE id_to = $ownerID");
		$msgcount = $db->result_count();
		$messages = array();
		//Check if there is any message
		if ($msgcount > 0) {
			for ($m = 0; $m < $msgcount; $m++) {
				$msg = $db->result_set();
				$messages[$m]['msgid'] = $msg->msgid;
				$messages[$m]['id_from'] = $msg->id_from;
				$messages[$m]['send_time'] = $msg->send_time;
				$messages[$m]['content'] = $msg->content;
			}
			//Delete the messages from the queue
			$db->exec("DELETE FROM user_msg_queue WHERE id_to = $ownerID");
			//Return the JSON-formatted data with the messages that were on the queue
			return json_encode($messages);
		} else {
			return "0";
		}
	} else {
		return "-1";
	}
}
?>
