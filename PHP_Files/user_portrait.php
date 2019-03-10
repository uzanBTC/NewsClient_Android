<?php

global $connection;
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


$upload_url = 'User/';

//response array
$response = array();


if($_SERVER['REQUEST_METHOD']=='POST'){

    //checking the required parameters from the request
    if(isset($_POST['isSuccess']))
    {

	      
        $fileinfo = pathinfo($_FILES['image']['name']);//getting file info from the request
        $extension = $fileinfo['extension']; //getting the file extension
        $file_url = $upload_url.getFileName().'.'.$extension; //file url to store in the server
        $user_id = getFileName();
	$img_name=$user_id. '.'. $extension; //file name to store in the database
	

        try{
            move_uploaded_file($_FILES['image']['tmp_name'],$file_url); //saving the file to the uploads folder
                            
            //adding the path and name to database
            $sql = "UPDATE Users SET photoName='{$img_name}' WHERE userID=$user_id ";
          			
            if(mysqli_query($connection,$sql)){
                //filling response array with values
                $response['error'] = false;
                $response['photoName'] = $img_name;
            	               
            }
            //if some error occurred
        }catch(Exception $e){
            $response['error']=true;
            $response['message']=$e->getMessage();
        }
        //displaying the response
        echo json_encode($response);

        //closing the connection
        mysqli_close($connection);
    }else{
        $response['error'] = true;
        $response['message']='Please choose a file';
    }
}

/*
We are generating the file name
so this method will return a file name for the image to be uploaded
*/
function getFileName(){
    global $connection;

    $sql = "SELECT max(userID) as id FROM Users";
    $result = mysqli_fetch_array(mysqli_query($connection, $sql));

    if($result['id']== null)
        return 1;
    else
        return $result['id'];

    mysqli_close($connection);
}
chmod($upload_url.$img_name, 0755);

?>
