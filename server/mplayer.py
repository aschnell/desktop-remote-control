
# Copyright (c) 2012-2013 Arvin Schnell

from utils import find_window_cached, send_key, match_wm_name


class MPlayer:

    def match_mplayer(self, window):
        return match_wm_name(window, 'MPlayer')


    def doit1(self, keysym):
        window = find_window_cached(self.match_mplayer)
        if window != None:
            send_key(window, keysym)


    def setup(self, poller):
        pass


    def handle(self, fds):
        pass


    def action(self, params):
        if params == [ "volume", "up" ]:
            self.doit1(0xffaa)
        elif params == [ "volume", "down" ]:
            self.doit1(0xffaf)
        elif params == [ "forward", "10s" ]:
            self.doit1(0xff53)
        elif params == [ "backward", "10s" ]:
            self.doit1(0xff51)
        elif params == [ "forward", "1m" ]:
            self.doit1(0xff52)
        elif params == [ "backward", "1m" ]:
            self.doit1(0xff54)
        elif params == [ "forward", "10m" ]:
            self.doit1(0xff55)
        elif params == [ "backward", "10m" ]:
            self.doit1(0xff56)
        elif params == [ "panscan", "plus" ]:
            self.doit1(0x65)
        elif params == [ "panscan", "minus" ]:
            self.doit1(0x77)
        elif params == [ "pause" ]:
            self.doit1(0x70)
        elif params == [ "quit" ]:
            self.doit1(0x71)
        elif params == [ "osd" ]:
            self.doit1(0x6f)
        elif params == [ "fullscreen" ]:
            self.doit1(0x66)


    def notify(self, message):
        pass


    def status(self):
        return []
