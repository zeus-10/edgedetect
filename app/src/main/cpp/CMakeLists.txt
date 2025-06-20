cmake_minimum_required(VERSION 3.22.1)
project("edgedetection")

set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv/sdk/native/jni)


find_package(OpenCV REQUIRED)


if(OpenCV_FOUND)
    message(STATUS "OpenCV found: ${OpenCV_VERSION}")
    message(STATUS "OpenCV include dirs: ${OpenCV_INCLUDE_DIRS}")
    message(STATUS "OpenCV libraries: ${OpenCV_LIBRARIES}")
else()
    message(FATAL_ERROR "OpenCV not found!")
endif()


find_library(log-lib log)
find_library(gles-lib GLESv2)
find_library(android-lib android)


add_library(edgedetection SHARED
        native-lib.cpp
)


target_include_directories(edgedetection PRIVATE
        ${OpenCV_INCLUDE_DIRS}
)


target_link_libraries(edgedetection
        ${OpenCV_LIBRARIES}
        ${log-lib}
        ${gles-lib}
        ${android-lib}
)