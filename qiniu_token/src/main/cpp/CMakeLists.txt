cmake_minimum_required(VERSION 3.22.1)
project("qiniu_token")

add_library( # Sets the name of the library.
        qiniu_token
        SHARED
        qiniu_token.cpp

        cpp-base64-master/base64.cpp

        hmac-sha1-master/src/hmac/hmac_sha1.c
        hmac-sha1-master/src/sha/sha1.c
        )
target_link_libraries( # Specifies the target library.
        qiniu_token
        log
        )
target_include_directories(
        qiniu_token
        PUBLIC
        .
        cpp-base64-master
        hmac-sha1-master/src
)