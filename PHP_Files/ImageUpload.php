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



//$upload_url = 'http://a17-sd606.studev.groept.be/Image/'; //upload url
$upload_url = 'Image/';

//response array
$response = array();


if($_SERVER['REQUEST_METHOD']=='POST')
{

    //checking the required parameters from the request
    if(isset($_POST['isSuccess']))
    {

	     $position=$_POST['position'];
	     $newsID=getFileName();
        $fileinfo = pathinfo($_FILES['image']['name']);//getting file info from the request
        $extension = 'jpg'; //getting the file extension
        $file_url = $upload_url.$newsID.$position.'.'.$extension; //file url to store in the server
        $img_name = $newsID.$position.'.'.$extension; //file name to store in the database
               try{
            		move_uploaded_file($_FILES['image']['tmp_name'],$file_url); //saving the file to the uploads folder
	       	  if(strcmp($position,"up")==0)
	       	 {
			 //adding the path and name to database
            		$sql = "UPDATE News SET frontPhoto='{$img_name}' WHERE newsID=$newsID ";

           		 if(mysqli_query($connection,$sql)){
               		 //filling response array with values
              		  $response['error'] = false;
              		  $response['photoName'] = $img_name;

         		   }
            }
	         else
		         {
			 //adding the path and name to database
            			$sql = "UPDATE News SET backPhoto='{$img_name}' WHERE newsID=$newsID ";

           			 if(mysqli_query($connection,$sql)){
               		 	//filling response array with values
              		  	$response['error'] = false;
              		  	$response['photoName'] = $img_name;

		                  }

		         }

	       }
            //if some error occurred
        catch(Exception $e){
            $response['error']=true;
            $response['message']=$e->getMessage();
        }
        //displaying the response
        echo json_encode($response);
    }else{
        $response['error'] = true;
        $response['message']='Please choose a file';
    }
chmod($upload_url.$img_name, 0755);

}


function getFileName(){
    global $connection;

    $sql = "SELECT max(newsID) as id FROM News";
    $result = mysqli_fetch_array(mysqli_query($connection, $sql));

    if($result['id']== null)
        return 1;
    else
        return $result['id'];

    mysqli_close($connection);
}
