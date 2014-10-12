WhatToWear-Android-Weather-App
==============================

Android based weather application which suggests different set of clothes to wear according to weather condition.

> Individual Android project developed for CMPE277 (Fall 2014) at San Jose State University
- Title: WhatToWear
- Youtube Demo Video: http://youtu.be/21kZTheFFO8

##Requirements
--------------
To run this application from eclipse, JDK 1.7, Android SDK, Google Play support libraries should be installed on it.

##Basic Configuration
--------------
* Mobile should have android version 4.4.2 installed on it
* GPS should be enabled on it.


##Developed
--------------
Following directory information:

* [src/com/example/androidweatherproject/MainActivity.java] - Main Activity handles weather details and city search functionality.
* [src/com/example/androidweatherproject/ParseActivity.java] - Parse Activity fetches outfit from Parse.com database according to weather.
* [/res/*] - contains layout files, activity xmls, icon images.
* [/assets/Fonts/*] - otf file for font description


##Technology Used
--------------
* Google API - To find the current location by using GPS of android phone, Google Maps, Google playservices and Google API has been used
* Yahoo API -  To fetch weather conditions according to current location or the city entered by user, Yahoo API’s have been.
* Parse - Parse.com is used as a cloud storage to store outfit images and weather images.

##Tools being used
--------------
* Eclipse for development
* Android SDK
* Google API, Yahoo API and Parse libraries

##Features
--------------
*	Display weather conditions to user according to current location.
*	Suggest a different outfit everytime a user checks application and which should be suitable for current weather condition.
*	Search facitlity have been provided to user where she can search for any city for its weather and outfit to be worn there.
*	Activity window slide display has been applied while switching between 2 activities.
*	Outfit suggestions are focussed on female Users only.
*	GPS and Internet should be enabled on device for this application to work
* Application is supported with Landscape and Portrait Layout.
