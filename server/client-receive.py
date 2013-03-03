#!/usr/bin/python

# Copyright (c) 2013 Arvin Schnell

import socket
import sys

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

s.connect(("192.168.178.23", 51203))

while True:
    data = s.recv(1)
    if not data:
        break
    sys.stdout.write(data)

s.close()
