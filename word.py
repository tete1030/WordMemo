__author__ = 'texot'

import pickle
import json

class Word:
    def __init__(self, word):
        self._word = word

    def adapt(self):
        return json.dumps(self._word).encode("utf-8")

    def get_data(self):
        return self._word

    @staticmethod
    def convert(data: bytes):
        return Word(json.loads(data.decode("utf-8")))