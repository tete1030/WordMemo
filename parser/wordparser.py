__author__ = 'texot'

import re

class WordListParser:

    @classmethod
    def parse(cls, words_text: str):
        wordstr_list = words_text.split('\n\n')
        return [WordParser.parse(wordstr.strip()) for wordstr in wordstr_list]

'''
{
  'word': word(str),
  'subwords': [
                {
                  'pronounce': pron(str),
                  'equvalent': equv(str),
                  'details': [
                               {
                                 'POS': pos(list(str)),
                                 'paraphrase': [
                                                 {
                                                   'cn': cn(str),
                                                   'en': en(str)
                                                 },
                                                 ...
                                               ]
                               },
                               ...
                             ]
                },
                ...
              ],
  'remarks': [
               (remark_type(str), remark_content(str)),
               ...
             ]
}

'''

class WordParser:

    @classmethod
    def parse(cls, word_text: str):
        lines = re.split(r'(?:\r)?\n(?:\r)?', word_text)
        _word = lines[0].strip()
        line2 = lines[1].strip()
        # line with soundmark alone
        line2_pron_match = re.match(r'^\s*\[\s*(.+?)\s*\]\s*$', line2)
        remarks_line_index = None

        if line2_pron_match is not None:
            # word with single pronounciation

            line_index = 2
            while line_index < len(lines):
                # end of meaning when hit chinese (paraphrase is start with part-of-speech)
                if ord(lines[line_index][0]) >= 128: break
                line_index += 1

            subword_data = (
                (line2_pron_match.group(1),)
                + cls._parse_subword(' '.join(lines[2:line_index]))
            )

            _subwords = [cls._make_subword(*subword_data)]

            remarks_line_index = line_index
        else:
            # word with multiple pronounciations (heteronyms)

            _subwords = []
            line_index = 1
            while line_index < len(lines):
                cur_line_str = lines[line_index].strip()
                pron_match = re.match(r'\s*\[\s*(.+?)\s*\]\s*', cur_line_str)
                if pron_match is None:
                    break

                sub_line_index = 1
                while line_index+sub_line_index < len(lines):
                    first_char = lines[line_index+sub_line_index][0]
                    # the first word in this line is a word type content
                    if ord(first_char) < 128 and first_char != '[':
                        sub_line_index += 1
                    else:
                        break

                subword_data = (
                    (pron_match.group(1),)
                    + cls._parse_subword(
                        cur_line_str[pron_match.end():] + ' '
                        + ' '.join(lines[line_index+1 : line_index+sub_line_index])
                      )
                )

                _subwords.append(cls._make_subword(*subword_data))
                line_index += sub_line_index

            remarks_line_index = line_index

        _remarks = []
        if len(lines) > remarks_line_index:
            _remarks = cls._parse_remarks(lines[remarks_line_index:])

        return {'word': _word, 'subwords': _subwords, 'remarks': _remarks}


    @classmethod
    def _make_subword(cls, pron, equivalent, detail):
        return {"pronounce": pron, "equvalent": equivalent, "details": detail}

    @classmethod
    def _parse_subword(cls, content: str) -> (str, list):
        pos_phr_list = []
        equivalent = None
        remaining_str = content
        equv_match = re.match(r'\s*（\s*＝\s*([a-z](?:-[a-z]+)*)\s*）\s*', content)
        if equv_match is not None:
            equivalent = equv_match.group(1)
            remaining_str = content[equv_match.end(0):]
        pos_types = r'\b(?:adj|adv|n|v|prep|interj|conj|a)\.'
        content_list = re.split('\\s*(%s(?:\\s*/\\s*%s)*)\\s*' % (pos_types, pos_types),
                                remaining_str)
        # delete empty group
        del content_list[0]

        for list_index in range(0, len(content_list), 2):
            pos_phr_list.append({
                'POS': re.findall(pos_types, content_list[list_index]),
                'paraphrase': cls._parse_paraphrase(content_list[list_index+1])
            })

        return (equivalent, pos_phr_list)

    '''
    Paraphrase
    Return [(中文释义, english), ...]'''
    @classmethod
    def _parse_paraphrase(cls, content):
        phr_str_list = content.split('；')
        phr_list = []
        for phr in phr_str_list:
            phr_match = re.match(r'(?P<cn>.*)（(?P<en>.*)）', phr)
            if phr_match is None or not cls._is_eng(phr_match.group('en')):
                phr_list.append({'cn': phr, 'en': None})
            else:
                phr_list.append({'cn': phr_match.group('cn'), 'en': phr_match.group('en')})
        return phr_list


    @staticmethod
    def _is_eng(text):
        if (float(sum([(1 if ord(char)<128 else 0) for char in text])) / len(text)) > 0.5:
            return True
        else:
            return False

    @classmethod
    def _parse_remarks(cls, line_list: list):
        rem_list = []
        for line in line_list:
            if line.strip():
                rem_list.append([line[0], line[1:].strip()])
        return rem_list
