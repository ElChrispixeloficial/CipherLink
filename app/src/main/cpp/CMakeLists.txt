cmake_minimum_required(VERSION 3.22.1)

project("ciphercore" LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Source files
add_library(
    ciphercore
    SHARED
    ciphercore.cpp
    crypto/secure_operations.cpp
)

# Include directories
target_include_directories(
    ciphercore
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/crypto
)

# Find Android log library
find_library(log-lib log)

# Link libraries
target_link_libraries(
    ciphercore
    ${log-lib}
)
