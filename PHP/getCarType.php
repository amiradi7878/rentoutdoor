<?php
include("include/config.php");
if(isset($_GET['value'])) {
$query = mysqli_query($con, "
SELECT car_rental.rental_Id, car_rental.rental_Title, car_rental.rental_Address, car_rental.rental_Email, car_rental.rental_Web, car_rental.rental_Fb, car_rental.rental_Twitter,
        car_rental.rental_Lat, car_rental.rental_Long, car_rental.rental_Rating, car_rental.ratingCount, car_rental.ratingScore, car_rental.rental_Tagline, car_rental.rental_Desc,
        car_rental.rental_Verif, car_rental.rental_Tag, car_car.car_Id, car_car.car_Title, car_car.car_TypeId, car_car.car_BrandID, car_car.car_RentalId, car_car.car_price, car_car.car_year, car_car.image1, car_car.image2, car_car.image3
FROM car_rental, car_car
WHERE car_rental.rental_Id=car_car.car_RentalId
AND car_car.car_TypeId='" . $_GET['value'] . "' ");
  while ($row = mysqli_fetch_assoc($query)) {
        $res [] =array("id"=>$row["rental_Id"], 
                        "title"=>$row["rental_Title"], 
                        "address"=>$row["rental_Address"], 
                        "telephone"=>$row["rental_Telephone"],
                        "rental_Email"=>$row["rental_Email"],
                        "web"=>$row["rental_Web"],
                        "fb"=>$row["rental_Fb"],
                        "twitter"=>$row["rental_Twitter"],
                        "lat"=>$row["rental_Lat"],
                        "long"=>$row["rental_Long"],
                        "rating"=>$row["rental_Rating"],
                        "ratingCount"=>$row["ratingCount"],
                        "ratingScore"=>$row["ratingScore"],
                        "tagline"=>$row["rental_Tagline"],
                        "desc"=>$row["rental_Desc"],
                        "verif"=>$row["rental_Verif"],
                        "rentLogo"=>$row["thumbImage1"],
                        "tag"=>$row["rental_Tag"],

                        "carId"=>$row["car_Id"],
                        "carTitle"=>$row["car_Title"],
                        "carType"=>$row["car_TypeId"], 
                        "carBrandID"=>$row["car_BrandID"],
                        "carRentalId"=>$row["car_RentalId"],
                        "carPrice"=>$row["car_price"],
                        "carYear"=>$row["car_year"],
                        "thumbImage"=>
            array(  $row["image1"],
                    $row["image2"],
                    $row["image3"]
                )); 
    }
    echo json_encode($res);
}
else
{
    $qu = mysqli_query($con, "
SELECT  car_rental.rental_Id, car_rental.rental_Title, car_rental.rental_Address, car_rental.rental_Telephone, car_rental.rental_Email, 
        car_rental.rental_Web, car_rental.rental_Fb, car_rental.rental_Twitter, car_rental.rental_Lat, car_rental.rental_Long, 
        car_rental.rental_Rating, car_rental.ratingCount, car_rental.ratingScore, car_rental.rental_Tagline, car_rental.rental_Desc, 
        car_rental.rental_Verif, car_rental.thumbImage1, car_rental.rental_Tag, car_car.car_Id, car_car.car_Title, car_car.car_TypeId, car_car.car_BrandID, car_car.car_RentalId, 
        car_car.car_price, car_car.car_year, car_car.image1, car_car.image2, car_car.image3
FROM car_rental, car_car
WHERE car_rental.rental_Id=car_car.car_RentalId ");
    while ($row = mysqli_fetch_assoc($qu)) {
        $res [] =array("id"=>$row["rental_Id"], 
                        "title"=>$row["rental_Title"], 
                        "address"=>$row["rental_Address"], 
                        "telephone"=>$row["rental_Telephone"],
                        "rental_Email"=>$row["rental_Email"],
                        "web"=>$row["rental_Web"],
                        "fb"=>$row["rental_Fb"],
                        "twitter"=>$row["rental_Twitter"],
                        "lat"=>$row["rental_Lat"],
                        "long"=>$row["rental_Long"],
                        "rating"=>$row["rental_Rating"],
                        "ratingCount"=>$row["ratingCount"],
                        "ratingScore"=>$row["ratingScore"],
                        "tagline"=>$row["rental_Tagline"],
                        "desc"=>$row["rental_Desc"],
                        "verif"=>$row["rental_Verif"],
                        "rentLogo"=>$row["thumbImage1"],
                        "tag"=>$row["rental_Tag"],

                        "carId"=>$row["car_Id"],
                        "carTitle"=>$row["car_Title"],
                        "carBrandID"=>$row["car_BrandID"],
                        "carRentalId"=>$row["car_RentalId"],
                        "carPrice"=>$row["car_price"],
                        "carYear"=>$row["car_year"],
                        "thumbImage"=>
            array(  $row["image1"],
                    $row["image2"],
                    $row["image3"]
                )); 
          
    }
    
    echo json_encode($res);
}

?>