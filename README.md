Bluewave
========
Bluewave was a final project for my class. It is an app that allows to meet and talk with people that you cross through the street on your daily routine, using the Bluetooth technology to find nearby devices and querying a remote server for their profile data, and includes an in-app chat to comunicate with other users remotely.

How does it work
----------------
![explanation](http://i.imgur.com/srSUzzs.png)

Bluewave scans the devices nearby (on the Bluetooth range, which is around 100 meters), gets their MAC address and query a remote server for the information of the device found nearby.
If the MAC address is vinculated with another device the server returns the profile data of the user and allows you to send contact requests to the user.

If the user accepts your contact request you can start chatting with the user remotely (with the remote server as the middle-man). Bluetooth is only required to detect new devices, not for chatting.

The original idea also included the ability to detect nearby bussiness near you when you walk past them and getting alerts and sales offers from them on your device, but that feature was never implemented due to lack of time.

What is the current status of the project?
------------------------------------------
This project was discontinued due to the size, the work required and the lack of resources to complete it fully, it was a very ambitious project overall.

The released source includes the server and the client, so you can build a working copy and test it out, improve or take any part for your project.

Currently there are some known bugs, the most serious are related to the server side, as there is no security at all and SQLi attacks are easily done on it.

Installation
------------
The bluewave_server folder contains a bunch of PHP files required to run on an Apache server with MySQL installed. The database backup is also included (the .sql file on the root of the folders)

The Android app is built using Android Studio, so you will need it to rebuild the project. You will also have to change the IP and port used by the app to conect to the remote server, and point it to your own server.
This can be changed on *bluewave_app/src/com/synxapps/bluewave/util/RESTHandler.java*

License
-------
This project is being released for free under the Creative Commons Attribution License, so you can use it fully or any part of it for anything you want, as long as you give proper attribution to this source.
