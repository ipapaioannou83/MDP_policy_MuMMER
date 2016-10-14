# -*- coding: utf-8 -*-
"""
Created on Mon Oct 10 14:29:32 2016

@author: elveleg
"""

from naoqi import ALProxy
from naoqi import ALModule


class SpeechEventModule(ALModule):
    """ A simple module able to react
    to facedetection events

    """
    def __init__(self, name):
        ALModule.__init__(self, name)
        # No need for IP and port here because
        # we have our Python broker connected to NAOqi broker

        # Create a proxy to ALTextToSpeech for later use
        self.tts = ALProxy("ALTextToSpeech")

        # Subscribe to the FaceDetected event:
        global memory
        memory = ALProxy("ALMemory")
        memory.subscribeToEvent("SpeechDetected",
            "SpeechEvent",
            "onSpeechDetected")

    def onSpeechDetected(self, *_args):
        """ This will be called each time a face is
        detected.

        """
        # Unsubscribe to the event when talking,
        # to avoid repetitions
        memory.unsubscribeToEvent("SpeechDetected",
            "SpeechEvent")

        #self.tts.say("OK")
        

        # Subscribe again to the event
        memory.subscribeToEvent("SpeechDetected",
            "SpeechEvent",
            "onSpeechDetected")