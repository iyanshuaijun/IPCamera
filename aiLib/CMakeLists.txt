cmake_minimum_required(VERSION 3.10.2)


set(LIB_DIR ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/libs)
set(CPPDIR ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp)

set(TNN ${LIB_DIR}/tnn)

set(KANNAROTATE ${LIB_DIR}/kannarotate-android-lib)
set(FFMPEG_DIR ${LIB_DIR}/ffmpeg)
set(OPENCV_ANDROID ${LIB_DIR}/opencv)

include_directories(${OPENCV_ANDROID}/native/jni/include)
include_directories(${KANNAROTATE}/include)
include_directories(${KANNAROTATE}/src)
include_directories(${TNN}/include)

include_directories(${CPPDIR}/cc)
include_directories(${FFMPEG_DIR}/include)
link_directories(${FFMPEG_DIR}/lib)


set(LIB_RTSP ${CPPDIR}/RTSPRender.cpp ${CPPDIR}/FFDemux.cpp ${CPPDIR}/FFBridge.cpp)
set(LIB_AI ${CPPDIR}/AIRender.cpp)

set(ANDROID_SYSLIBS android log GLESv3 OpenSLES)

#SET(FFMPEG_LIB avformat avcodec avdevice avfilter avutil swresample swscale fdk-aac x264 z c m dl)


##################### TNN - ENV - LIB ###########################


#=== 导入TNN库libTNN.so
add_library(tnn_lib SHARED IMPORTED)
set_target_properties(tnn_lib PROPERTIES IMPORTED_LOCATION ${TNN}/${ANDROID_ABI}/libTNN.so)

#=== 编译写好的接口, 生成链接文件
file(GLOB_RECURSE TNN_WRAPPER_SRCS ${CPPDIR}/cc/*.cc)    # 包含TNN的代码
file(GLOB_RECURSE KANNAROTATE_SRCS ${KANNAROTATE}/src/*.c)    # 包含TNN的代码
#file(GLOB_RECURSE OTHER_SRCS ${CMAKE_SOURCE_DIR}/AIRender.cpp)         # 不包含TNN的代码
add_library( # Sets the name of the library.
        aiNative
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        ${TNN_WRAPPER_SRCS} ${OTHER_SRCS} ${KANNAROTATE_SRCS})

#=== 将TNN库与目标文件链接
target_link_libraries( # Specifies the target library.
        aiNative tnn_lib  -ljnigraphics ${ANDROID_SYSLIBS})


###################  ffmpeg - lib ####################################

add_library(avformat
        SHARED
        IMPORTED)

set_target_properties(
        avformat
        PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavformat.so)

add_library(avcodec
        SHARED
        IMPORTED)

set_target_properties(
        avcodec
        PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavcodec.so)

add_library(swscale
        SHARED
        IMPORTED)

set_target_properties(
        swscale
        PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libswscale.so)


add_library(avutil
        SHARED
        IMPORTED)

set_target_properties(
        avutil
        PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavutil.so)


add_library(swresample
        SHARED
        IMPORTED)

set_target_properties(
        swresample
        PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libswresample.so)


add_library(avfilter
        SHARED
        IMPORTED)

set_target_properties(
        avfilter
        PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavfilter.so)


add_library(postproc
        SHARED
        IMPORTED)

set_target_properties(
        postproc
        PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libpostproc.so)

############### RTSP - LIB ################
add_library(RTSP SHARED ${LIB_RTSP})
target_link_libraries(
        RTSP
        avcodec
        avfilter
        avformat
        avutil
        swresample
        swscale
        postproc
        ${ANDROID_SYSLIBS})