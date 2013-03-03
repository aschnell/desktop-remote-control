#!/usr/bin/python

# Copyright (c) 2013 Arvin Schnell

from socket import socket, AF_INET, SOCK_STREAM

import sys

if len(sys.argv) < 2:
    sys.stderr.write("usage: client-send.py args")
    sys.exit(1)

command = " ".join(sys.argv[1:])
print "'" + command + "'"

s = socket(AF_INET, SOCK_STREAM)

s.connect(("192.168.178.23", 51203))

s.sendall(command + "\n")

s.close()
