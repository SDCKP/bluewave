<?php
/*Update the profile of the selected user
-1: Missing parameters
-2: Invalid fields
1: Profile updated*/

function run() {
	if (isset($_POST['userID']) && isset($_POST['alias']) && isset($_POST['gender']) && isset($_POST['birthdate'])
	 && isset($_POST['nationality']) && isset($_POST['lookingfor']) && isset($_POST['about']) && isset($_POST['about'])
	  && isset($_POST['height']) && isset($_POST['weight']) && isset($_POST['interests']) && isset($_POST['avatar'])) {
		$userID = $_POST['userID'];
		$alias = $_POST['alias'];
		$gender = $_POST['gender'];
		$birthdate = $_POST['birthdate'];
		$nationality = $_POST['nationality'];
		$lookingfor = $_POST['lookingfor'];
		$about = $_POST['about'];
		$height = $_POST['height'];
		$weight = $_POST['weight'];
		$interests = $_POST['interests'];
		$avatar = $_POST['avatar'];
		//Connect to the db
		$db = new db();
		//Check the fields content
		if (strlen($alias) > 0 && strlen($alias) <= 16 && checkGender($gender) && checkNationality($nationality) && checkLookingFor($lookingfor) && strlen($about) <= 500 && (($height >= 140 && $height <= 220) || $height == 0) && (($weight >= 45 && $weight <= 140) || $weight == 0) && checkAge($birthdate) && checkInterests($interests)) {
			//Generate the update query
			$sql = "UPDATE user_profile SET alias = '$alias',";
			if (strlen($birthdate) > 0) {
				$sql .= " birthdate = '$birthdate',";
			}
			$sql .= " gender = '$gender', nationality = '$nationality', lookingfor = '$lookingfor',";
			if ($about != "null") {
				$sql .= " about = '$about',";
			}
			$sql .= " height = $height, weight = $weight,";
			if (strlen($avatar) > 0) {
				$sql .= "avatar = '$avatar',";
			}
			$sql .= " interests = '$interests' WHERE user_id = $userID";
			//Run the query
			$db->exec($sql);
			echo "1";
		} else {
			echo "-2";
		}
	} else {
		echo "-1";
	}
}

function checkInterests($i) {
	$in = array("FOOTBALL","CYCLING","TRAVEL","VIDEOGAMES","TVSHOWS","ANIME","MANGA","PROGRAMMING","PARTY","DANCING",
			"MUSIC","MUSICIAN","POLITICS","MOVIES","TECHNOLOGY","DOGS","CATS","EXOTICPETS","NULL");
	$iArr = explode(",", $i);
	foreach ($iArr as $interest) {
		if (!in_array($interest, $in)) {
			return false;
		}
	}
	return true;
}

function checkGender($g) {
	$gen = array("MALE","FEMALE","OTHER","NULL");
	
	return in_array($g, $gen);
}

function checkNationality($n) {
	$nat = array("NORTHAMERICAN","SPANISH","BRITISH","IRISH","SOUTHAMERICAN","GERMAN","SWEDISH","PORTUGUESE",
			"ITALIAN","CHINESE","INDIAN","EGYPTIAN","ARAB","SOUTHAFRICAN","GYPSY","FRENCH","RUSSIAN","NULL");
	
	return in_array($n, $nat);
}

function checkLookingFor($lf) {
	$lokfor = array("FRIEND","LOVE","SEX","SWINGER","HOBBY_MATE","NULL");
	
	return in_array($lf, $lokfor);
}

function checkAge($a) {
	//TODO: Server-side check of the birthdate. Although is not mandatory, it should be done soon or later, as client-side check is not enough
	return true;
}
?>
