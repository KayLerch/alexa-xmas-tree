#include <Adafruit_NeoPixel.h>
#include <aws_iot_mqtt.h>
#include <aws_iot_version.h>
#include "aws_iot_config.h"

#ifdef __AVR__
  #include <avr/power.h>
#endif

#define PIN 6
#define NUM_LEDS 100
#define BRIGHTNESS 100

Adafruit_NeoPixel strip = Adafruit_NeoPixel(NUM_LEDS, PIN, NEO_RGB + NEO_KHZ800);
aws_iot_mqtt_client myClient; // init iot_mqtt_client

char msg[32]; // read-write buffer
int cnt = 0; // loop counts
int rc = -100; // return value placeholder
bool success_connect = false; // whether it is connected
char JSON_buf[100];
int gamma[] = {
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  1,  1,  1,
    1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  2,  2,  2,  2,  2,  2,
    2,  3,  3,  3,  3,  3,  3,  3,  4,  4,  4,  4,  4,  5,  5,  5,
    5,  6,  6,  6,  6,  7,  7,  7,  7,  8,  8,  8,  9,  9,  9, 10,
   10, 10, 11, 11, 11, 12, 12, 13, 13, 13, 14, 14, 15, 15, 16, 16,
   17, 17, 18, 18, 19, 19, 20, 20, 21, 21, 22, 22, 23, 24, 24, 25,
   25, 26, 27, 27, 28, 29, 29, 30, 31, 32, 32, 33, 34, 35, 35, 36,
   37, 38, 39, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 50,
   51, 52, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 66, 67, 68,
   69, 70, 72, 73, 74, 75, 77, 78, 79, 81, 82, 83, 85, 86, 87, 89,
   90, 92, 93, 95, 96, 98, 99,101,102,104,105,107,109,110,112,114,
  115,117,119,120,122,124,126,127,129,131,133,135,137,138,140,142,
  144,146,148,150,152,154,156,158,160,162,164,167,169,171,173,175,
  177,180,182,184,186,189,191,193,196,198,200,203,205,208,210,213,
  215,218,220,223,225,228,231,233,236,239,241,244,247,249,252,255 };

void setup() {
  // Start Serial for print-out and wait until it's ready
  Serial.begin(115200);
  //while(!Serial);

  delay(3000);

  strip.setBrightness(BRIGHTNESS);
  strip.begin();
  strip.show();
  
  char curr_version[80];
  snprintf_P(curr_version, 80, PSTR("AWS IoT SDK Version(dev) %d.%d.%d-%s\n"), VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH, VERSION_TAG);
  Serial.println(curr_version);

  while(success_connect == false) {
     if((rc = myClient.setup(AWS_IOT_CLIENT_ID, true, MQTTv311, true)) == 0) {
      if((rc = myClient.configWss(AWS_IOT_MQTT_HOST, AWS_IOT_MQTT_PORT, AWS_IOT_ROOT_CA_PATH)) == 0) {
        if((rc = myClient.connect()) == 0) {
          success_connect = true;
  
          // indicate success with green lights
          changeColor(0, 255, 0, 0);
          delay(1000);
          changeColor(0, 0, 0, 0);
          
          print_log("shadow init", myClient.shadow_init(AWS_IOT_MY_THING_NAME));
          print_log("register thing shadow delta function", myClient.shadow_register_delta_func(AWS_IOT_MY_THING_NAME, msg_callback_delta));
        }
        else {
          // indicate error with red lights
          changeColor(255, 0, 0, 0);
          delay(1000);
          changeColor(0, 0, 0, 0);
          Serial.println(F("Connect failed!"));
          Serial.println(rc);
        }
      }
      else {
        // indicate error with red lights
        changeColor(255, 0, 0, 0);
        delay(1000);
        changeColor(0, 0, 0, 0);
        Serial.println(F("Config failed!"));
        Serial.println(rc);
      }
    }
    else {
      // indicate error with red lights
      changeColor(255, 0, 0, 0);
      delay(1000);
      changeColor(0, 0, 0, 0);
      Serial.println(F("Setup failed!"));
      Serial.println(rc);
    }
    // Delay to make sure SUBACK is received, delay time could vary according to the server
    delay(2000); 
  }
}

void loop() {
  if(success_connect) {
    if(myClient.yield()) {
      Serial.println(F("Yield failed."));
    }
    delay(1000); // check for incoming delta per 100 ms
    //Serial.println(F("Looping..."));
  }
}

bool print_log(const char* src, int code) {
  bool ret = true;
  if(code == 0) {
    #ifdef AWS_IOT_DEBUG
      Serial.print(F("[LOG] command: "));
      Serial.print(src);
      Serial.println(F(" completed."));
    #endif
    ret = true;
  }
  else {
    #ifdef AWS_IOT_DEBUG
      Serial.print(F("[ERR] command: "));
      Serial.print(src);
      Serial.print(F(" code: "));
     Serial.println(code);
    #endif
    ret = false;
  }
  Serial.flush();
  return ret;
}

void msg_callback_delta(char* src, unsigned int len, Message_status_t flag) {
  Serial.println(F("Message arrived."));
  if(flag == STATUS_NORMAL) {
    print_log("getDeltaKeyValue", myClient.getDeltaValueByKey(src, "io.klerch.alexa.xmastree.skill.model.TreeState\"r", JSON_buf, 50));
    int r = (String(JSON_buf)).toInt();
    Serial.println(r);
    
    print_log("getDeltaKeyValue", myClient.getDeltaValueByKey(src, "io.klerch.alexa.xmastree.skill.model.TreeState\"g", JSON_buf, 50));
    int g = (String(JSON_buf)).toInt();
    Serial.println(g);
    
    print_log("getDeltaKeyValue", myClient.getDeltaValueByKey(src, "io.klerch.alexa.xmastree.skill.model.TreeState\"b", JSON_buf, 50));
    int b = (String(JSON_buf)).toInt();
    Serial.println(b);
    
    print_log("getDeltaKeyValue", myClient.getDeltaValueByKey(src, "io.klerch.alexa.xmastree.skill.model.TreeState\"mode", JSON_buf, 50));
    String mode = String(JSON_buf);
    Serial.println(mode);

    //print_log("getDeltaKeyValue", myClient.getDeltaValueByKey(src, "", JSON_buf, 100));
    //String payload = "{\"state\":{\"reported\":";
    //payload += JSON_buf;
    //payload += "}}";
    //payload.toCharArray(JSON_buf, 100);
    //print_log("update thing shadow", myClient.shadow_update(AWS_IOT_MY_THING_NAME, JSON_buf, strlen(JSON_buf), NULL, 5));

    if (mode == "COLOR") {
        //changeColorBackwards(0, 0, 0, 30);
        changeColor(r, g, b, 300);
    }
    else if (mode == "ON") {
        changeColor(r, g, b, 300);
    }
    else if (mode == "OFF") {
        changeColorBackwards(0, 0, 0, 50);
    }
    else if (mode == "SHOW") {
        long showTime = 0;
        while(showTime < 50000) {
          int to = random(5, strip.numPixels());
          showTime = showTime + (10 * to);
          changeColorPartial(random(0, 255), random(0, 255), random(0, 255), 10, to);

          Serial.println(showTime);

          int to2 = random(5, strip.numPixels());
          showTime = showTime + (10 * to2);
          changeColorBackwardsPartial(random(0, 255), random(0, 255), random(0, 255), 10, to2);

          Serial.println(showTime);
        }
        Serial.println(showTime);
        changeColor(r, g, b, 15);
    }
    else if (mode == "STOP") {
        Serial.println("Would stop show because SHOW.");
        changeColor(r, g, b, 0);
    }
    else {
        Serial.println("Unexpected mode given - do nothing.");
    }
  }
}

void changeColor(uint8_t r, uint8_t g, uint8_t b, int delayMs) 
{
  changeColorPartial(r, g, b, delayMs, strip.numPixels());
}

void changeColorPartial(uint8_t r, uint8_t g, uint8_t b, int delayMs, int to) 
{
  for(uint8_t i=0; i<to; i++) {
    // look for colorful
    if (r == 1 && g == 1 && b == 1) {
      strip.setPixelColor(i, strip.Color(random(0, 255), random(0, 255), random(0, 255)));
    }
    else {
      strip.setPixelColor(i, strip.Color(r, g, b));
    }
    if (delayMs > 0) {
      delay(delayMs);
      strip.show();
    }
  }
  strip.show();
}

void randomColors() 
{
  for(uint8_t i=0; i<strip.numPixels(); i++) {
    int r = random(0, 255);
    int g = random(0, 255);
    int b = random(0, 255);
    strip.setPixelColor(i, strip.Color(r, g, b));
    Serial.println(String(i) + ") " + String(r) + ", " + String(g) + ", " + String(b));
  }
  strip.show();
}

void changeColorBackwards(uint8_t r, uint8_t g, uint8_t b, int delayMs) 
{
  changeColorBackwardsPartial(r, g, b, delayMs, 0);
}

void changeColorBackwardsPartial(uint8_t r, uint8_t g, uint8_t b, int delayMs, int to) 
{
  for(uint8_t i=strip.numPixels()-1; i>=to; i--) {
    // look for colorful
    if (r == 1 && g == 1 && b == 1) {
      strip.setPixelColor(i, strip.Color(random(0, 255), random(0, 255), random(0, 255)));
    }
    else {
      strip.setPixelColor(i, strip.Color(r, g, b));
    }
    
    if (delayMs > 0) {
      delay(delayMs);
      strip.show();
    }
  }
  strip.show();
}

// Basic callback function that prints out the message
void msg_callback(char* src, unsigned int len, Message_status_t flag) {
  if(flag == STATUS_NORMAL) {
    Serial.println("CALLBACK:");
    int i;
    for(i = 0; i < (int)(len); i++) {
      Serial.print(src[i]);
    }
    Serial.println("");
  }
}

// Slightly different, this makes the rainbow equally distributed throughout
void rainbowCycle(uint8_t wait) {
  uint16_t i, j;

  for(j=0; j<256 * 5; j++) { // 5 cycles of all colors on wheel
    for(i=0; i< strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel(((i * 256 / strip.numPixels()) + j) & 255));
    }
    strip.show();
    delay(wait);
  }
}

void rainbow(uint8_t wait) {
  uint16_t i, j;

  for(j=0; j<256; j++) {
    for(i=0; i<strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel((i+j) & 255));
    }
    strip.show();
    delay(wait);
  }
}

// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) {
  WheelPos = 255 - WheelPos;
  if(WheelPos < 85) {
    return strip.Color(255 - WheelPos * 3, 0, WheelPos * 3,0);
  }
  if(WheelPos < 170) {
    WheelPos -= 85;
    return strip.Color(0, WheelPos * 3, 255 - WheelPos * 3,0);
  }
  WheelPos -= 170;
  return strip.Color(WheelPos * 3, 255 - WheelPos * 3, 0,0);
}

uint8_t red(uint32_t c) {
  return (c >> 8);
}
uint8_t green(uint32_t c) {
  return (c >> 16);
}
uint8_t blue(uint32_t c) {
  return (c);
}

