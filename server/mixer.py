
# Copyright (c) 2012-2013 Arvin Schnell

from re import match
from os import system
from pyalsa import alsamixer


class Mixer:

    def __init__(self):

        self.mixer = alsamixer.Mixer()
        self.mixer.attach()
        self.mixer.load()

        self.names = [ "Master", "PCM", "Front" ]

        self.elements = dict()

        for name in self.names:
            element = alsamixer.Element(self.mixer, name)
            element.set_callback(self.event_callback)
            self.elements[name] = element


    def make_message(self, element):

        name = element.name
        volume = element.get_volume_tuple()
        switch = element.get_switch_tuple()

        line = "mixer " + name

        if len(volume) == 0:
            line += " unknown"
        else:
            line += " " + str(element.ask_volume_dB(volume[0]) * 0.01) + "dB"

        if len(switch) == 0:
            line += " unknown"
        else:
            line += " " + ("on" if switch[0] else "off")

        return line


    def event_callback(self, element, events):
        self.notify(self.make_message(element))


    def action_volume(self, name, delta):

        element = self.elements[name]

        vl = list(element.get_volume_tuple())
        vl = map(lambda v: element.ask_volume_dB(v), vl)
        vl = map(lambda v: v + delta, vl)
        vl = map(lambda v: element.ask_dB_volume(v, cmp(delta, 0)), vl)
        element.set_volume_tuple(tuple(vl))

        self.notify(self.make_message(element))


    def action_switch(self, name):

        element = self.elements[name]

        sl = list(element.get_switch_tuple())
        sl = map(lambda s: not s, sl)
        element.set_switch_tuple(tuple(sl))

        self.notify(self.make_message(element))


    def setup(self, poller):

        self.mixer.register_poll(poller)

        print "mixer.poll_fds", self.mixer.poll_fds


    def handle(self, events):

        tmp1 = set([ fd for fd, flags in events ])
        tmp2 = set([ fd for fd, flags in self.mixer.poll_fds ])

        if tmp1.intersection(tmp2):
            self.mixer.handle_events()


    def action(self, params):

        if params[0] == "volume":
            m = match("([+-]?[0-9]+)dB", params[2])
            self.action_volume(params[1], 100 * int(m.group(1)))
        elif params[0] == "switch":
            self.action_switch(params[1])
        elif params == [ "reset" ]:
            system("alsactl restore -f ~/.asound.onboard.state 0 &")


    def notify(self, message):
        pass


    def status(self):
        return [ self.make_message(element) for element in self.elements.values() ]
