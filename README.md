# token-dispenser
Stores email-password pairs, gives out Google Play Store tokens.

Using Google Play Store API requires logging in using email and password. If you have a project which works with Google Play Store API you no longer have to make the users use their live accounts or ship your software with your account credentials inside. You can deploy a token dispenser instance and it will provide auth tokens on demand without letting the world know your password.

### Building

1. `git clone https://github.com/yeriomin/token-dispenser`
2. `cd token-dispenser`
3. Edit `src/main/resources/config.properties`
4. `mvn install`
5. `java -jar target/token-dispenser.jar`

### Configuration

[config.properties](/src/main/resources/config.properties) holds token dispenser's configuration.

Two things are configurable:
* web server
* storage

#### Web server

Token dispenser uses [spark framework](http://sparkjava.com/). To configure network address and port on which spark should listen change `spark-host` and `spark-port`.

#### Storage

There are two storage options supported:
* **Plain text** Set `storage` to `plaintext` to use it. `storage-plaintext-path` property is used to store filesystem path to a plain text file with email-password pairs. There is an example [here](/passwords.txt). Securing it is up to you.
* **MongoDB** Set `storage` to `mongodb` to use it. Configurable parameters are self-explanatory.

### Usage
Once server is configured, you can get the tokens for **regular requests** at http://server-address:port/token/email/youremail@gmail.com
and tokens for **checkin requests** at http://server-address:port/token-ac2dm/email/youremail@gmail.com

### Credits

* [play-store-api](https://github.com/yeriomin/play-store-api)
* [spark](http://sparkjava.com/)
