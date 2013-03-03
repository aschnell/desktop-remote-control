
# Copyright (c) 2012-2013 Arvin Schnell

from Xlib import X, display
from Xlib.protocol import event
from Xlib.error import BadWindow


display = display.Display()

root = display.screen().root


def send_key(window, keysym):

    keycode = display.keysym_to_keycode(keysym)

    # print "send_key", window, keycode

    keypress_event = event.KeyPress(
        detail = keycode,
        time = X.CurrentTime,
        root = root,
        window = window,
        child = X.NONE,
        root_x = 1, root_y = 1,
        event_x = 1, event_y = 1,
        state = 0,
        same_screen = 1
    )

    window.send_event(keypress_event)
    display.sync()

    keyrelease_event = event.KeyRelease(
        detail = keycode,
        time = X.CurrentTime,
        root = root,
        window = window,
        child = X.NONE,
        root_x = 1, root_y = 1,
        event_x = 1, event_y = 1,
        state = 0,
        same_screen = 1
    )

    window.send_event(keyrelease_event)
    display.sync()


def match_wm_name(window, wm_name):
    try:
        return window.get_wm_name() == wm_name
    except BadWindow:
        return False


def match_wm_class(window, wm_class):
    try:
        return window.get_wm_class() == wm_class
    except BadWindow:
        return False


def find_window_helper(func, window):

    tree = window.query_tree()

    for child in tree.children:

        if func(child):
            return child

        x = find_window_helper(func, child)

        if x != None:
            return x

    return None


def find_window(func):

    print "searching window"

    return find_window_helper(func, root)


def find_window_cached(func):

    window = find_window_cached.cache.get(func)

    if window == None or not func(window):
        window = find_window(func)
        find_window_cached.cache[func] = window

    return window

find_window_cached.cache = dict()
