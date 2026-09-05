from deep_translator import GoogleTranslator

translator = GoogleTranslator(source='en', target='th')

text = "Hello _M0_, how are you _M1_?"
print(translator.translate(text))

text2 = "Hello M0M, how are you M1M?"
print(translator.translate(text2))

text3 = "Hello <span class=\"notranslate\">$h</span>, how are you?"
print(translator.translate(text3))

text4 = "Hello ZZZ0ZZZ, how are you ZZZ1ZZZ?"
print(translator.translate(text4))
