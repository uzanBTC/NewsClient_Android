<?php
    /**
    *Database config variables,
    */
    define("DB_HOST","studev.groept.be"); 
    define("DB_USER","a17_sd606");
    define("DB_PASSWORD","a17_sd606");
    define("DB_DATABASE","a17_sd606");
 
    $connection = mysqli_connect(DB_HOST, DB_USER, DB_PASSWORD, DB_DATABASE);
 
    if(mysqli_connect_errno()){
        die("Database connnection failed " . "(" .
            mysqli_connect_error() . " - " . mysqli_connect_errno() . ")"
                );
    }
?>