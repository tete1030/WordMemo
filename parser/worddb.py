__author__ = 'texot'

import sqlite3
import pickle
from word import Word

class WordDB:

    @staticmethod
    def _init_sqlite_type():
        sqlite3.register_adapter(Word, Word.adapt)
        sqlite3.register_converter('GWORD', Word.convert)

    def __init__(self, file):
        self._init_sqlite_type()
        self._dbcon = sqlite3.connect(file, detect_types=sqlite3.PARSE_DECLTYPES)
        self._init_db()

    def __del__(self):
        self._dbcon.commit()
        self._dbcon.close()

    # tooooooooooooo complex ~~~
    #
    # # id, word, (subword[n])
    # def _create_word_table(self):
    #     pass
    #
    # # id, pronounciation, equvalent, (detail[m]) to word
    # def _create_subword_table(self):
    #     pass
    #
    # # id, POS_name
    # def _create_POS_table(self):
    #     pass
    #
    # # id, (POS[a], paraphrase[b]) to subword
    # def _create_detail_table(self):
    #     pass
    #
    # # id to detail, POS
    # def _create_detail_POS_table(self):
    #     pass
    #
    # # id, cn, en to detail
    # def _create_paraphrase_table(self):
    #     pass
    #

    # id, word, content(pickled data structure)
    def _init_db(self):
        self._dbcon.execute("CREATE TABLE IF NOT EXISTS words"
                            "(id INTEGER PRIMARY KEY AUTOINCREMENT, list_id INTEGER, word TEXT, content GWORD);")
        self._dbcon.execute("CREATE INDEX IF NOT EXISTS wordindex ON words (word)")
        self._dbcon.execute("CREATE TABLE IF NOT EXISTS logs"
                            "(id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            "word_id INTEGER REFERENCES words(id), "
                            "time INTEGER, "
                            # "duration INTEGER, "
                            "result INTEGER)")


    def add_word(self, list_id, word, content):
        self._dbcon.execute("INSERT INTO words(list_id, word, content) VALUES (?, ?, ?)", (list_id, word, Word(content)))

    def get_list(self, list_id, with_content=False):
        columns = "word,content" if with_content else "word"
        result = self._dbcon.execute("SELECT "+columns+" FROM words WHERE list_id = ?", (list_id,)).fetchall()
        ret = [(item[0], item[1].get_data() if with_content else None) for item in result]
        return ret

    def get_word_content(self, word):
        result = self._dbcon.execute("SELECT content FROM words WHERE word = ?", (word,)).fetchone()
        if result is None:
            return None
        return result['content'].get_data()

    def add_log(self, word_id, time, duration, result):
        self._dbcon.execute("INSERT INTO logs(word_id, time, duration, result) values(?, ?, ?, ?)",
                            (word_id, time, duration, result))


    pass