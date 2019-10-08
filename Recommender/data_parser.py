import gzip


# Parsing data from gzip file.
def parse_data(path: str):
    file = gzip.open(path, 'rt', encoding="UTF-8")
    for line in file:
        yield eval(line)
    file.close()
