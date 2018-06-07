//
// Created by sym on 2018/6/6.
//
#include "com_appsflyer_androidsampleapp_JniTest.h"
#include <studio.h>
JNIEXPORT jstring JNICALL Java_com_appsflyer_androidsampleapp_JniTest_get
  (JNIEnv *env, jobject thiz){

        printf("invoke get in c\n");

        return (*env)->NewStringUTF(env,"Hello from JNI!");
  }

JNIEXPORT void JNICALL Java_com_appsflyer_androidsampleapp_JniTest_set
  (JNIEnv *env, jobject thiz, jstring string){

        printf("invoke set from c\n");
        char* str = (char*)(*env) -> GetStringUTFChars(env,string,NULL);

        printf("%s\n",str);
        (*env)->ReleaseStringUTFChars(env,string,str);
  }

