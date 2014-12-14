<?php
class db {

//Connection properties
private $host = "localhost";
private $dbname = "Bluewave";
private $user = "postgres";
private $pass = "123456";
private $conn;
private $last_result;

private function connect() {
	//Set the timezone
	date_default_timezone_set('Europe/Madrid');
	//Connects to the DB
	$this->conn = new PDO("pgsql:host=".$this->host.";dbname=".$this->dbname, $this->user, $this->pass);
}

public function exec($qry) {
	//Check if there is already an existing connection, else create a new one
	if ($this->conn == null) { $this->connect(); }

	//Execute the query
	$this->conn->exec($qry);
}

public function query($qry) {
	//Check if there is already an existing connection, else create a new one
	if ($this->conn == null) { $this->connect(); }

	//Execute the query and save the resultset
	$this->last_result = $this->conn->query($qry);
}

public function result_count() {
	return $this->last_result->rowCount();
}

public function result_set() {
	return $this->last_result->fetch(PDO::FETCH_OBJ);
}

}
