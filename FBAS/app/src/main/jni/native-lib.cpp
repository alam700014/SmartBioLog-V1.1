#include <jni.h>
#include <string>
#include <fcntl.h>
#include <zconf.h>


//=============================== Set Date Time =======================================//

extern "C" JNIEXPORT jstring JNICALL
Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_setDateTimeJNI(
        JNIEnv *env,
        jobject,
        jstring path,
        jstring val) {

//     JNIEnv *env,
//            jobject,
//    jstring com){

    const char *cpath = env->GetStringUTFChars(path, NULL);
    const char *cval = env->GetStringUTFChars(val, NULL);

    system(cpath);

    std::string hello = cpath;
    return env->NewStringUTF(hello.c_str());


//    FILE *pf;
//    char data[512];
//    const char *command = env->GetStringUTFChars(com, NULL);
//
//    // Setup our pipe for reading and execute our command.
//    pf = popen(command, "rw");
//    // Get the data from the process execution
//
//    fputs(command,pf);
//
//   // fgets(data, 512, pf);
//
//    std::string retValue = data;
//    if (pclose(pf) != 0)
//        fprintf(stderr, " Error: Failed to close command stream \n");
//
//    return env->NewStringUTF(retValue.c_str());

}


//=============================== Set GPIO Value =======================================//

extern "C" JNIEXPORT jstring JNICALL
Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_setGPIOValueJNI(
        JNIEnv *env,
        jobject,
        jstring path,
        jstring val) {

    const char *cpath = env->GetStringUTFChars(path, NULL);
    const char *cval = env->GetStringUTFChars(val, NULL);

    system(cpath);

    std::string hello = cval;
    return env->NewStringUTF(hello.c_str());
}

//=============================== Set Green LED Value =======================================//

extern "C" JNIEXPORT jstring JNICALL
Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_setGreenLedValueJNI(
        JNIEnv *env,
        jobject,
        jstring path,
        jstring val) {

    const char *cpath = env->GetStringUTFChars(path, NULL);
    const char *cval = env->GetStringUTFChars(val, NULL);

    system(cpath);
    std::string hello = cval;
    return env->NewStringUTF(hello.c_str());

}

//=============================== Set Red LED Value =======================================//

extern "C" JNIEXPORT jstring JNICALL
Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_setRedLedValueJNI(
        JNIEnv *env,
        jobject,
        jstring path,
        jstring val) {

    const char *cpath = env->GetStringUTFChars(path, NULL);
    const char *cval = env->GetStringUTFChars(val, NULL);

    system(cpath);
    std::string hello = cval;
    return env->NewStringUTF(hello.c_str());
}


//================================= Read Wiegand Value ====================================//

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_getWiegandValueJNI(
//        JNIEnv *env,
//        jobject,
//        jstring com) {
//
//    FILE *pf;
//    char data[512];
//    const char *command = env->GetStringUTFChars(com, NULL);
//
//    // Setup our pipe for reading and execute our command.
//    pf = popen(command, "r");
//    // Get the data from the process execution
//
//    fgets(data, 512, pf);
//
//    std::string retValue = data;
//    if (pclose(pf) != 0)
//        fprintf(stderr, " Error: Failed to close command stream \n");
//
//    return env->NewStringUTF(retValue.c_str());
//}

extern "C" JNIEXPORT jstring JNICALL
Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_getWiegandValueJNI(
        JNIEnv *env,
        jobject,
        jstring com) {

    FILE *pf;
    char data[512];
    const char *command = env->GetStringUTFChars(com, NULL);

    // Setup our pipe for reading and execute our command.
    pf = popen(command, "r");
    // Get the data from the process execution

    fgets(data, 512, pf);

    std::string retValue = data;
    if (pclose(pf) != 0)
        fprintf(stderr, " Error: Failed to close command stream \n");

    return env->NewStringUTF(retValue.c_str());
}



extern "C" JNIEXPORT jstring JNICALL
Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C" JNIEXPORT jint JNICALL
Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_intFromJNI(
        JNIEnv *env,
        jobject, jint value /* this */) {
    jint mydefault = 99 + value;
    return mydefault;
}


extern "C" JNIEXPORT jint JNICALL
Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_intSumFromJNI(
        JNIEnv *env,
        jobject, jint value1, jint value2 /* this */) {
    jint sum = value1 + value2;
    return sum;
}


extern "C" JNIEXPORT jint JNICALL
Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_intBuzzJNI(
        JNIEnv *env,
        jobject,
        jint val /* this */) {
    jint sum = val + 10;
    return sum;
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_wigJNI(
        JNIEnv *env,
        jobject,
        jstring,
        jint val /* this */) {

    FILE *pf;

    int wig_fd;
    int rd_len;
    char rd_data[64];

    memset(rd_data,0,64);
    //char r[256];
    //char a[16];
    //const char *cpath = env->GetStringUTFChars(path,NULL);
    // const char *cval = env->GetStringUTFChars(val,NULL);


    pf = popen("/dev/wig_device", "r");
    // Get the data from the process execution

    fgets(rd_data, 64, pf);


    std::string retValue = rd_data;
    if (pclose(pf) != 0)
        fprintf(stderr, " Error: Failed to close command stream \n");

//    wig_fd = open("/dev/wig_device", O_RDWR);
//    if(wig_fd == -1)
//    {
//        std::string hello = "Error Opening wig_cDriver" ; //"Hello from JNI (with file io)!";
//        return env->NewStringUTF(hello.c_str() );
//    }

    //sleep(1); //wait


//    rd_len = read(wig_fd, rd_data, 0);
//    if(rd_len > 0){
//
//    }


//    close (wig_fd);
//    std::string data = rd_data ; //"Hello from JNI (with file io)!";

    if (strlen(rd_data) > 0) {
        return env->NewStringUTF(retValue.c_str());
    } else {
        std::string error = "No Data Found";
        return env->NewStringUTF(error.c_str());
    }
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_android_fortunaattendancesystem_forlinx_ForlinxHardwareController_resetJNI(
        JNIEnv *env,
        jobject,
        jstring path,
        jstring val) {

    const char *cpath = env->GetStringUTFChars(path, NULL);
    const char *cval = env->GetStringUTFChars(val, NULL);

    system(cpath);

    std::string hello = cval;
    return env->NewStringUTF(hello.c_str());
}