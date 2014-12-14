<?php
/*Request handler controller
Obtains the type of request from the client
and loads the proper page code for process it*/
require("lib/db.php");

//Check if the type param is set
if (isset($_REQUEST['type'])) {
	//Load a different file depending on the type
	include($_REQUEST['type'].".php");
	//Execute the run method of the loaded file
	run();
} else {
	echo "Missing parameters";
}
