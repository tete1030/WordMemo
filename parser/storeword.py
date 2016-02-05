__author__ = 'texot'

from wordparser import WordListParser
from worddb import WordDB


word_list_str = open('word/list32.txt', encoding='utf-8').read()
word_list = WordListParser.parse(word_list_str)
db = WordDB("../db/word.db")

print(len(word_list))
for word in word_list:
    db.add_word(32, word['word'], word)
    print('%s' % word['word'], end='')
    for sub in word['subwords']:
        print('\t[%s]\t%s' % (sub['pronounce'],
                              ' '.join(('/'.join(detail['POS']) + ' ' +
                                        '；'.join(phr['cn'] for phr in detail['paraphrase']))
                                        for detail in sub['details'])))

