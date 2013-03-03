
# Copyright (c) 2012-2013 Arvin Schnell

from os import system
from utils import find_window_cached, send_key, match_wm_class


class XMMS:

    def doit1(self, option):
        system("xmms %s &" % option)


    def match_xmms(self, window):
        return match_wm_class(window, ('XMMS_Player', 'xmms'))


    def doit2(self, keysym):
        window = find_window_cached(self.match_xmms)
        if window != None:
            send_key(window, keysym)


    def setup(self, poller):
        pass


    def handle(self, fds):
        pass


    def action(self, params):
        if params == [ "play" ]:
            self.doit1("--play")
        elif params == [ "pause" ]:
            self.doit1("--pause")
        elif params == [ "stop" ]:
            self.doit1("--stop")
        elif params == [ "quit" ]:
            self.doit1("--quit")
        elif params == [ "forward", "song" ]:
            self.doit1("--fwd")
        elif params == [ "backward", "song" ]:
            self.doit1("--rew")
        elif params == [ "shuffle" ]:
            self.doit2(0x73)
        elif params == [ "repeat" ]:
            self.doit2(0x72)
        elif params == [ "volume", "up" ]:
            self.doit2(0xff52)
        elif params == [ "volume", "down" ]:
            self.doit2(0xff54)
        elif params == [ "forward", "5s" ]:
            self.doit2(0xff53)
        elif params == [ "backward", "5s" ]:
            self.doit2(0xff51)


    def notify(self, message):
        pass


    def status(self):
        return []
