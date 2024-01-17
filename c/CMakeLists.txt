cmake_minimum_required(VERSION 3.23.2)
project(kcp_server)
set(CMAKE_CXX_STANDARD 17)
set(SRC_SERVER_LIST ikcp.c kcp_server.c)
add_executable(kcp_server ${SRC_SERVER_LIST})


project(kcp_client)
set(CMAKE_CXX_STANDARD 17)
set(SRC_CLIENT_LIST ikcp.c kcp_client.c)
add_executable(kcp_client ${SRC_CLIENT_LIST})


project(main)
set(CMAKE_CXX_STANDARD 17)
set(SRC_MAIN_LIST main.cpp)
add_executable(main ${SRC_MAIN_LIST})