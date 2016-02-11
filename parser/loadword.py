__author__ = 'texot'

from worddb import WordDB


db = WordDB("../db/word.db")

word_list = [item[1] for item in db.get_list(42, True)]

print(len(word_list))
for word in word_list:
    print('%s' % word['word'], end='')
    for sub in word['subwords']:
        print('\t[%s]\t%s' % (sub['pronounce'],
                              ' '.join(('/'.join(detail['POS']) + ' ' +
                                        '；'.join(phr['cn'] for phr in detail['paraphrase']))
                                        for detail in sub['details'])))
