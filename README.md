## An Alexa-controlled Christmas Tree

Here you can find the sources of a custom Alexa skill that controls an LED strand on a Christmas
tree. This repo contains:
* Java code for an AWS Lambda function which is the endpoint for the Alexa skill
* An Arduino sketch which sits between AWS IoT and a WS2811 LED strand. The sketch is optimized for running on an Arduino Yun.

####The Hardware setup
If you want to build your own Alexa-controlled christmas tree with help of these sources you need specific hardware. This project used the
following components:
* 1 x [Arduino Yun with Linino OS](https://www.arduino.cc/en/Main/ArduinoBoardYun)
* 2 x [WS2811 LED Strand with 50 LEDs each](https://www.amazon.com/dp/B013FU2PZ4)
* 3 x [Jumper wires](https://www.amazon.com/dp/B01EV70C78)
* 1 x [Power Supply Adapter and 2.1mm x 5.5mm DC Connector](https://www.amazon.com/dp/B00ZU9MZ1S)
* 1 x [Micro-USB to USB Cable](https://www.amazon.com/dp/B00NH13O7K)

####The Software solution
The following image illustrates a typical roundtrip to handle a voice user request.
![](docs/solution-architecture.png)
The solution leverages a bunch of AWS cloud services to communicate with the hardware backend - the
christmas tree. The only things you really need to set up is the Lambda function, an S3 bucket containing
the MP3 files and an IAM role with AWS IoT and Dynamo permissions. The table in Dynamo as well as
the thing shadow in AWS IoT will be created on the first skill invocation on the fly.

So what happens on a voice user request given to an Alexa device from a technical perspective?
1) User speaks to Alexa to _"open the christmas tree"_. ASR and NLU magic happens in the Alexa cloud service.
2) An intent is given to the skill code hosted in AWS Lambda. You can find the code in this repo.
3) If the user just desires an action like _"turn on the tree"_ or _"start the show"_ without giving
this skill a color for the tree it looks up the last set color in Dynamo DB. If there's a color
given the skill will persist the information in the same table. This is how Alexa keeps in mind the last set color
of the tree. Secondly, the action and the color command is written to a thing shadow in AWS IoT.
4) If the shadow is updated an MQTT message is exposed to the delta topic of the corresponding thing. The Arduino Yun
is subscribed to that topic. Side note: The name of the thing being created by the skill code is equal
to the skill-id coming in (all dots replaced with a dash). This might help you if you want to rebuild the project.
5) The Arduino is polling on the Delta topic so it receives the commands as an MQTT message in JSON format.
The information is extracted and the Arduino sketch performs an action with the LED strand according to what is given in the message (new color, christmas show, on, off).
6) Finally, the Arduino sends an MQTT message to the Update topic of the AWS IoT thing in order to let the world know
that the action was performed.
7) The message is consumed by the AWS IoT service and the contained state information
is written back to the thing shadow as a _reported_ state. It would be possible to also have the skill read the last tree state from the thing shadow
instead of looking it up in Dynamo DB. The reason for this fallback approach is MQTT is asynchronous and we cannot rely on
the Arduino to give an immediate response.
8) Actually this step happens right after step 3) as the skill is decoupled from the hardware backend on purpose.
So right after updating the thing shadow in AWS IoT the skill code returns output speech text and optionally an
_SSML_ tag with audio contents. The MP3s which are part of Alexa's playback (christmas sounds) are stored in an AWS S3 bucket.
9) Alexa reads out the text returned by the skill and plays back the audio in the response.