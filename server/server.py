#!/usr/bin/python

# Copyright (c) 2012-2013 Arvin Schnell

from socket import socket, error, AF_INET, SOCK_STREAM, SOL_SOCKET, SO_REUSEADDR
from select import poll, POLLIN, POLLOUT

from mixer import Mixer
from mplayer import MPlayer
from xmms import XMMS


def dispatch(command):
    print "received:", command
    tmp = command.split(",")
    program = programs.get(tmp[0])
    if program != None:
        program.action(tmp[1:])


def notify(message):
    print "notify:", message
    for client in clients:
        client.add([ message ])


class ExtMixer(Mixer):
    def notify(self, message):
        notify(message)


class Client:

    def __init__(self, sock):
        self.sock = sock
        self.input = ""
        self.output = ""
        self.input_error = False
        self.output_error = False

    def fileno(self):
        return self.sock.fileno()

    def setup(self):
        print "connected:", self.sock.getpeername()[0], client.fileno()
        poller.register(self.fileno(), POLLIN)

    def set_poller(self):
        flags = 0
        if not self.input_error:
            flags |= POLLIN
        if not self.output_error and self.output:
            flags |= POLLOUT
        poller.modify(self.fileno(), flags)

    def cleanup(self):
        if self.input_error and ( self.output_error or not self.output ):
            print "disconnected:"
            poller.unregister(self.fileno())
            self.sock.close()
            clients.remove(self)

    def handle_read(self):

        try:
            data = self.sock.recv(1)
        except error:
            self.input_error = True
            self.set_poller()
            self.cleanup()
            return

        if not data:
            self.input_error = True
            self.set_poller()
            self.cleanup()
            return

        if data == "\n":
            if self.input:
                dispatch(self.input)
                self.input = ""
        else:
            self.input += data

    def handle_write(self):

        try:
            sent = self.sock.send(self.output[0])
        except error:
            self.output_error = True
            self.set_poller()
            self.cleanup()
            return

        if sent == 0:
            self.output_error = True
            self.set_poller()
            self.cleanup()
            return

        self.output = self.output[1:]

        if not self.output:
            poller.modify(self.fileno(), POLLIN)

    def add(self, messages):

        for message in messages:
            self.output += message + "\n"

        self.set_poller()


programs = dict({ "mixer" : ExtMixer(),
                  "mplayer" : MPlayer(),
                  "xmms" : XMMS() })


serversocket = socket(AF_INET, SOCK_STREAM)
serversocket.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
serversocket.bind(("192.168.178.23", 51203))
serversocket.listen(1)


poller = poll()
poller.register(serversocket.fileno())
for program in programs.values():
    program.setup(poller)


clients = []


while True:

    events = poller.poll()

    if serversocket.fileno() in set([ fd for fd, flags in events ]):
        (clientsocket, address) = serversocket.accept()
        client = Client(clientsocket)
        client.setup()
        clients.append(client)

        for program in programs.values():
            client.add(program.status())

    for fd, flags in events:

        if flags & POLLIN:
            for client in clients:
                if fd == client.fileno():
                    client.handle_read()

        if flags & POLLOUT:
            for client in clients:
                if fd == client.fileno():
                    client.handle_write()


    for program in programs.values():
        program.handle(events)
